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

(ns cljig.web
  "cljig - fns related to the web")

(def encode-qs #(java.net.URLEncoder/encode ^String % "UTF-8"))

(defmulti  open-url "Opens the user's default browser to the given URL." class)
(defmethod open-url java.net.URI     [^java.net.URI uri] (.browse (java.awt.Desktop/getDesktop) uri))
(defmethod open-url java.net.URL     [^java.net.URL url] (open-url (.toURI url)))
(defmethod open-url java.lang.String [^String s]         (open-url (java.net.URI. s)))
(defmethod open-url java.io.File     [^java.io.File f]   (open-url (.toURI f)))

(defn google
  "Performs a google search for the given term(s)."
  [terms]
  (open-url (format "https://www.google.com/search?q=%s" (encode-qs terms))))

(defn google-clj
  "Performs a google search for the given term(s), with 'clojure' added to the search."
  [terms]
  (open-url (format "https://www.google.com/search?q=clojure+%s" (encode-qs terms))))

(defn lucky
  "Performs a google search for the given term(s) and attempts to display the first result (via the 'I'm feeling lucky' feature, which doesn't always jump to the first result)."
  [terms]
  (open-url (format "https://www.google.com/search?q=%s&btnI=&sourceid=navclient&gfns=1" (encode-qs terms))))

(defn lucky-clj
  "Performs a google search for the given term(s), with 'clojure' added to the search, and attempts to display the first result (via the 'I'm feeling lucky' feature, which doesn't always jump to the first result)."
  [terms]
  (open-url (format "https://www.google.com/search?q=%s&btnI=&sourceid=navclient&gfns=1" (encode-qs terms))))

(defn github
  "Opens a browser with the results of a GitHub search for Clojure repositories with the given term(s)."
  [terms]
  (open-url (str "https://github.com/search?type=Repositories&q=language%3AClojure+" (encode-qs terms))))

; Note: GitLab doesn't (yet) support actual language filters, so we just add the term "clojure" to the search (which is super-lame...)
; GitLab's issue tracking this is: https://gitlab.com/groups/gitlab-org/-/epics/5476
(defn gitlab
  "Opens a browser with the results of a GitLab search for Clojure projects with the given term(s)."
  [terms]
  (open-url (str "https://gitlab.com/search?search=clojure+"  (encode-qs terms))))

; BitBucket is a lot less "open source friendly", and so far I haven't figured out how to search public repositories (if indeed it even supports such a thing...).
(defn bitbucket
  "Opens a browser with the results of a BitBucket search for Clojure projects with the given term(s)."
  [terms]
  (open-url (str "https://bitbucket.org/dashboard/repositories?search=clojure+" (encode-qs terms))))

(defn slack-web
  "Opens clojurians Slack team in a browser."
  []
  (open-url "https://clojurians.slack.com/"))

(defn slack-app
  "Opens clojurians Slack team in the app (which must be installed)."
  []
  (open-url "slack://open?team=T03RZGPFR"))

(def slack slack-web)

(defn cljdoc
  "Opens cljdoc.org in a browser."
  []
  (open-url "https://cljdoc.org/"))

(defn clojureverse
  "Opens clojureverse in a browser."
  []
  (open-url "https://clojureverse.org/"))

(defn discord
  "Opens the Clojure Discord server in a browser."
  []
  (open-url "https://discord.gg/discljord"))   ; Note: the other "Clojure" Discord server doesn't have an administrator, resulting in spam and other undesirable behaviour
