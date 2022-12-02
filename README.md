[![CI](https://github.com/pmonks/cljig/workflows/CI/badge.svg?branch=main)](https://github.com/pmonks/cljig/actions?query=workflow%3ACI+branch%3Amain) [![Dependencies](https://github.com/pmonks/cljig/workflows/dependencies/badge.svg?branch=main)](https://github.com/pmonks/cljig/actions?query=workflow%3Adependencies+branch%3Amain) [![Open Issues](https://img.shields.io/github/issues/pmonks/cljig.svg)](https://github.com/pmonks/cljig/issues) [![License](https://img.shields.io/github/license/pmonks/cljig.svg)](https://github.com/pmonks/cljig/blob/main/COPYING)

# cljig

A little Clojure [jig](https://en.wikipedia.org/wiki/Jig_(tool)) for discovering, evaluating, and prototyping 3rd party
Clojure and Java libraries, for eventual use in your own project(s), all from the REPL.

**Important note: this started out as a little "weekend experiment" which quickly ran out of steam. These days you're better off looking at something like [babashka + add-deps](https://book.babashka.org/#babashkadeps) for a more comprehensive and battle-tested equivalent.**

## Trying it Out
Clone the repo, then run:

```shell
$ clj -i init.clj -r
```

This will load some useful namespaces, display some help, the drop you in a REPL, ready to explore.

### Example
```clojure
user=> (web/search-github "gravity")
nil
user=> ; Browser opens, showing Clojure repositories on GitHub that match "gravity"
user=> ; pmonks/gravity catches our eye...
user=> (deps/search "pmonks/gravity" 10)
{:deps {clj-commons/multigrep #:mvn{:version "1.0.128"}, org.clojars.pmonks/multigrep #:mvn{:version "0.4.0"}, com.github.pmonks/multigrep #:mvn{:version "1.0.135"}}}
user=> ; Looks nice - let's load the library and have a play
user=> (def mg-deps (deps/search "multigrep"))
#'user/mg-deps
user=> (deps/load-deps mg-deps)
Downloading: clj-commons/multigrep/0.5.0/multigrep-0.5.0.pom from clojars
Downloading: clj-commons/multigrep/0.5.0/multigrep-0.5.0.jar from clojars
#object[clojure.core$future_call$reify__8477 0x4f9871a2 {:status :pending, :val nil}]
user=> ; What namespaces does it offer?
user=> (deps/nses mg-deps)
#:clj-commons{multigrep {:mvn/version "0.5.0", :deps/manifest :mvn, :parents #{[]}, :paths ["~/.m2/repository/clj-commons/multigrep/0.5.0/multigrep-0.5.0.jar"], :nses [multigrep.core]}}
user=> ; Lets make that a bit more readable...
user=> (for [[k v] (deps/nses mg-deps)] [k (:nses v)])
([clj-commons/multigrep [multigrep.core]])
user=> ; Require the library's core namespace...
user=> (require '[multigrep.core :as mg])
nil
user=> ; Pull up the namespace's docstrings
user=> (docs/namespace 'multigrep.core)
-------------------------
multigrep.core
nil
-------------------------
multigrep.core/grep
([r f])
  Returns a sequence of maps representing each of the matches of r (one or more regexes) in f (one or more things that can be read by clojure.io/reader).

Each map in the sequence has these keys:
  {
    :file         ; the entry in f that matched
    :line         ; text of the line that matched
    :line-number  ; line-number of that line (note: 1 based)
    :regex        ; the entry in r that matched
    :re-seq       ; the output from re-seq for this line and this regex
  }
-------------------------
multigrep.core/greplace!
([r s f] [r s f in-memory-threshold])
  Applies r (a single regex) to f (one or more things that can be read by clojure.io/reader), substituting s (a string, or a function of one parameter (the match(es) from the regex) returning a string).

Returns a sequence of maps representing each of the substitutions.  Each map in the sequence has these keys:
  {
    :file         ; the entry in f that matched
    :line-number  ; line-number of the line that had one or more substitutions (note: 1 based)
  }

The optional fourth parameter specifies at what file size processing should switch from in-memory to on-disk.  It defaults to 1MB.
nil
user=> ; Take the library for a spin...
user=> (mg/grep #"something" "/usr/share/dict/words")
({:file "/usr/share/dict/words", :line "something", :line-number 184879, :regex #"something", :re-seq ("something")} {:file "/usr/share/dict/words", :line "somethingness", :line-number 184880, :regex #"something", :re-seq ("something")} {:file "/usr/share/dict/words", :line "undersomething", :line-number 214685, :regex #"something", :re-seq ("something")})
user=> ^D
```

## Contributor Information

[GitHub project](https://github.com/pmonks/cljig)

[Bug Tracker](https://github.com/pmonks/cljig/issues)

## License

Copyright © 2021 Peter Monks

This work is licensed under the [GNU Affero General Public License v3.0 or later](http://www.gnu.org/licenses/agpl-3.0.html).
If this is problematic for you or your employer, feel free to reach out.

SPDX-License-Identifier: [AGPL-3.0-or-later](https://spdx.org/licenses/AGPL-3.0-or-later.html)
