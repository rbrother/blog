(ns brotherus.blog.items-list
  (:require [brotherus.blog.db :refer [articles]]
            [brotherus.blog.filters :as filters]))

(defn view [tag]
  (into
    [:div.product-table.col-span-2]
    (for [{:keys [name id thumbnail date]} (filters/filter-articles articles tag)]
      [:div.article-box
       [:a {:href (str "/item/" id)}
        [:div.crop-container
         [:img.cropped-image {:src thumbnail}]]
        [:div.margin2 name]
        [:div.grid.margin2 {:style {:grid-template-columns "auto 1fr" :align-items "center"}}
         [:div [:img {:src "images/robert.jpg" :style {:width "50px"}}]]
         [:div.small
          [:div "Robert J. Brotherus"]
          [:div date]]]]])))
