(ns brotherus.blog.home
  (:require
    [re-frame.core :as rf]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.item-page :as item-page]
    [brotherus.blog.info :as info]
    [brotherus.blog.items-list :as items-list]))

(defn main-panel []
  (let [page @(rf/subscribe [::page])
        filter @(rf/subscribe [::filters/filter])
        selected-item @(rf/subscribe [::item-page/selected-item])]
    [:div.main-container
     [:div.main
      [:div [:a {:href "/"}
             [:div.main-title "Building Programs"]]]
      [:div.justify-end
       [:a.pad {:href "/about"} "About"]]
      (cond
        (= page :info) [info/view]
        selected-item [item-page/view]
        :else [items-list/view filter])]]))

;; Subs

(rf/reg-sub ::page (fn [db _] (:page db)))

;; Events

(rf/reg-event-db ::home
  (fn [db _]
    (dissoc db :filter :selected-item :page)))