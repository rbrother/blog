(ns brotherus.blog.item-page
  (:require [re-frame.core :as rf]
            [brotherus.blog.db :as db]))

(defn view []
  (let [item-id @(rf/subscribe [::selected-item])
        {:keys [name tags photo description detail-photos folder] :as _item} (get db/articles-index item-id)
        all-photos (if (= folder :only-details)
                     detail-photos
                     (concat [photo] detail-photos))]
    [:<>
     [:div.col-span-2
      (into [:div] (interpose ", " (for [tag tags] [:a {:href (str "/items/" tag)} tag])))
      [:p.main-title name]]
     (into [:<>]
           (for [{:keys [file title]} all-photos]
             [:div.col-span-2.justify-center
              [:img.large-photo {:src file}]
              (when title [:div.light-font title])]))
     [:div.col-span-2
      [:div.light-font description]]]))

;; Subs

(rf/reg-sub ::selected-item (fn [db _] (:selected-item db)))

;; Events

(rf/reg-event-db ::select-item
  (fn [db [_ name]]
    (-> db
        (dissoc :page)
        (assoc :selected-item name))))