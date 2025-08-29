(defproject brotherus-blog "1.0"

  ;; NOTE: This is *dummy* project-file to satisfy IntelliJ IDEA intellisense needs.
  ;; We are now using Shadow-cljs.edn as the primary first-class project file.

  :description "Robert Brotherus Blog Software"
  :url "http://www.brotherus.net"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [medley "1.4.0"]
                 [hashp "0.2.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [org.clojure/data.avl "0.1.0"]
                 [com.rpl/specter "1.1.4"]
                 [metosin/malli "0.10.1"]
                 [rm-hull/infix "0.4.1"]
                 [camel-snake-kebab "0.4.3"]
                 [taipei.404/html-to-hiccup "0.1.8"]]

  :source-paths ["src"]

  :test-paths ["test"])
