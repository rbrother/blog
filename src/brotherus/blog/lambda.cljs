(ns brotherus.blog.lambda
  (:require
    [cljs.nodejs :as nodejs]
    [brotherus.blog.db :as db]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.server-render :as render]
    [clojure.string :as str]))

;; Node.js imports
(def marked (nodejs/require "marked"))
(def fetch (nodejs/require "node-fetch"))

;; Helper functions for marked
(defn parse-markdown [content]
  (.parse marked content))

;; URL parsing utilities
(defn parse-path
  "Parse the request path and extract route information"
  [path]
  (cond
    (= path "/") {:route :home}
    (= path "/about") {:route :about}
    (= path "/posts") {:route :posts :tag nil}
    (str/starts-with? path "/post/") {:route :post :id (subs path 6)}
    (str/starts-with? path "/posts/") {:route :posts :tag (subs path 7)}
    :else {:route :not-found}))

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
(defn handle-home
  "Handle the home page route"
  [_event _context]
  (js/Promise.resolve
    {:statusCode 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (render/render-home-page)}))

(defn handle-about
  "Handle the about page route"
  [_event _context]
  (js/Promise.resolve
    {:statusCode 200
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (render/render-about-page)}))

(defn handle-post
  "Handle individual post route"
  [_event _context id]
  (if-let [article (get db/articles-index id)]
    (let [url (or (:url article) (str id "/article.md"))]
      (-> (fetch-article-content url)
          (.then (fn [content]
                   (let [html-content (parse-markdown content)]
                     {:statusCode 200
                      :headers {"Content-Type" "text/html; charset=utf-8"}
                      :body (render/render-article-page article html-content)})))))
    (js/Promise.resolve
      {:statusCode 404
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (render/render-404-page)})))

(defn handle-posts
  "Handle posts filtered by tag"
  [_event _context tag]
  (let [filtered-articles (filters/filter-articles db/articles tag)]
    (js/Promise.resolve
      {:statusCode 200
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :body (render/render-posts-page tag filtered-articles)})))

(defn handle-not-found
  "Handle 404 not found"
  [_event _context]
  (js/Promise.resolve
    {:statusCode 404
     :headers {"Content-Type" "text/html; charset=utf-8"}
     :body (render/render-404-page)}))

;; Main Lambda handler
(defn handler
  "Main AWS Lambda handler function"
  [event context callback]
  (let [path (or (.-path event) "/")
        route-info (parse-path path)]

    (js/console.log "Processing request for path:" path)
    (js/console.log "Route info:" (pr-str route-info))

    (-> (case (:route route-info)
          :home (handle-home event context)
          :about (handle-about event context)
          :post (handle-post event context (:id route-info))
          :posts (handle-posts event context (:tag route-info))
          :not-found (handle-not-found event context))
        (.then (fn [response]
                 (js/console.log "Response status:" (:statusCode response))
                 (callback nil (clj->js response))))
        (.catch (fn [error]
                  (js/console.error "Error processing request:" error)
                  (callback nil (clj->js {:statusCode 500
                                          :headers {"Content-Type" "text/html; charset=utf-8"}
                                          :body (render/render-error-page error)})))))))

;; Export for Node.js
(set! js/exports.handler handler)
