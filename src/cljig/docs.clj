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

(ns cljig.docs
  "cljig - fns related to developer documentation"
  (:require [clojure.string  :as s]
            [clojure.repl    :as repl]
            [clojure.reflect :as rf]
            [cljig.web       :as jw]))

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

(defn cljdoc
  "Opens cljdoc.org search with the given term(s)."
  [terms]
  (jw/open-url (str "https://cljdoc.org/search?q=" (jw/encode-qs terms))))

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
  ([java-class]              (cljig.docs/javadoc java-class 11))
  ([java-class java-version] (jw/lucky (str "java " java-version " javadoc " java-class))))
