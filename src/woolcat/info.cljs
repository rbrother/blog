(ns woolcat.info
  (:require [re-frame.core :as rf]
            [woolcat.db :as db]))

(defn view []
  [:div.col-span-2
   [:div {:style {:display "grid", :grid-template-columns "auto 1fr", :gap "32px"}}
    [:div [:img {:src (str db/img-base "ann.jpg"), :width "200px"}]]
    [:div {:style {:max-width "600px" :justify-self "start" :align-self "start"}}
     [:div.main-title "About Chan Ann Chuang"]
     [:p "Chuang is an architect by training from Beijing, now based in Helsinki. Her journey from architecture to interior design and handicrafts has been one of exploration and rediscovery. After years of contributing as an architect to large-scale projects, Ann realized that her passion lies in creating on a more intimate scale — crafting pieces where she can design and construct every detail with her full control."]
     [:p "After moving to Nordic area, Ann was captivated by the simplicity and elegance of the crafting techniques there. This inspiration, combined with her appreciation for sustainable living, led Ann to work with natural materials like wool, cotton, and other resources that are farmed, renewable and kind to the environment. Every piece she creates is entirely handmade, reflecting her dedication to craftsmanship and rejection of mass production."]
     [:p "Ann's background in architecture continues to shape her approach to design. Her current work is also a continuation of her master research area in parametric architecture and historic city renovation from TU Delft University. At Delft she explored theoretical themes that now find practical expression in her handcrafted items. Ann draws inspiration from some of the world's greatest architectural and art studios, which lead her into a brand new design approach of her own - \"Architect made\" - throughout her studio."]
     [:p "In her creations, Ann strives to harmonize two distinct but complementary worlds: the rich history and culture of ancient China and the timeless functionality and minimalism of Nordic design. Many pieces Ann crafts are exploration of these connections, blending tradition with modernity to create something timeless."]]]])

;; Events

(rf/reg-event-db ::show-info
                 (fn [db _]
                   (assoc db :page :info)))