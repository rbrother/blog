(ns brotherus.blog.core
  (:require
    [accountant.core :as accountant]
    [re-frame.core :as rf]
    [re-frame.db]
    [reagent-dev-tools.core :as dev-tools]
    [medley.core :refer [find-first]]
    [reagent.dom :as rdom]
    [brotherus.blog.config :as config]
    [brotherus.blog.db :as db]
    [brotherus.blog.styles]
    [brotherus.blog.info]
    [brotherus.blog.home :as home] ;; Needed for global styles
    ))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")
    (dev-tools/start! {:state-atom re-frame.db/app-db})))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [home/main-panel] root-el)))

(def routes
  [{:regex #"/about", :dispatch [:brotherus.blog.info/show-info]}
   {:regex #"/post/(.+)", :dispatch [:brotherus.blog.item-page/select-item]}
   {:regex #"/posts/(.+)", :dispatch [:brotherus.blog.filters/select-items]}
   {:regex #".*", :dispatch [::home/home]} ;; Default route, match anything else
   ])

(defn dispatch-route! [{:keys [matches dispatch]}]
  (let [params (->> matches rest (map js/decodeURIComponent))]
    (rf/dispatch (vec (concat dispatch params)))))

(defn setup-routes []
  (accountant/configure-navigation!
    {:nav-handler
     (fn [raw-path]
       (js/window.scrollTo 0 0)
       ;; Filter away query parameters from the path, Facebook etc sometimes insert those
       (let [path (re-find #"^[^\?]+" raw-path)]
         (->> routes
              (map (fn [{:keys [regex] :as route}]
                     (assoc route :matches (re-matches regex path))))
              (find-first :matches)
              dispatch-route!)))
     :path-exists? (fn [_path] true) ;; For absolute external links, this will not be called
     })
  (accountant/dispatch-current!))

(defn init []
  (setup-routes)
  (rf/dispatch-sync [::db/initialize-db])
  (dev-setup)
  (mount-root))
