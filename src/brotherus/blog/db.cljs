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

;; URL of articles should be eventually in form:
;;     https://www.brotherus.net/post/<id>
;; eg. https://www.brotherus.net/post/don-t-llms-really-have-understanding
;; For articles id can also be shorter and id2 is alias for backward compatibility
(def articles
  [{:name "Humble Beginnings: the VIC-20", :id "humble-beginnings-the-vic-20", :date "2019-02-15",
    :tags #{"Computers" "VIC-20" "Commodore"}}
   {:name "Why so many languages?", :id "why-so-many-languages", :date "2019-03-13",
    :tags #{"Computers" "Programming" "Learning"}}
   {:name "From a Product company to a Consulting company", :id "from-a-product-company-to-a-consulting-company"
    :date "2020-02-06", :tags #{"Computers" "Programming" "Jobs" "Career"}}
   {:name "Creating the INFIA Spectrum Analysis Software in 1996-1998"
    :id "infia", :id2 "creating-the-infia-spectrum-analysis-software-in-1996-1998"
    :date "2024-12-21", :tags #{"Computers" "Programming" "Jobs" "Career"}}
   {:name "Don't LLMs really have understanding?", :id "don-t-llms-really-have-understanding",
    :date "2025-03-28", :url "llm-understanding/article.md"
    :thumbnail "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/llm-understanding/thumbnail.jpg"
    :tags #{"Computers" "AI" "LLM"}}
   {:name "Resurrecting Infia in 2025", :id "resurrecting-infia",
    :date "2025-05-01", :url "resurrect-infia-2025/article.md"
    :thumbnail "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/resurrect-infia-2025/thumbnail.jpg"
    :tags #{"Computers" "Programming" "Development Tools" "Delphi"}}])

(def articles-index (index-by :id articles))

(rf/reg-event-db ::initialize-db
  (fn [_] {}))