(ns brotherus.blog.home
  (:require
    [brotherus.blog.components :as components]
    [re-frame.core :as rf]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.item-page :as item-page]
    [brotherus.blog.info :as info]
    [brotherus.blog.items-list :as items-list]))

(defn blog-home []
  [:<>
   components/title-panel
   [components/error-view]
   [items-list/view "Computers"] ;; Default filter for the "Building Programs" blog, allows other blogs with other themes later based on filter
   [:div.roboto-light {:style {:width "100%", :position "relative", :display "inline-block"}}
    [:img {:src "/images/staircases_background.jpg" :style {:width "100%", :height "auto"}}]
    [:div {:style {:position "absolute", :top "8%", :left "17%", :width "25%"}}
     [:div " “In some ways, programming is like painting. You start with a blank canvas
     and certain basic raw materials. You use a combination of science, art, and
     craft to determine what to do with them.” "]
     [:div [:i " - Andrew Hunt"]]]
    [:div {:style {:position "absolute", :top "50%", :left "65%", :width "25%"}}
     [:div " “Perfection is achieved not when there is nothing more to add,
           but rather when there is nothing more to take away.”  "]
     [:div [:i "– Antoine de Saint-Exupery"]]]]])

(defn main-panel []
  (let [page @(rf/subscribe [::page])
        filter @(rf/subscribe [::filters/filter])
        selected-item @(rf/subscribe [::item-page/selected-item])]
    [:<>
     [:div.header
      [:div [:a {:href "/"} "Building Programs Blog"]]
      [:div.justify-end [:a {:href "/about"} "About"]]]
     (cond
       (= page :info) [info/view]
       selected-item [item-page/view]
       filter [items-list/view filter]
       :else [blog-home])]))

;; Subs

(rf/reg-sub ::page (fn [db _] (:page db)))

;; Events

(rf/reg-event-db ::home
  (fn [db _]
    (dissoc db :filter :selected-item :page :article-content :error)))
