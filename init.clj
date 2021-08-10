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

; Handy default imports for code exploration
(require '[clojure.string       :as s])
(require '[clojure.set          :as set])
(require '[clojure.pprint       :as pp])
(require '[clojure.java.io      :as io])
(require '[clojure.java.javadoc :as jd])
(require '[clojure.repl         :as repl])
(require '[clojure.reflect      :as rf])

(require '[cljig.core :as jig] :reload-all)

(println "\nThese namespaces are loaded and available to you via these aliases:\n")
(println "  s    - clojure.string")
(println "  set  - clojure.set")
(println "  pp   - clojure.pprint")
(println "  io   - clojure.java.io")
(println "  jd   - clojure.java.javadoc")
(println "  repl - clojure.repl")
(println "  rf   - clojure.reflect")
(println "  jig  - cljig.core")
(println)
(println "The cljig.core namespace provides these exploration functions:")
(jig/ns-docs 'cljig.core)
(println)
(flush)
