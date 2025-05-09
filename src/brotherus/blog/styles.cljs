(ns brotherus.blog.styles
  (:require [spade.core :refer [defglobal]]))

(def small-font "12px")
(def medium-font "18px")
(def large-font "30px")

(def default-font
  {:color :black, :background-color :white
   :font-family "Libre Baskerville"
   :font-size medium-font
   :font-weight "400"})

(def light-font {:font-weight 300})

(defglobal
  defaults
  #_[:div {:border "dotted 1px #888"}] ;; For debugging layouts
  [:body (assoc default-font :user-select "none", :margin 0)]
  [:.main-title {:font-size "25px", :font-weight 400}]
  [:.logo-font {:font-family "fredericka the great" :font-size "90px"}]
  [:.small {:font-size small-font}]
  [:.large {:font-size large-font}]
  [:.relative {:position "relative"}]
  [:.absolute {:position "absolute"}]
  [:.pad {:padding "8px"}]
  [:.center {:text-align "center"}]
  [:.margin-top {:margin-top "8px"}]
  [:.margin {:margin "8px"}]
  [:.margin2 {:margin "16px"}]
  [:.bold {:font-weight "bold"}]
  [:.gray {:color "#AAA"}]
  [:div.flex {:display "flex" :align-items "center"}]
  [:div.error {:display "grid" :grid-template-columns "1fr auto"
               :background "#f33" :color "black" :font-weight "bold"
               :padding "8px" :align-items "center"}]
  [:div.top-level {:margin "0px" :padding "0px"}]
  [:div.main-container {:background "#EEE"}]
  [:div.main { :max-width "1000px", :margin-left "auto", :margin-right "auto"
              :padding-top "32px"
              :background "white"}]
  [:div.article { :margin-left "100px", :margin-right "100px", :line-height 1.6}]
  ["div.article img" {:width "800px"}]

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
  [:div.justify-end {:justify-self "end"}]
  [:div.justify-center {:justify-self "center", :text-align "center"}]
  [:a (merge light-font {:color "black", :text-decoration "none"})]
  [:.light-font light-font]
  )

