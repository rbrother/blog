(ns brotherus.blog.components)

(def articles-base-url "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/")

(def robert-small-pic
  [:img {:src "/images/robert.jpg"
         :style {:width "50px", :border-radius "50%"}}])

(def title-panel
  [:div {:style {:width "100%", :position "relative", :display "inline-block"}}
   [:img {:src "/images/background_tech_face.jpg" :style {:width "100%", :height "auto"}}]
   [:div {:style {:position "absolute", :top "50%", :left "50%", :transform "translate(-50%, -50%)"
                  :background "black" :padding "16px 64px 16px 64px" :text-align "center"}}
    [:h1 {:style {:color "#FFF"}} "Building Programs"]
    [:p {:style {:color "#888" :font-family "Roboto"}} "A Blog about Love for Creating Software"]]])