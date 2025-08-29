(ns brotherus.blog.filters)

;; Helpers

(defn filter-articles [articles tag]
  (if-not tag articles
              (->> articles
                   (filter (fn [{tags :tags}] (get tags tag)))
                   (sort-by :date #(compare %2 %1)))))
