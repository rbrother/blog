(ns brotherus.blog.lambda
  (:require
   [brotherus.blog.article :as article]
   [brotherus.blog.db :as db]
   [brotherus.blog.filters :as filters]
   [brotherus.blog.server-render :as render :refer [hiccup-to-html]]
   [cljs.nodejs :as nodejs]
   [clojure.string :as str]
   [medley.core :refer [find-first]]))

;; Node.js imports
(def fetch (nodejs/require "node-fetch"))

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
  (if-let [article-info (get db/articles-index id)]
    (let [url (or (:url article-info) (str id "/article.md"))]
      (-> (fetch-article-content url)
          (.then (fn [markdown]
                   (let [hiccup-content (article/markdown-to-hiccup markdown {:item-id id})]
                     {:statusCode 200
                      :headers {"Content-Type" "text/html; charset=utf-8"}
                      :body (hiccup-to-html (render/render-article-page article-info hiccup-content))})))))
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
        path (re-find #"^[^\?]+" raw-path)
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
