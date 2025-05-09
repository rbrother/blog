(ns brotherus.blog.item-page
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core]
            ["marked" :as marked]
            [brotherus.blog.db :as db]
            [brotherus.blog.components :as components]))

(defn view []
  (let [item-id @(rf/subscribe [::selected-item])
        content @(rf/subscribe [::article-html])
        {:keys [tags date] :as _item} (get db/articles-index item-id)
        mins (js/Math.round (/ (count content) 2000))]
    (print (count content))
    [:div.article
     [:div {:style {:display "flex" :align-items "center"}}
      [:div components/robert-small-pic]
      [:div.small.margin "Robert J. Brotherus  •  " date "  •  " mins " min read"]]
     (if content
       [:div {:dangerouslySetInnerHTML {:__html content}}]
       [:p "Loading..."])
     (into [:div.small] (interpose " • " (for [tag tags] [:a {:href (str "/items/" tag)} tag])))
     [:hr]

     ]))

;; Subs

(rf/reg-sub ::selected-item (fn [db _] (:selected-item db)))

(rf/reg-sub ::article-content (fn [db _] (:article-content db)))

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
       :http-xhrio {:method :get
                    :uri url
                    :timeout 8000
                    :response-format (ajax.core/text-response-format)
                    :on-success [::set-article-content]
                    :on-failure [::set-article-content-failed]}})))

(rf/reg-event-db ::set-article-content
                 (fn [db [_ content]]
                   (assoc db :article-content content)))

(rf/reg-event-db ::set-article-content-failed
                 (fn [db [_ error]]
                   (assoc db :article-content (str "Failed to load content: " error))))
