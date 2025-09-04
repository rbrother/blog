(ns brotherus.blog.db
  (:require [medley.core :refer [index-by]]))

(def tags
  [{:name "Agile"}
   {:name "AI"}
   {:name "AWS"}
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
   {:name "SPA"}
   {:name "VIC-20"}])

;; URL of articles should be eventually in form:
;;     https://www.brotherus.net/post/<id>
;; eg. https://www.brotherus.net/post/don-t-llms-really-have-understanding
;; For articles id can also be shorter and id-long is alias for backward compatibility

;; Article ideas:
;; TODO: onverting blog to backend, with aid from Augment Code
;; TODO convert drafts from Wix as well. 

;; TODO: Validate that all tags are found in tag-list and that tag-list does not have ophans