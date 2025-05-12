(ns brotherus.blog.item-page
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core]
            ["marked" :as marked]
            [brotherus.blog.db :as db]
            [brotherus.blog.components :as components]
            [brotherus.blog.items-list :as items-list]))

(defn article-html []
  (let [html @(rf/subscribe [::article-html])]
    (if html
      [:div {:dangerouslySetInnerHTML {:__html html}}]
      [:p "Loading..."])))

(defn view []
  (let [item-id @(rf/subscribe [::selected-item])
        {:keys [tags date] :as _item} (get db/articles-index item-id)
        mins @(rf/subscribe [::article-mins])]
    [:div.article-container
     [:div.article-inner
      [:div.article
       [:div {:style {:display "flex" :align-items "center"}}
        [:div components/robert-small-pic]
        [:div.small.margin "Robert J. Brotherus  •  " date "  •  " mins " min read"]]
       [article-html]
       (into [:div.small] (interpose " • " (for [tag tags] [:a {:href (str "/posts/" tag)} tag])))
       [:hr]]
      [items-list/view]]]))

;; Subs

(rf/reg-sub ::selected-item (fn [db _] (:selected-item db)))

(rf/reg-sub ::article-content (fn [db _] (:article-content db)))

(rf/reg-sub ::article-mins :<- [::article-content]
            (fn [md _] (when md (js/Math.round (/ (count md) 2000)))))

(rf/reg-sub ::article-html :<- [::article-content]
            (fn [md _] (when md (marked/parse md))))

;; Events

(rf/reg-event-fx
  ::select-item
  (fn [{:keys [db]} [_ id]]
    (let [{:keys [url]} (get db/articles-index id)]
      {:db (-> db
               (dissoc :page)
               (assoc :selected-item id))
       :dispatch [::load-article (or url (str id "/article.md"))]})))

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
