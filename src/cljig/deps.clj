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

(ns cljig.deps
  "cljig - fns related to dependencies"
  (:require [clojure.string                :as s]
            [clojure.java.io               :as io]
            [clojure.tools.deps.alpha      :as tda]
;            [clojure.tools.deps.alpha.repl :as tdar]   ; Awaiting functional add-libs impl
            [cemerick.pomegranate          :as pom]     ; While we wait for add-libs to work
            [cemerick.pomegranate.aether   :as poma]
            [clojure.tools.namespace.find  :as tnsf]
            [find-deps.core                :as fd]))

(defn search
  "Returns up to max-results potential Clojure libraries hosted in clojars or Maven Central, that match the given search term.  Result is in tools.deps.alpha dep maps format."
  ([term] (search term 1))
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

(comment   "Waiting for tools.deps.alpha to have a functioning version of add-libs..."
(defn load
  "Loads the given deps (in tools.deps.alpha deps map format) into the REPL's classloader, downloading their artifacts (and their dependencies) first if necessary."
  [deps]
  (tdar/add-libs deps))
)

(defn- jitpack-gav
  "Attempts to convert a maven GA t"
  [ga {:keys [git-url sha] :as v}]
  (if (and git-url
           sha
           (s/starts-with? git-url "https://github.com/"))
    [(symbol (str "com.github." (s/replace (s/replace git-url "https://github.com/" "") ".git" ""))) sha]
    (println "⚠️ Unknown dep type for artifact" (str ga ":") v)))

(defn- dep-to-coord
  "Converts a tools.deps dep into a Leiningen style coord e.g.

  {http-kit/http-kit {:mvn/version \"2.5.0\"}}   -->  [http-kit/http-kit \"2.5.0\"]

  Will also attempt to convert Git+SHA deps that point to github.com to jitpack.io equivalents (since Pomegranate can't resolve Git+SHA deps natiovely).  YMMV."
  [[ga v]]
  (if-let [{maven-version :mvn/version} v]
    [ga maven-version]     ; It's a standard Maven-style GAV
    (jitpack-gav ga v)))   ; It's something else (e.g. git+sha) - attempt to convert to jitpack format

; While we wait for tools.deps add-libs to become functional, we fall back on pomegranate (which will fail for anything but Maven-hosted artifacts and some Github deps)...
(defn deps-to-coords
  "Attempt to convert a tools.deps deps map to a Leiningen coords vector."
  [deps]
  (vec (filter identity (map dep-to-coord deps))))

(def ^:private repos (merge poma/maven-central {"clojars" "https://clojars.org/repo" "jitpack" "https://jitpack.io"}))

(defn load
  [deps]
  (pom/add-dependencies :coordinates  (deps-to-coords deps)
                        :repositories repos))
