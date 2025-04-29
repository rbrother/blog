(ns brotherus.blog.styles
  (:require [spade.core :refer [defglobal]]))

(def small-font "12px")
(def medium-font "16px")
(def large-font "30px")

(def default-font
  {:color :black, :background-color :white
   :font-family "Libre Baskerville"
   :font-size medium-font
   :font-weight "400"})

(def light-font {:font-weight 300})

(defglobal
  defaults
  ;; [:div {:border "dotted 1px #888"}] ;; For debugging layouts
  [:body (assoc default-font :user-select "none" :padding "16px")]
  [:.main-title {:font-size "25px", :font-weight 400}]
  [:.logo-font {:font-family "fredericka the great" :font-size "90px"}]
  [:.small {:font-size small-font}]
  [:.large {:font-size large-font}]
  [:.relative {:position "relative"}]
  [:.absolute {:position "absolute"}]
  [:.pad {:padding "8px"}]
  [:.center {:text-align "center"}]
  [:.margin-top {:margin-top "8px"}]
  [:.margin-right-32 {:margin-right "32px"}]
  [:.margin {:margin "8px"}]
  [:.margin2 {:margin "16px"}]
  [:.bold {:font-weight "bold"}]
  [:.gray {:color "#AAA"}]
  [:div.flex {:display "flex" :align-items "center"}]
  [:div.error {:display "grid" :grid-template-columns "1fr auto"
               :background "#f33" :color "black" :font-weight "bold"
               :padding "8px" :align-items "center"}]
  [:div.main {:display "grid", :grid-template-columns "auto auto"
              :grid-gap "32px"
              :max-width "1200px"
              :margin-left "auto", :margin-right "auto"}]
  [:div.product-table
   {:display "grid"
    :grid-template-columns "repeat(3, 300px)"
    :gap "40px"}]
  [:div.links-table
   {:display "grid"
    :grid-template-columns "repeat(3, 300px)"
    :gap "8px"}]
  [:div.article-box {:border "solid 1px gray"}]
  [:div.crop-container {:width "298px" :height "160px" :overflow "hidden" :position "relative"}]
  [:img.cropped-image {:width "100%" :height "100%" :object-fit "cover"}]
  [:img.large-photo {:width "50%"}]
  [:div.grid {:display "grid"}]
  [:div.col-span-2 {:grid-column "span 2"}]
  [:div.justify-end {:justify-self "end"}]
  [:div.justify-center {:justify-self "center", :text-align "center"}]
  [:a (merge light-font {:color "black", :text-decoration "none"})]
  [:.light-font light-font]
  )

