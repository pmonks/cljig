;
; Copyright © 2021 Peter Monks
;
; This file is part of pmonks/cljig
;
; pmonks/cljig is free software: you can redistribute it and/or modify
; it under the terms of the GNU Affero General Public License as published by
; the Free Software Foundation, either version 3 of the License, or
; (at your option) any later version.
;
; pmonks/cljig is distributed in the hope that it will be useful,
; but WITHOUT ANY WARRANTY; without even the implied warranty of
; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
; GNU Affero General Public License for more details.
;
; You should have received a copy of the GNU Affero General Public License
; along with pmonks/cljig.  If not, see <https://www.gnu.org/licenses/>.
;

(ns cljig.core
  "A little Clojure jig for discovering, evaluating, and prototyping 3rd party Clojure and Java libraries, for eventual use in your own project(s)."
  (:require [clojure.string               :as s]
            [clojure.java.io              :as io]
            [clojure.repl                 :as repl]
            [clojure.reflect              :as rf]
            [clojure.tools.deps.alpha     :as tda]
            [clojure.tools.namespace.find :as tnsf]
            [find-deps.core               :as fd]))

(def ^:private encode-qs #(java.net.URLEncoder/encode % "UTF-8"))

(defmulti  open-browser "Opens the user's default browser to the given URL." class)
(defmethod open-browser java.net.URI     [^java.net.URI uri] (.browse (java.awt.Desktop/getDesktop) uri))
(defmethod open-browser java.net.URL     [^java.net.URL url] (open-browser (.toURI url)))
(defmethod open-browser java.lang.String [^String s]         (open-browser (java.net.URI. s)))
(defmethod open-browser java.io.File     [^java.io.File f]   (open-browser (.toURI f)))

(defn lucky
  "Performs a google search for the given term and attempts to display the first result (via the 'I'm feeling lucky' feature, which doesn't always jump to the first result)."
  [term]
  (open-browser (format "https://www.google.com/search?q=%s&btnI=&sourceid=navclient&gfns=1" (encode-qs term))))

(defn search-github
  "Opens the default browser with the results of a GitHub search for Clojure repositories with the given term."
  [term]
  (open-browser (str "https://github.com/search?type=Repositories&q=language%3AClojure+" (encode-qs term))))

; Note: GitLab doesn't (yet) support actual language filters, so we just add the term "clojure" to the search (which is super-lame...)
; GitLab's issue tracking this is: https://gitlab.com/groups/gitlab-org/-/epics/5476
(defn search-gitlab
  "Opens the default browser with the results of a GitLab search for Clojure projects with the given term."
  [term]
  (open-browser (str "https://gitlab.com/search?search=clojure+"  (encode-qs term))))

; BitBucket is a lot less "open source friendly", and so far I haven't figured out how to search public repositories (if indeed it even supports such a thing...).
(defn search-bitbucket
  "Opens the default browser with the results of a BitBucket search for Clojure projects with the given term."
  [term]
  (open-browser (str "https://bitbucket.org/dashboard/repositories?search=clojure+" (encode-qs term))))

(def search #'search-github)

(defn query
  "Returns up to max-results potential Clojure libraries (in tools.deps.alpha dep maps format) that match the given search term."
  ([term] (query term 1))
  ([term max-results]
   (apply fd/query* [term {:sources [:clojars :mvn]
                           :rank    :fuzzy
                           :format  :deps
                           :limit   max-results}])))

(defn nses
  "Determines the namespaces provided by the given dep(s).  Both the argument and result are dep maps in tools.deps.alpha format e.g.

  {http-kit/http-kit {:mvn/version \"2.5.0\"}
   ring/ring         {:mvn/version \"1.8.2\"}}

The result will add a :nses key to each dependency e.g.

  {http-kit/http-kit {:mvn/version \"2.5.0\" :nses [org.httpkit.timer org.httpkit.client org.httpkit.encode org.httpkit.server org.httpkit.sni-client]}
   ring/ring         {:mvn/version \"1.8.2\" :nses []}}

Notes:
  * Has the side effect of downloading each dependency's artifact(s), as well as (recursively) all artifacts they depend on.
  * As currently written, only supports libraries hosted on Maven central, Clojars, and GitHub. I think this is easy to change, by accepting a full deps.edn map, instead of just the :deps map within it.
  * As currently written, doesn't maintain the association between individual JAR files / directories within a dependency and namespace(s). This would be easy enough to do, but complicates the output and didn't seem necessary."
  [deps]
  (let [tda-deps      {:mvn/repos {"central" {:url "https://repo1.maven.org/maven2/"}
                                   "clojars" {:url "https://repo.clojars.org/"}}
                       :deps deps}
        resolved-deps (select-keys (tda/resolve-deps tda-deps nil) (keys deps))]
    (apply merge (map (fn [[dep attr]] {dep (assoc attr :nses (vec (tnsf/find-namespaces (map io/file (:paths attr)))))}) resolved-deps))))

(comment
(defn load-deps
  [deps]
  (tda/add-libs TODO!!!!))
)

(defn ns-docs
  "Prints the docstrings of all public vars in the given namespace (provided as a symbol).  Note that the namespace must already have been loaded."
  [ns]
  (println "-------------------------")
  (println ns)
  (println (:doc (meta (find-ns ns))))
  (dorun
    (map (comp #'repl/print-doc meta)
         (->> ns
              ns-publics
              sort
              vals))))

(defn ns-docs-for-deps
  "Prints namespace docs for all of the namespaces in the given deps (in tools.deps.alpha format)."
  [deps]
  (let [nses (mapcat :nses (vals deps))]
    (doall (map ns-docs nses))))

(defn java-methods
  "Handy method for returning all public Java methods in the given class or object.

  Note: does not take inherited methods into account!  Use clojure.core/ancestors for that."
  [java-class-or-obj]
  (when java-class-or-obj
    (->> java-class-or-obj rf/reflect
           :members
           (filter :return-type)
           (filter #(contains? (:flags %) :public))
           (map #(str "("
                      (if (contains? (:flags %) :static)
                        (str (rf/typename java-class-or-obj) "/")
                        ".")
                      (:name %)
                      " "
                      (s/join " " (:parameter-types %))
                      ") => "
                      (:return-type %) "\n"))
           distinct
           println)))

(defn javadoc
  "Attempts to find and open javadoc for the given Java class (a symbol or String), optionally for the given Java version (defaults to the latest LTS)."
  ([java-class]              (cljig.core/javadoc java-class 11))
  ([java-class java-version] (lucky (str "java " java-version " javadoc " java-class))))
