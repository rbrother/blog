(ns brotherus.blog.home
  (:require
    [re-frame.core :as rf]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.item-page :as item-page]
    [brotherus.blog.info :as info]
    [brotherus.blog.items-list :as items-list]))

(defn blog-home []
  [:<>
   [:div {:style {:width "100%", :position "relative", :display "inline-block"}}
    [:img {:src "/images/background_tech_face.jpg" :style {:width "100%", :height "auto"}}]
    [:div {:style {:position "absolute", :top "50%", :left "50%", :transform "translate(-50%, -50%)"
                   :background "black" :padding "32px 128px 32px 128px" :text-align "center"}}
     [:h1 {:style {:color "#FFF"}} "Building Programs"]
     [:p {:style {:color "#888" :font-family "Roboto"}} "A Blog about Love for Creating Software"]]]
   [items-list/view "Computers"] ;; Default filter for the "Building Programs" blog, allows other blogs with other themes later based on filter
   ])

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
    (dissoc db :filter :selected-item :page :article-content)))
