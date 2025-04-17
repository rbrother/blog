(ns woolcat.core
  (:require
    [accountant.core :as accountant]
    [re-frame.core :as rf]
    [re-frame.db]
    [reagent-dev-tools.core :as dev-tools]
    [medley.core :refer [find-first]]
    [reagent.dom :as rdom]
    [woolcat.config :as config]
    [woolcat.db :as db]
    [woolcat.styles]
    [woolcat.info]
    [woolcat.home :as home] ;; Needed for global styles
    ))

(defn dev-setup []
  (when config/debug?
    (do
      (println "dev mode")
      (dev-tools/start! {:state-atom re-frame.db/app-db}))))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [home/main-panel] root-el)))

(def routes
  [{:regex #"/about", :dispatch [:woolcat.info/show-info]}
   {:regex #"/item/(.+)", :dispatch [:woolcat.item-page/select-item]}
   {:regex #"/items/(.+)", :dispatch [:woolcat.filters/select-items]}
   {:regex #".*", :dispatch [::home/home]} ;; Default route, match anything else
   ])

(defn dispatch-route! [{:keys [matches dispatch]}]
  (let [params (->> matches rest (map js/decodeURIComponent))]
    (rf/dispatch (vec (concat dispatch params)))))

(defn setup-routes []
  (accountant/configure-navigation!
    {:nav-handler (fn [path]
                    (print [:nav-handler path])
                    (js/window.scrollTo 0 0)
                    (->> routes
                         (map (fn [{:keys [regex] :as route}]
                                (assoc route :matches (re-matches regex path))))
                         (find-first :matches)
                         dispatch-route!))
     :path-exists? (fn [_path] true)})
  (accountant/dispatch-current!))

(defn init []
  (setup-routes)
  (rf/dispatch-sync [::db/initialize-db])
  (dev-setup)
  (mount-root))
