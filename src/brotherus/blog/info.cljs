(ns brotherus.blog.info
  (:require [re-frame.core :as rf]))

(defn view []
  [:div.col-span-2
   [:div {:style {:display "grid", :grid-template-columns "auto 1fr", :gap "32px"}}
    [:div [:img {:src "images/Robert_Brotherus_portrait.avif"}]]
    [:div {:style {:max-width "600px" :justify-self "start" :align-self "start"}}
     [:p "In the 1993 movie " [:i "Philadelphia" ] ", Tom Hanks portrays a homosexual lawyer Andrew Beckett
         in the 1980's Philadelphia, inspired by the true story of lives of attorneys Geoffrey Bowers and Clarence Cain.
         Beckett is doing valuable work for a law-firm,
         but fired when his managers learn he is gay after he develops first signs of AIDS."]
     [:p "Beckett goes on to sue his ex-employer for discrimination and the main debate in
         the court-room is whether Beckett was a good lawyer (and therefore fired for illegal reasons)
         or a bad lawyer (therefore rightfully fired for his incompetence).
         Pivotal moment is when Becketts defence attorney asks him in court why is he a good lawyer.
         Beckett answers: \" " [:i "Because I know the law and I love the law."] " \" "]
     [:p "To be good in something is to know it - to have competence - and to love it - to have passion.
         Passion and competence create a positive spiral of achievement that leads to further passion,
         learning and ever higher competence."]
     [:p "Software development, like practice of law, is vast and ever-changing area where no-one can
         be expert of everything. In software development life-long continuous learning is a given.
         Those of us who love that learning, that kick that comes from understanding a new technology
         that is ever more powerful or robust than previous ones, can say:
         " [:i "I know software development and I love software development."] ]
     [:p [:i "Robert J. Brotherus"]]
     ]]])

;; Events

(rf/reg-event-db ::show-info
                 (fn [db _]
                   (assoc db :page :info)))