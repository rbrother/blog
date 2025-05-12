(ns brotherus.blog.info
  (:require [brotherus.blog.components :as components]
            [re-frame.core :as rf]
            [brotherus.blog.item-page :as item-page]))

(defn view []
  [:<>
   components/title-panel
   [:div.article-inner
    [:div {:style {:display "grid", :grid-template-columns "auto 1fr", :gap "32px"}}
     [:div [:img {:src "images/Robert_Brotherus_portrait.avif"
                  :style {:width "400px" :border-radius "30%"}}]]
     [:div {:style {:max-width "600px" :justify-self "start" :align-self "start"}}
      [item-page/article-html]]]]])

;; Events

(rf/reg-event-fx
  ::show-info
  (fn [{:keys [db]} _]
    {:db (assoc db :page :info)
     :dispatch [::item-page/load-article "about/article.md"]}))
