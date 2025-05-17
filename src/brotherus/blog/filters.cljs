(ns brotherus.blog.filters
  (:require
    [re-frame.core :as rf]))

;; Helpers

(defn filter-articles [articles tag]
  (if-not tag articles
              (->> articles
                   (filter (fn [{tags :tags}] (get tags tag)))
                   (sort-by :date #(compare %2 %1)))))

;; Subs

(rf/reg-sub ::filter (fn [db] (:filter db)))

;; Events

(rf/reg-event-db ::select-items
  (fn [db [_ filter-name]]
    (-> db
        (assoc :filter filter-name)
        (dissoc :selected-item :page))))
