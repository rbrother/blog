(ns brotherus.blog.db
  (:require [medley.core :refer [index-by]]
            [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]))

(def tags
  [{:name "Agile"}
   {:name "AI"}
   {:name "Career"}
   {:name "Chemistry"}
   {:name "Clojure"}
   {:name "ClojureScript"}
   {:name "Commodore"}
   {:name "Computers"}
   {:name "C++"}
   {:name "Delphi"}
   {:name "Development Tools"}
   {:name "Fortran"}
   {:name "Jobs"}
   {:name "Learning"}
   {:name "LLM"}
   {:name "Mathematics"}
   {:name "Markdown"}
   {:name "Physical Chemistry"}
   {:name "Programming"}
   {:name "Re-Frame"}
   {:name "Science"}
   {:name "VIC-20"}])

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
   {:name "Blast from my Commodore 64 Past", :id "ruletti-c64", :id2 "blast-from-my-commodore-64-past",
    :date "2021-08-10", :tags #{"Computers" "Commodore" "Programming"}}
   {:name "Diving into Ruletti-64 code", :id "ruletti-code",
    :date "2021-08-25", :tags #{"Computers" "Commodore" "Programming"}}
   {:name "Ruletti re-born after 34 years", :date "2021-09-09", :id "ruletti-reborn"
    :id2 "ruletti-re-born-after-34-years", :tags #{"Computers" "Commodore" "Programming" "Clojure" "Re-Frame"}}
   {:name "Developer experience in 1987 with C64 vs modern tools", :date "2021-12-18", :id "ruletti-developer-experience"
    :id2 "developer-experience-in-1987-with-c64-vs-modern-tools", :tags #{"Computers" "Commodore" "Programming" "Development Tools"}}
   {:name "From imperative C64 Basic to functional-reactive programming", :date "2022-02-06"
    :id "ruletti-imperative-to-functional", :id2 "from-depths-of-imperative-programming-to-heights-of-functional-style"
    :tags #{"Computers" "Programming" "Commodore" "Re-Frame" "Clojure" "ClojureScript"}}
   {:name "Why do I like Programming so much?", :id "i-like-programming"
    :id2 "why-do-i-like-programming-so-much", :date "2022-05-08"
    :tags #{"Computers" "Programming" "Learning"}}
   {:name "Excel-revolution at Steel Factory in 1989" :date "2022-05-29", :updated "2024-12-25"
    :id "ovako-excel" :id2 "excel-revolution-at-steel-factory-in-1989",
    :tags #{"Computers" "Programming" "Jobs" "Career"}}
   {:name "Logistic Map Fractal", :id "logistic-map-fractal", :date "2022-10-14"
    :tags #{"Computers" "Programming" "Learning"}}
   {:name "My entry to Quantum Chemistry programming", :date "2023-01-03"
    :id "entry-to-chemistry-programming", :id2 "starting-on-a-career-in-computational-quantum-chemistry"
    :tags #{"Computers" "Programming" "C++" "Chemistry" "Physical Chemistry" "Mathematics" "Learning" "Career"}}
   {:name "Death by Fortran Common Block", :id "death-by-fortran-common-block", :date "2024-01-17"
    :tags #{"Computers" "Programming" "Fortran" "Chemistry" "Physical Chemistry"}}
   {:name "Creating the INFIA Spectrum Analysis Software in 1996-1998"
    :id "infia", :id2 "creating-the-infia-spectrum-analysis-software-in-1996-1998"
    :date "2024-12-21", :tags #{"Computers" "Programming" "Jobs" "Career"}}
   {:name "Don't LLMs really have understanding?", :date "2025-03-28"
    :id "llm-understanding" :id2 "don-t-llms-really-have-understanding",
    :tags #{"Computers" "AI" "LLM"}}

   ;; Publish new version of blog first, this then:
   #_{:name "Resurrecting Infia in 2025", :id "resurrect-infia-2025",
      :date "2025-05-01" :tags #{"Computers" "Programming" "Development Tools" "Delphi"}}

    ;; TODO convert drafts from Wix as well. Mark with "state: draft" which shows only in dev mode

   #_{:name "The tech stack of this blog"
    :id "blog-tech-stack", :date "2025-06-01"
    :tags #{"Computers" "Programming" "Development Tools" "Clojure" "ClojureScript" "Re-Frame" "Markdown"}}


   #_{:name "The Agile Manifesto and its 12 Principles"}


   ])

(def articles-index (index-by :id articles))

(def articles-index2 (index-by :id2 articles)) ;; Old long ids for backward compatibility

;; TODO: Validate that all tags are found in tag-list and that tag-list does not have ophans

(rf/reg-event-db ::initialize-db
                 (fn [_] {}))