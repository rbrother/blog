(ns brotherus.blog.lambda
  (:require
   [brotherus.blog.article :as article]
   [brotherus.blog.db :as db]
   [brotherus.blog.filters :as filters]
   [brotherus.blog.server-render :as render :refer [hiccup-to-html]]
   [clojure.string :as str]
   [medley.core :refer [find-first]]))

;; Article fetching
(defn fetch-article-content
  "Fetch article content from GitHub"
  [url]
  (-> (js/fetch (str "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/" url))
      (.then #(.text %))
      (.catch #(do
                 (js/console.error "Failed to fetch article:" %)
                 "Failed to load article content"))))

;; Hit counter functionality
(defn increment-view-counter
  "Increment view counter for an article using counterapi.dev"
  [article-id]
  (let [counter-url (str "https://api.counterapi.dev/v1/building-programs-blog/" article-id "/up")]
    (-> (js/fetch counter-url)
        (.then #(.json %))
        (.then (fn [response] (.-count response)))
        (.catch (fn [error]
                  (js/console.error "Failed to increment view counter for" article-id ":" error)
                  nil)))))

;; Route handlers
(defn handle-home []
  (js/Promise.resolve (render/render-home-page)))

(defn handle-about []
  (-> (fetch-article-content "about/article.md")
      (.then (fn [markdown]
               (let [hiccup-content (article/markdown-to-hiccup markdown {:item-id "about"})]
                 (render/render-about-page hiccup-content))))))

(defn handle-post [id]
  (if-let [article-info (get db/articles-index id)]
    (let [url (or (:url article-info) (str id "/article.md"))]
      ;; Increment view counter asynchronously (don't wait for it)
      (-> (increment-view-counter id)
          (.then (fn [new-count]
                   (-> (fetch-article-content url)
                       (.then (fn [markdown]
                                (let [hiccup-content (article/markdown-to-hiccup markdown {:item-id id})]
                                  (render/render-article-page article-info hiccup-content new-count)))))))))
    (js/Promise.resolve (render/render-404-page))))

(defn handle-posts [tag]
  (let [filtered-articles (filters/filter-articles db/articles tag)]
    (js/Promise.resolve (render/render-posts-page tag filtered-articles))))

(defn handle-not-found []
  (js/Promise.resolve (render/render-404-page)))

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
  (let [start-time (.now js/Date)
        raw-path (or (.-rawPath event) (.-path event) "/")
        path (re-find #"^[^\?]+" raw-path)
        {handler-function :function matches :matches}
        (->> routes
             (map (fn [{:keys [regex] :as route}]
                    (assoc route :matches (re-matches regex path))))
             (find-first :matches))
        params (->> matches rest (map js/decodeURIComponent))]
    (.log js/console "handler" raw-path path)
    (-> (apply handler-function params)
        (.then (fn [response-hiccup] 
                 (let [elapsed-time (- (.now js/Date) start-time)]
                   (callback nil (clj->js {:statusCode 200
                                           :headers {"Content-Type" "text/html; charset=utf-8"
                                                     "Server-Timing" (str "t;dur=" elapsed-time)}
                                           :body (hiccup-to-html response-hiccup)})))))
        (.catch (fn [error]
                  (js/console.error "Error processing request:" error)
                  (callback nil (clj->js {:statusCode 500
                                          :headers {"Content-Type" "text/html; charset=utf-8"}
                                          :body (hiccup-to-html (render/render-error-page error))})))))))

;; Export for Node.js
(set! js/exports.handler handler)
