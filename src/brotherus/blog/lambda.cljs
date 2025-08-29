(ns brotherus.blog.lambda
  (:require
    [cljs.nodejs :as nodejs]
    [medley.core :refer [find-first]]
    [brotherus.blog.db :as db]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.server-render :as render :refer [hiccup-to-html]]
    [clojure.string :as str]))

;; Node.js imports
(def marked (nodejs/require "marked"))
(def fetch (nodejs/require "node-fetch"))

;; Helper functions for marked
(defn parse-markdown [content]
  (.parse marked content))

;; Path normalization for API Gateway
(defn normalize-path
  "Remove stage prefix from API Gateway path (e.g., '/prod/about' -> '/about')"
  [path]
  (if (and path (str/starts-with? path "/"))
    (let [segments (str/split path #"/")
          filtered-segments (filter seq segments)
          stage-patterns #{"prod" "dev" "test" "staging" "stage"}
          cleaned-segments (if (and (seq filtered-segments)
                                   (contains? stage-patterns (first filtered-segments)))
                            (rest filtered-segments)
                            filtered-segments)]
      (if (empty? cleaned-segments)
        "/"
        (str "/" (str/join "/" cleaned-segments))))
    "/"))

;; Article fetching
(defn fetch-article-content
  "Fetch article content from GitHub"
  [url]
  (-> (fetch (str "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/" url))
      (.then #(.text %))
      (.catch #(do
                 (js/console.error "Failed to fetch article:" %)
                 "Failed to load article content"))))

;; Route handlers
(defn handle-home []
  (js/Promise.resolve
    {:statusCode 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (hiccup-to-html (render/render-home-page))}))

(defn handle-about []
  (js/Promise.resolve
    {:statusCode 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (hiccup-to-html (render/render-about-page))}))

(defn handle-post [id]
  (if-let [article (get db/articles-index id)]
    (let [url (or (:url article) (str id "/article.md"))]
      (-> (fetch-article-content url)
          (.then (fn [content]
                   (let [html-content (parse-markdown content)]
                     {:statusCode 200
                      :headers {"Content-Type" "text/html; charset=utf-8"}
                      :body (hiccup-to-html (render/render-article-page article html-content))})))))
    (js/Promise.resolve
      {:statusCode 404
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (hiccup-to-html (render/render-404-page))})))

(defn handle-posts [tag]
  (let [filtered-articles (filters/filter-articles db/articles tag)]
    (js/Promise.resolve
      {:statusCode 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (hiccup-to-html (render/render-posts-page tag filtered-articles))})))

(defn handle-not-found []
  (js/Promise.resolve
    {:statusCode 404
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (hiccup-to-html (render/render-404-page))}))

(def routes
  [{:regex #"(?:/(?:prod|dev|qa))?/?", :function handle-home}
   {:regex #".*/about/?", :function handle-about}
   {:regex #".*/post/(.+)", :function handle-post}
   {:regex #".*/posts(?:/(.+))?", :function handle-posts}
   {:regex #".*", :function handle-not-found} ;; Default route, match anything else
   ])

;; Main Lambda handler
(defn handler [event context callback]
  ;; API Gateway v2 (HTTP API) uses rawPath, v1 (REST API) uses path
  (let [raw-path (or (.-rawPath event) (.-path event) "/")
        path (normalize-path (re-find #"^[^\?]+" raw-path))
        {handler-function :function matches :matches}
        (->> routes
             (map (fn [{:keys [regex] :as route}]
                    (assoc route :matches (re-matches regex path))))
             (find-first :matches))
        params (->> matches rest (map js/decodeURIComponent))]
    (.log js/console "handler" raw-path path)
    (-> (apply handler-function params)
        (.then (fn [response]
                 (js/console.log "Response status:" (:statusCode response))
                 (callback nil (clj->js response))))
        (.catch (fn [error]
                  (js/console.error "Error processing request:" error)
                  (callback nil (clj->js {:statusCode 500
                                          :headers {"Content-Type" "text/html; charset=utf-8"}
                                          :body (hiccup-to-html (render/render-error-page error))})))))))

;; Export for Node.js
(set! js/exports.handler handler)
