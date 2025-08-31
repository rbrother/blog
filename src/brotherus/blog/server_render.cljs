(ns brotherus.blog.server-render
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require
   [brotherus.blog.config :as config]
   [brotherus.blog.db :as db]
   [brotherus.blog.filters :as filters]
   [brotherus.blog.styles :as styles]
   [garden.core :refer [css]]
   [hiccups.runtime :as hiccupsrt]
   [clojure.string :as str]))

;; CSS generation
(defn generate-css []
  (css styles/defaults))

(defn hiccup-to-html [hiccup-data]
  (-> (str "<!DOCTYPE html>\n" (html hiccup-data))
      (str/replace "&lt;" "<")
      (str/replace "&gt;" ">")
      (str/replace "&amp;" "&")
      (str/replace "&quot;" "\"")))

(defn base-html-template [title content]
  [:html {:lang "en"}
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:name "viewport" :content "width=device-width,initial-scale=1"}]
    [:style (generate-css)]
    [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
    [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin true}]
    [:link {:href "https://fonts.googleapis.com/css2?family=Libre+Baskerville:ital,wght@0,400;0,700;1,400&display=swap"
            :rel "stylesheet"}]
    [:link {:href "https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap"
            :rel "stylesheet"}]
    [:title title]]
   [:body
    [:div#app.top-level
     [:div.header
      [:div [:a {:href "/"} "Building Programs Blog"]]
      [:div.justify-end [:a {:href "/about"} "About"]]]
     content]]])

(def title-panel-component
  [:div {:style "width: 100%; position: relative; display: inline-block;"}
   [:img {:src (config/image-url "background_tech_face.jpg") :style "width: 100%; height: auto;"}]
   [:div {:style "position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: black; padding: 16px 64px 16px 64px; text-align: center;"}
    [:h1 {:style "color: #FFF;"} "Building Programs"]
    [:p {:style "color: #888; font-family: Roboto;"} "A Blog about Love for Creating Software"]]])

(defn robert-small-pic
  "Robert's small profile picture - returns Hiccup"
  []
  [:img {:src (config/image-url "robert.jpg") :style "width: 50px; border-radius: 50%;"}])

(defn article-box
  "Article box component - returns Hiccup"
  [article]
  (let [{:keys [name id thumbnail date]} article
        thumb-url (or thumbnail (str "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/" id "/thumbnail.jpg"))]
    [:div.article-box
     [:a {:href (str "/post/" id)}
      [:div.crop-container
       [:img.cropped-image {:src thumb-url}]]
      [:div.margin2 name]
      [:div.grid.margin2 {:style "grid-template-columns: auto 1fr; align-items: center;"}
       [:div (robert-small-pic)]
       [:div.small
        [:div "Robert J. Brotherus"]
        [:div date]]]]]))

(defn articles-list [articles]
  [:div.article-inner
   [:div.product-table
    (map article-box articles)]])

(defn home-content []
  (let [filtered-articles (filters/filter-articles db/articles "Computers")]
    [:div
     title-panel-component
     (articles-list filtered-articles)
     [:div.roboto-light {:style "width: 100%; position: relative; display: inline-block;"}
      [:img {:src (config/image-url "staircases_background.jpg") :style "width: 100%; height: auto;"}]
      [:div {:style "position: absolute; top: 8%; left: 17%; width: 25%;"}
       [:div "\"In some ways, programming is like painting. You start with a blank canvas and certain basic raw materials. 
              You use a combination of science, art, and craft to determine what to do with them.\""]
       [:div [:i "- Andrew Hunt"]]]
      [:div {:style "position: absolute; top: 50%; left: 65%; width: 25%;"}
       [:div "\"Perfection is achieved not when there is nothing more to add, 
              but rather when there is nothing more to take away.\""]
       [:div [:i "– Antoine de Saint-Exupery"]]]]]))

;; Page renderers
(defn render-home-page []
  (base-html-template "Building Programs"
                      [:div 
                       (home-content)]))

(defn render-about-page [content-hiccup]
  (base-html-template "About - Building Programs"
                      [:div
                       title-panel-component
                       [:div.article-inner
                        [:div {:style "display: grid; grid-template-columns: auto 1fr; gap: 32px;"}
                         [:div [:img {:src (config/image-url "Robert_Brotherus_portrait.avif")
                                      :style "width: 400px; border-radius: 30%;"}]]
                         [:div {:style "max-width: 600px; justify-self: start; align-self: start;"}
                          content-hiccup]]]]))

(defn render-article-page [article hiccup-content]
  (let [{:keys [tags date name]} article
        mins (js/Math.round (/ (count (str hiccup-content)) 2000))]
    (base-html-template
     (str name " - Building Programs")
     [:div 
      [:div.article-container
       [:div.article-inner
        [:div.article
         [:div {:style "display: flex; align-items: center;"}
          [:div (robert-small-pic)]
          [:div.small.margin (str "Robert J. Brotherus  •  " date "  •  " mins " min read")]]
         [:div.article-content hiccup-content]
         [:div.small
          (interpose " • " (map (fn [tag] [:a {:href (str "/posts/" tag)} tag]) tags))]
         [:hr]]
        (articles-list db/articles)]]])))

(defn render-posts-page [tag articles]
  (base-html-template
   (str tag " Posts - Building Programs")
   [:div
    title-panel-component
    (articles-list articles)]))

(defn render-404-page  []
  (base-html-template
   "Page Not Found - Building Programs"
   [:div
    [:div.article-inner
     [:h1 "Page Not Found"]
     [:p "The page you're looking for doesn't exist."]
     [:p [:a {:href "/"} "Return to home page"]]]]))

(defn render-error-page [error]
  (base-html-template
   "Error - Building Programs"
   [:div
    [:div.article-inner
     [:h1 "An Error Occurred"]
     [:p "Sorry, something went wrong while processing your request."]
     [:p [:a {:href "/"} "Return to home page"]]]]))
