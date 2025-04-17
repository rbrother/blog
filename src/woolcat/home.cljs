(ns woolcat.home
  (:require
    [re-frame.core :as rf]
    [woolcat.filters :as filters]
    [woolcat.item-page :as item-page]
    [woolcat.info :as info]
    [woolcat.items-list :as items-list]))

(defn top-selection []
  [:<>
   [:div.col-span-2] ;; spacer
   [filters/filters-with-pics]])

(defn main-panel []
  (let [page @(rf/subscribe [::page])
        filter @(rf/subscribe [::filters/filter])
        selected-item @(rf/subscribe [::item-page/selected-item])]
    [:div.main
     [:div [:a {:href "/"}
            [:div.main-title "Chan Ann Chuang " [:span.gray "Atelier"]]
            [:div.chinese-name "莊誠安"]]]
     [:div.justify-end
      [:a.pad {:href "/items/Stone"} "Stone"]
      [:a.pad {:href "/items/Painting"} "Paint"]
      [:a.pad {:href "/items/CGI"} "CGI"]
      [:a.pad {:href "/items/Travel%20Log"} "Travel Log"]]
     (cond
       (= page :info) [info/view]
       selected-item [item-page/view]
       filter [items-list/view filter]
       :else [top-selection])
     [:div]
     [:div.justify-end
      [:a.pad {:href "/about"} "About"]
      [:a.pad {:href "/item/studio"} "Studio"]]]))

;; Subs

(rf/reg-sub ::page (fn [db _] (:page db)))

;; Events

(rf/reg-event-db ::home
  (fn [db _]
    (dissoc db :filter :selected-item :page)))