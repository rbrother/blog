(ns brotherus.blog.filters)

;; Helpers

(defn filter-articles [articles {:keys [tag blog]}]
  (cond->> articles
           tag (filter (fn [{tags :tags}] (get tags tag)))
           blog (filter (fn [{post-blog :blog}] (= blog post-blog)))
           true (sort-by :date #(compare %2 %1))))
