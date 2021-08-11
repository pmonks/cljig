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

; Because java.util.logging is a hot mess
(org.slf4j.bridge.SLF4JBridgeHandler/removeHandlersForRootLogger)
(org.slf4j.bridge.SLF4JBridgeHandler/install)

; Because Java's default exception behaviour in threads other than main is a hot mess
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ t e]
     (binding [*out* *err*]
       (println (str "Uncaught exception thrown in thread " (.getName t)))
       (.printStackTrace e *err*)))))

; Handy default imports for code exploration
(require '[clojure.string           :as s])
(require '[clojure.set              :as set])
(require '[clojure.pprint           :as pp])
(require '[clojure.java.io          :as io])
(require '[clojure.java.javadoc     :as jd])
(require '[clojure.repl             :as repl])
(require '[clojure.reflect          :as rf])
(require '[clojure.tools.deps.alpha :as tda])

(require '[cljig.web   :as web]  :reload-all)
(require '[cljig.deps  :as deps] :reload-all)
(require '[cljig.docs  :as docs] :reload-all)

(defn help
  []
  (println "\nThese namespaces are loaded and available to you via these aliases:\n")
  (println "  s    - clojure.string")
  (println "  set  - clojure.set")
  (println "  pp   - clojure.pprint")
  (println "  io   - clojure.java.io")
  (println "  jd   - clojure.java.javadoc")
  (println "  repl - clojure.repl")
  (println "  rf   - clojure.reflect")
  (println "  tda  - clojure.tools.deps.alpha")
  (println)
  (println "  web  - cljig.web")
  (println "  deps - cljig.deps")
  (println "  docs - cljig.docs")
  (println)
  (println "Try starting out by using the (docs/ns-docs `ns) fn to learn more about these (or any other) namespaces.")
  (println)
  (println "To get this message back, invoke the (help) fn at any time.")
  (println)
  (flush))

(help)
