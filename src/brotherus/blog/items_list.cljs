(ns brotherus.blog.items-list
  (:require [brotherus.blog.db :refer [articles]]
            [brotherus.blog.filters :as filters]
            [brotherus.blog.components :as components]))

(defn view [tag]
  [:div
   [:div.article-inner
    (into
      [:div.product-table]
      (for [{:keys [name id thumbnail date]} (filters/filter-articles articles tag)]
        [:div.article-box
         [:a {:href (str "/post/" id)}
          [:div.crop-container
           [:img.cropped-image {:src (or thumbnail (str components/articles-base-url "/" id "/thumbnail.jpg"))}]]
          [:div.margin2 name]
          [:div.grid.margin2 {:style {:grid-template-columns "auto 1fr" :align-items "center"}}
           [:div components/robert-small-pic]
           [:div.small
            [:div "Robert J. Brotherus"]
            [:div date]]]]]))]])
