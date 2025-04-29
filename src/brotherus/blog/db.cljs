(ns brotherus.blog.db
  (:require [medley.core :refer [index-by]]
            [re-frame.core :as rf]))

(def tags
  [{:name "Computers"}
   {:name "VIC-20"}
   {:name "Commodore"}
   {:name "Programming"}
   {:name "Jobs"}
   {:name "Career"}
   {:name "Learning"}
   {:name "Development Tools"}
   {:name "Clojure"}
   {:name "ClojureScript"}
   {:name "Re-Frame"}
   {:name "C++"}
   {:name "Science"}
   {:name "Chemistry"}
   {:name "Physical Chemistry"}
   {:name "Fortran"}
   {:name "Delphi"}
   {:name "Agile"}
   {:name "AI"}
   {:name "LLM"}])

;; URL of articles should be in form:
;;     https://www.brotherus.net/post/<id>
;; eg. https://www.brotherus.net/post/don-t-llms-really-have-understanding
(def articles
  [{:name "Resurrecting Infia in 2025", :id "resurrecting-infia",
    :date "2025-05-01", :url "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/resurrect-infia-2025/article.md"
    :thumbnail "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/resurrect-infia-2025/thumbnail.jpg"
    :tags #{"Computers" "Programming" "Development Tools" "Delphi"}}
   {:name "Don't LLMs really have understanding?", :id "don-t-llms-really-have-understanding",
    :date "2025-03-28",
    :thumbnail "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/llm-understanding/thumbnail.jpg"
    :tags #{"Computers" "AI" "LLM"}}])

(def articles-index (index-by :id articles))

(rf/reg-event-db ::initialize-db
  (fn [_] {}))