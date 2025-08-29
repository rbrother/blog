(ns brotherus.blog.server-render
  (:require-macros [hiccups.core :as hiccups :refer [html]])
  (:require
    [brotherus.blog.db :as db]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.styles :as styles]
    [garden.core :refer [css]]
    [hiccups.runtime :as hiccupsrt]
    [clojure.string :as str]))

;; CSS generation
(defn generate-css []  
  (css styles/defaults))

;; HTML conversion utilities
(defn raw-html-marker
  "Create a marker for raw HTML content"
  [content]
  (str "<!--RAW_HTML_START-->" content "<!--RAW_HTML_END-->"))

(defn hiccup-to-html
  "Convert Hiccup data structure to HTML string, handling raw HTML content"
  [hiccup-data]
  (let [html-str (str "<!DOCTYPE html>\n" (html hiccup-data))]
    ;; Post-process to handle raw HTML content markers
    (str/replace html-str
                 #"&lt;!--RAW_HTML_START--&gt;(.*?)&lt;!--RAW_HTML_END--&gt;"
                 (fn [[_ content]]
                   ;; Decode HTML entities in the content
                   (-> content
                       (str/replace "&lt;" "<")
                       (str/replace "&gt;" ">")
                       (str/replace "&amp;" "&")
                       (str/replace "&quot;" "\""))))))

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
    [:div#app.top-level content]]])

(defn header-component
  "Header component - returns Hiccup"
  []
  [:div.header
   [:div [:a {:href "/"} "Building Programs Blog"]]
   [:div.justify-end [:a {:href "/about"} "About"]]])

(defn title-panel-component
  "Title panel component - returns Hiccup"
  []
  [:div {:style "width: 100%; position: relative; display: inline-block;"}
   [:img {:src "/images/background_tech_face.jpg" :style "width: 100%; height: auto;"}]
   [:div {:style "position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: black; padding: 16px 64px 16px 64px; text-align: center;"}
    [:h1 {:style "color: #FFF;"} "Building Programs"]
    [:p {:style "color: #888; font-family: Roboto;"} "A Blog about Love for Creating Software"]]])

(defn robert-small-pic
  "Robert's small profile picture - returns Hiccup"
  []
  [:img {:src "/images/robert.jpg" :style "width: 50px; border-radius: 50%;"}])

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

(defn articles-list
  "Articles list component - returns Hiccup"
  [articles]
  [:div.article-inner
   [:div.product-table
    (map article-box articles)]])

(defn home-content
  "Home page content - returns Hiccup"
  []
  (let [filtered-articles (filters/filter-articles db/articles "Computers")]
    [:div
     (title-panel-component)
     (articles-list filtered-articles)
     [:div.roboto-light {:style "width: 100%; position: relative; display: inline-block;"}
      [:img {:src "/images/staircases_background.jpg" :style "width: 100%; height: auto;"}]
      [:div {:style "position: absolute; top: 8%; left: 17%; width: 25%;"}
       [:div "\"In some ways, programming is like painting. You start with a blank canvas and certain basic raw materials. You use a combination of science, art, and craft to determine what to do with them.\""]
       [:div [:i "- Andrew Hunt"]]]
      [:div {:style "position: absolute; top: 50%; left: 65%; width: 25%;"}
       [:div "\"Perfection is achieved not when there is nothing more to add, but rather when there is nothing more to take away.\""]
       [:div [:i "– Antoine de Saint-Exupery"]]]]]))

;; Page renderers
(defn render-home-page
  "Render the home page - returns HTML string"
  []
  (base-html-template
   "Building Programs"
   [:div
    (header-component)
    (home-content)]))

(defn render-about-page
  "Render the about page - returns HTML string"
  []
  (base-html-template
   "About - Building Programs"
   [:div
    (header-component)
    (title-panel-component)
    [:div.article-inner
     [:div {:style "display: grid; grid-template-columns: auto 1fr; gap: 32px;"}
      [:div [:img {:src "/images/Robert_Brotherus_portrait.avif"
                   :style "width: 400px; border-radius: 30%;"}]]
      [:div {:style "max-width: 600px; justify-self: start; align-self: start;"}
       [:h2 "About Robert J. Brotherus"]
       [:p "Welcome to my blog about programming and software development..."]]]]]))

(defn render-article-page
  "Render an individual article page - returns HTML string"
  [article html-content]
  (let [{:keys [tags date name]} article
        mins (js/Math.round (/ (count html-content) 2000))]
    (base-html-template
     (str name " - Building Programs")
     [:div
      (header-component)
      [:div.article-container
       [:div.article-inner
        [:div.article
         [:div {:style "display: flex; align-items: center;"}
          [:div (robert-small-pic)]
          [:div.small.margin (str "Robert J. Brotherus  •  " date "  •  " mins " min read")]]
         [:div.article-content (raw-html-marker html-content)]
         [:div.small
          (interpose " • " (map (fn [tag] [:a {:href (str "/posts/" tag)} tag]) tags))]
         [:hr]]
        (articles-list db/articles)]]])))

(defn render-posts-page
  "Render posts page filtered by tag - returns HTML string"
  [tag articles]
  (base-html-template
   (str tag " Posts - Building Programs")
   [:div
    (header-component)
    (title-panel-component)
    (articles-list articles)]))

(defn render-404-page
  "Render 404 error page - returns HTML string"
  []
  (base-html-template
   "Page Not Found - Building Programs"
   [:div
    (header-component)
    [:div.article-inner
     [:h1 "Page Not Found"]
     [:p "The page you're looking for doesn't exist."]
     [:p [:a {:href "/"} "Return to home page"]]]]))

(defn render-error-page
  "Render error page - returns HTML string"
  [error]
  (base-html-template
   "Error - Building Programs"
   [:div
    (header-component)
    [:div.article-inner
     [:h1 "An Error Occurred"]
     [:p "Sorry, something went wrong while processing your request."]
     [:p [:a {:href "/"} "Return to home page"]]]]))
