(ns brotherus.blog.item-page
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [day8.re-frame.http-fx]
            [ajax.core]
            [accountant.core :as accountant]
            [brotherus.blog.config :as config]
            [brotherus.blog.db :as db]
            [brotherus.blog.components :as components]
            [brotherus.blog.items-list :as items-list]
            [brotherus.blog.article :as article]))

(defn article-html []
  (let [html @(rf/subscribe [::article-html])]
    (or html [:p "Loading..."])))

(defn view []
  (let [item-id @(rf/subscribe [::selected-item])
        {:keys [tags date] :as _item} (get db/articles-index item-id)
        mins @(rf/subscribe [::article-mins])
        views @(rf/subscribe [::views])]
    [:div.article-container
     [:div.article-inner
      [:div.article
       [:div {:style {:display "flex" :align-items "center"}}
        [:div components/robert-small-pic]
        [:div.small.margin "Robert J. Brotherus  •  " date "  •  " mins " min read"
         "  •  " views " views"]]
       [article-html]
       (into [:div.small] (interpose " • " (for [tag tags] [:a {:href (str "/posts/" tag)} tag])))
       [:hr]]
      [items-list/view]]]))

;; Subs

(rf/reg-sub ::selected-item (fn [db _] (:selected-item db)))

(rf/reg-sub ::article-content (fn [db _] (:article-content db)))

(rf/reg-sub ::article-mins :<- [::article-content]
            (fn [md _] (when md (js/Math.round (/ (count md) 2000)))))

(rf/reg-sub ::article-html
            :<- [::article-content]
            :<- [::selected-item]
            (fn [[markdown item-id] _]
              (article/markdown-to-hiccup markdown {:item-id item-id})))

(rf/reg-sub ::views (fn [db _] (:views db)))

;; Events

(rf/reg-event-fx
  ::select-item
  (fn [{:keys [db]} [_ id-raw]]
    (if-let [info (get db/articles-index id-raw)]
      ;; Happy case: id-raw is a valid article short id
      {:db (-> db
               (dissoc :page :error)
               (assoc :selected-item id-raw)
               (assoc :views (:views info)))
       :dispatch [::load-article (str id-raw "/article.md")]
       :fx [(when (not config/localhost?) ;; Up counters only in production
              [:http-xhrio {:method :get
                            :uri (str "https://api.counterapi.dev/v1/building-programs-blog/" id-raw "/up")
                            :timeout 8000
                            :response-format (ajax.core/json-response-format {:keywords? true})
                            :on-success [::counter-upped]}])]}
      (if-let [id (get-in db/articles-index2 [id-raw :id])]
        ;; Found old long id, redirect to new short id
        (do (accountant/navigate! (str "/post/" id))
            {:db db})
        {:db (assoc db :error (str "Article not found: " id-raw))}))))

(rf/reg-event-fx
  ::counter-upped
  (fn [{{:keys [selected-item] :as db} :db} [_ response]]
    (let [{orig-views :views} (get db/articles-index selected-item)
          views (:count response)
          new-views (max (inc orig-views) views)]
      {:db (assoc db :views new-views)
       :fx [(when (> orig-views views)
              [:http-xhrio {:method :get
                            :uri (str "https://api.counterapi.dev/v1/building-programs-blog/" selected-item
                                      "/set?count=" new-views)
                            :timeout 8000
                            :response-format (ajax.core/json-response-format {:keywords? true})
                            :on-success [::counter-reset]
                            :on-failure [::counter-reset-fail]}])]})))

(rf/reg-event-db
  ::counter-reset
  (fn [db [_ _response]]
    (print "::counter-reset")
    db))

(rf/reg-event-db
  ::counter-reset-fail
  (fn [db [_ _response]]
    (print "::counter-reset-fail")
    db))

(rf/reg-event-fx
  ::load-article
  (fn [{:keys [db]} [_ url]]
    {:db db
     :http-xhrio {:method :get
                  :uri (str components/articles-base-url url)
                  :timeout 8000
                  :response-format (ajax.core/text-response-format)
                  :on-success [::set-article-content]
                  :on-failure [::set-article-content-failed]}}))

(rf/reg-event-db ::set-article-content
                 (fn [db [_ content]]
                   (assoc db :article-content content)))

(rf/reg-event-db ::set-article-content-failed
                 (fn [db [_ error]]
                   (assoc db :article-content (str "Failed to load content: " error))))
