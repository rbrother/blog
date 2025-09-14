(ns brotherus.blog.styles)

(def small-font "14px")
(def medium-font "18px")
(def large-font "30px")

(def default-font
  {:color :black, :background-color :white
   :font-family "Libre Baskerville"
   :font-size medium-font
   :font-weight "400"})

(def defaults
  [#_[:div {:border "dotted 1px #888"}] ;; For debugging layouts
   #_[:p {:border "dotted 1px #888"}]
   [:body (assoc default-font :margin 0)]
   [:a {:color "inherit", :text-decoration "none"}]
   [:hr {:margin-top "24px", :margin-bottom "24px"}]
   [:.main-title {:font-size "25px", :font-weight 400}]
   [:.roboto {:font-family "Roboto"}]
   [:.roboto-bold {:font-family "Roboto", :font-weight 500}]
   [:.roboto-light {:font-family "Roboto", :font-weight 300}]
   [:.roboto-regular {:font-family "Roboto", :font-weight 400}]
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
   [:div.error {:margin "16px", :border "8px solid red" :padding "16px"}]
   [:div.flex {:display "flex" :align-items "center"}]
   [:div.top-level {:margin "0px" :padding "0px"}]
   [:div.header {:background "black", :color "white", :padding "16px"
                 :font-family "Roboto", :font-size medium-font, :font-weight 300
                 :display "grid", :grid-template-columns "auto 1fr"}]
   [:div.article-container {:background "#EEE"}]
   [:div.article-inner {:max-width "1000px", :margin-left "auto", :margin-right "auto"
                        :padding-top "32px" :padding-bottom "32px"
                        :background "white"}]
   [:div.article {:margin-left "clamp(8px, 8%, 100px)", :margin-right "clamp(8px, 8%, 100px)", :line-height 1.6}]
   ["div.article img" {:max-width "800px" :max-height "500px"}]
   ["div.article img.fullsize" {:max-width "800px" :max-height "none"}]
   ["div.article p:has(img)" {:display "flex" :justify-content "center"}]
   ["div.article p:has(img):has(+ p small)" {:margin-bottom "8px"}]
   ["div.article p:has(small)" {:text-align "center" :margin-top "8px" :padding-top "0px"}]
   ["div.article a" {:color "#40F"}]
   ["div.article pre code" {:overflow "visible", :background "#eee"}]
   ["div.article blockquote" {:margin-left 0, :border-left "5px #bbf solid", :padding-left "24px"
                              :font-style "italic"}]
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
   [:div.justify-center {:justify-self "center", :text-align "center"}]])
