(ns brotherus.blog.server-render
  (:require
    [brotherus.blog.db :as db]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.styles :as styles]
    [garden.core :refer [css]]
    [clojure.string :as str]))

;; CSS generation
(defn generate-css []  
  (css styles/defaults))

;; HTML template utilities
(defn html-escape [text] 
  (-> text
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")
      (str/replace "\"" "&quot;")
      (str/replace "'" "&#39;")))

(defn base-html-template [title content]
  (str "<!DOCTYPE html>
<html lang=\"en\">
  <head>
    <meta charset='utf-8'>
    <meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">
    <style>" (generate-css) "</style>
    <link rel=\"preconnect\" href=\"https://fonts.googleapis.com\">
    <link rel=\"preconnect\" href=\"https://fonts.gstatic.com\" crossorigin>
    <link href=\"https://fonts.googleapis.com/css2?family=Libre+Baskerville:ital,wght@0,400;0,700;1,400&display=swap\" rel=\"stylesheet\">
    <link href=\"https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,100;0,300;0,400;0,500;0,700;0,900;1,100;1,300;1,400;1,500;1,700;1,900&display=swap\" rel=\"stylesheet\">
    <title>" title "</title>
  </head>
  <body>
    <div id=\"app\" class=\"top-level\">
      " content "
    </div>
  </body>
</html>"))

(defn header-component []
  "<div class=\"header\">
    <div><a href=\"/\">Building Programs Blog</a></div>
    <div class=\"justify-end\"><a href=\"/about\">About</a></div>
  </div>")

(defn title-panel-component []
  "<div style=\"width: 100%; position: relative; display: inline-block;\">
    <img src=\"/images/background_tech_face.jpg\" style=\"width: 100%; height: auto;\">
    <div style=\"position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: black; padding: 16px 64px 16px 64px; text-align: center;\">
      <h1 style=\"color: #FFF;\">Building Programs</h1>
      <p style=\"color: #888; font-family: Roboto;\">A Blog about Love for Creating Software</p>
    </div>
  </div>")

(defn robert-small-pic []
  "<img src=\"/images/robert.jpg\" style=\"width: 50px; border-radius: 50%;\">")

(defn article-box [article]
  (let [{:keys [name id thumbnail date]} article
        thumb-url (or thumbnail (str "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/" id "/thumbnail.jpg"))]
    (str "<div class=\"article-box\">
      <a href=\"/post/" id "\">
        <div class=\"crop-container\">
          <img class=\"cropped-image\" src=\"" thumb-url "\">
        </div>
        <div class=\"margin2\">" (html-escape name) "</div>
        <div class=\"grid margin2\" style=\"grid-template-columns: auto 1fr; align-items: center;\">
          <div>" (robert-small-pic) "</div>
          <div class=\"small\">
            <div>Robert J. Brotherus</div>
            <div>" date "</div>
          </div>
        </div>
      </a>
    </div>")))

(defn articles-list [articles]
  (str "<div class=\"article-inner\">
    <div class=\"product-table\">"
       (str/join "" (map article-box articles))
       "</div>
  </div>"))

(defn home-content []
  (let [filtered-articles (filters/filter-articles db/articles "Computers")]
    (str (title-panel-component)
         (articles-list filtered-articles)
         "<div class=\"roboto-light\" style=\"width: 100%; position: relative; display: inline-block;\">
           <img src=\"/images/staircases_background.jpg\" style=\"width: 100%; height: auto;\">
           <div style=\"position: absolute; top: 8%; left: 17%; width: 25%;\">
             <div>\"In some ways, programming is like painting. You start with a blank canvas
             and certain basic raw materials. You use a combination of science, art, and
             craft to determine what to do with them.\"</div>
             <div><i>- Andrew Hunt</i></div>
           </div>
           <div style=\"position: absolute; top: 50%; left: 65%; width: 25%;\">
             <div>\"Perfection is achieved not when there is nothing more to add,
                   but rather when there is nothing more to take away.\"</div>
             <div><i>– Antoine de Saint-Exupery</i></div>
           </div>
         </div>")))

;; Page renderers
(defn render-home-page []
  (base-html-template 
   "Building Programs"
   (str (header-component)
        (home-content))))

(defn render-about-page []
  (base-html-template
   "About - Building Programs"
   (str (header-component)
        (title-panel-component)
        "<div class=\"article-inner\">
           <div style=\"display: grid; grid-template-columns: auto 1fr; gap: 32px;\">
             <div><img src=\"/images/Robert_Brotherus_portrait.avif\" style=\"width: 400px; border-radius: 30%;\"></div>
             <div style=\"max-width: 600px; justify-self: start; align-self: start;\">
               <h2>About Robert J. Brotherus</h2>
               <p>Welcome to my blog about programming and software development...</p>
             </div>
           </div>
         </div>")))

(defn render-article-page [article html-content]
  (let [{:keys [tags date name]} article
        mins (js/Math.round (/ (count html-content) 2000))]
    (base-html-template
     (str name " - Building Programs")
     (str (header-component)
          "<div class=\"article-container\">
             <div class=\"article-inner\">
               <div class=\"article\">
                 <div style=\"display: flex; align-items: center;\">
                   <div>" (robert-small-pic) "</div>
                   <div class=\"small margin\">Robert J. Brotherus  •  " date "  •  " mins " min read</div>
                 </div>
                 <div>" html-content "</div>
                 <div class=\"small\">" 
          (str/join " • " (map #(str "<a href=\"/posts/" % "\">" % "</a>") tags))
          "</div>
                 <hr>
               </div>
               " (articles-list db/articles) "
             </div>
           </div>"))))

(defn render-posts-page [tag articles]
  (base-html-template
   (str tag " Posts - Building Programs")
   (str (header-component)
        (title-panel-component)
        (articles-list articles))))

(defn render-404-page []
  (base-html-template
   "Page Not Found - Building Programs"
   (str (header-component)
        "<div class=\"article-inner\">
           <h1>Page Not Found</h1>
           <p>The page you're looking for doesn't exist.</p>
           <p><a href=\"/\">Return to home page</a></p>
         </div>")))

(defn render-error-page [error]
  (base-html-template
   "Error - Building Programs"
   (str (header-component)
        "<div class=\"article-inner\">
           <h1>An Error Occurred</h1>
           <p>Sorry, something went wrong while processing your request.</p>
           <p><a href=\"/\">Return to home page</a></p>
         </div>")))
