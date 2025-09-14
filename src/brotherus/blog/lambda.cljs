(ns brotherus.blog.lambda
  (:require
    [brotherus.blog.article :as article]
    [brotherus.blog.filters :as filters]
    [brotherus.blog.github-api :as github]
    [brotherus.blog.server-render :as render :refer [hiccup-to-html]]
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
  (-> (github/get-cached-articles)
      (.then (fn [articles]
               (render/render-home-page articles)))))

(defn handle-about []
  (-> (fetch-article-content "about/article.md")
      (.then (fn [markdown]
               (let [hiccup-content (article/markdown-to-hiccup markdown {:item-id "about"})]
                 (render/render-about-page hiccup-content))))))

(defn serve-article [article-info id articles]
  (let [url (or (:url article-info) (str id "/article.md"))]
    (-> (increment-view-counter id)
        (.then (fn [new-count]
                 (-> (fetch-article-content url)
                     (.then (fn [markdown]
                              (let [hiccup-content (article/markdown-to-hiccup markdown {:item-id id})]
                                (render/render-article-page (assoc article-info :views new-count)
                                                            hiccup-content articles))))))))))

(defn redirect-to-short-id [short-id]
  (js/Promise.resolve
    {:statusCode 301
     :headers {"Location" (str "/post/" short-id)
               "Content-Type" "text/html; charset=utf-8"}
     :body (str "<!DOCTYPE html><html><head><title>Redirecting...</title></head>"
                "<body><p>Redirecting to <a href=\"/post/" short-id "\">/post/" short-id "</a></p>"
                "<script>window.location.href='/post/" short-id "';</script></body></html>")}))

(defn handle-post [id]
  ;; First fetch articles from GitHub API, then handle the request
  (-> (github/get-cached-articles)
      (.then (fn [articles]
               (let [articles-index (github/create-articles-index articles)
                     articles-index2 (github/create-articles-index2 articles)]
                 (if-let [article-info (get articles-index id)]
                   ;; Found by short ID - serve the article
                   (serve-article article-info id articles)
                   ;; Not found by short ID - check if it's a long ID
                   (if-let [article-info (get articles-index2 id)]
                     ;; Found by long ID - redirect to short ID
                     (redirect-to-short-id (:id article-info))
                     ;; Not found in either index - 404
                     (js/Promise.resolve (render/render-404-page)))))))))

(defn handle-posts [tag]
  (-> (github/get-cached-articles)
      (.then (fn [articles]
               (let [filtered-articles (filters/filter-articles articles {:tag tag})]
                 (render/render-posts-page tag filtered-articles))))))

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
        (.then (fn [response]
                 (let [elapsed-time (- (.now js/Date) start-time)]
                   ;; Check if response is already a complete HTTP response (e.g., redirect)
                   (if (and (map? response) (:statusCode response))
                     ;; Already a complete response - pass it through
                     (callback nil (clj->js response))
                     ;; Hiccup response - convert to HTML
                     (callback nil (clj->js {:statusCode 200
                                             :headers {"Content-Type" "text/html; charset=utf-8"
                                                       "Server-Timing" (str "t;dur=" elapsed-time)}
                                             :body (hiccup-to-html response)}))))))
        (.catch (fn [error]
                  (js/console.error "Error processing request:" error)
                  (callback nil (clj->js {:statusCode 500
                                          :headers {"Content-Type" "text/html; charset=utf-8"}
                                          :body (hiccup-to-html (render/render-error-page error))})))))))

;; Export for Node.js
(set! js/exports.handler handler)
