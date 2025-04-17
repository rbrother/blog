(ns woolcat.items-list
  (:require [woolcat.db :refer [products]]
            [woolcat.filters :as filters]))

(defn view [tag]
  [:<>
   [:div.main-title tag]
   (into
     [:div.product-table.col-span-2]
     (for [{:keys [name id photo]} (filters/filter-products products tag)]
       [:div
        [:a {:href (str "/item/" id)}
         [:div.crop-container
          [:img.cropped-image {:src (:file photo)}]]
         [:div.margin-top name]]]))
   [filters/filters-without-pics]])
