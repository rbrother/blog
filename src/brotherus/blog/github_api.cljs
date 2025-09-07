(ns brotherus.blog.github-api
  (:require [cljs.reader :as reader]))

;; GitHub API configuration
(def articles-repo-api "https://api.github.com/repos/rbrother/articles/contents/")
(def articles-raw-base "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main/")

;; Cache for articles to avoid hitting GitHub API repeatedly. This works even between
;; lambda invocations because the lambda container is reused. Re-deploy the lambda to invalidate the cache.
(def articles-cache (atom nil))
(def cache-timestamp (atom 0))
(def cache-ttl (* 5 60 1000)) ; 5 mins in milliseconds

(defn fetch-metadata
  "Fetch metadata.edn for a specific article"
  [article-id]
  (let [metadata-url (str articles-raw-base article-id "/metadata.edn")]
    (-> (js/fetch metadata-url)
        (.then (fn [response]
                 (when (.-ok response) (.text response))))
        (.then reader/read-string)
        (.catch (fn [_error] (js/Promise.resolve nil)) ;; Folders with no metadata.edn should be ignored
                ))))

(defn fetch-articles-list
  "Fetch list of article directories from GitHub API"
  []
  (-> (js/fetch articles-repo-api)
      (.then #(.json %))
      (.then (fn [response]
               (->> (js->clj response :keywordize-keys true)
                    (filter #(= (:type %) "dir"))
                    (map :name))))
      (.catch (fn [error]
                (js/console.error "Failed to fetch articles list:" error)
                []))))

(defn fetch-all-articles
  "Fetch all articles with their metadata"
  []
  (-> (fetch-articles-list)
      (.then (fn [article-ids]
               (js/Promise.all
                (clj->js (->> article-ids
                              (map fetch-metadata))))))
      (.then (fn [articles-array]
               (js/console.log "Fetched articles:" (str articles-array) )
               (->> (js->clj articles-array :keywordize-keys true)
                    (filter identity) ;; Remove folders with no metadata)
                    (sort-by :date #(compare %2 %1)))
               ))))

(defn get-cached-articles
  "Get articles from cache if valid, otherwise fetch from GitHub"
  []
  (let [now (.now js/Date)
        cached @articles-cache
        last-fetch @cache-timestamp]
    (if (and cached (< (- now last-fetch) cache-ttl))
      ;; Return cached data
      (js/Promise.resolve cached)
      ;; Fetch fresh data
      (-> (fetch-all-articles)
          (.then (fn [articles]
                   (reset! articles-cache articles)
                   (reset! cache-timestamp now)
                   articles))))))

(defn create-articles-index
  "Create index by :id for fast lookup"
  [articles]
  (reduce (fn [acc article]
            (assoc acc (:id article) article))
          {} articles))

(defn create-articles-index2
  "Create index by :id-long for backward compatibility"
  [articles]
  (reduce (fn [acc article]
            (if-let [id-long (:id-long article)]
              (assoc acc id-long article)
              acc))
          {} articles))
