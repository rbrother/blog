(ns woolcat.db
  (:require [medley.core :refer [index-by]]
            [re-frame.core :as rf]))

;; All pics in dropbox
;; https://www.dropbox.com/scl/fo/k92lhu64t31v0yhg99kli/ALTQYZFlBq8hA8jbh5mciXM?rlkey=u9p2d55n1hm3rvxemtzkx36cc&e=1&dl=0

(def img-base "https://rjb-share.s3.eu-north-1.amazonaws.com/woolcat-media/")

(defn amend-product [{:keys [id detail-pics folder] :as info}]
  (assoc info :photo {:file (if folder (str img-base "products/" id "/main.jpg")
                                       (str img-base "products/" id ".jpg"))}
              :detail-photos (if (number? detail-pics)
                               (->> (range 1 (inc detail-pics))
                                    (map (fn [index]
                                           {:file (str img-base "products/" id "/detail" index ".jpg")})))
                               (->> detail-pics
                                    (map (fn [{:keys [name] :as pic}]
                                           (assoc pic :file (str img-base "products/" id "/" name))))))))

(def tags
  [{:name "Fiber", :photo (str img-base "fiber.jpg")}
   {:name "Yarn", :photo (str img-base "yarn.jpg")}
   {:name "Fabric", :photo (str img-base "textile.jpg")}
   {:name "Leather & Fur", :photo (str img-base "skin.jpg")}
   {:name "Paper", :photo (str img-base "paper.jpg")}
   {:name "Felt", :photo (str img-base "Felt.jpg")}
   {:name "Knit & Crochet", :photo (str img-base "Knit-Crochet.jpg")}
   {:name "Weave & PomPom", :photo (str img-base "Weave-PomPom.jpg")}
   {:name "Haute Couture", :photo (str img-base "Sew.jpg")}
   {:name "Origami", :photo (str img-base "Origami.jpg")}
   {:name "Hats", :photo (str img-base "Hats.jpg")}
   {:name "Garment", :photo (str img-base "Clothes.jpg")}
   {:name "Accessories", :photo (str img-base "Gloves-Shoes.jpg")}
   {:name "Bags", :photo (str img-base "Bags.jpg")}
   {:name "Room Decor", :photo (str img-base "Home-Decor.jpg")}
   ])

(def products-raw
  [{:name "Planet Bag", :id "bag-earth-moon", :tags #{"Fiber" "Felt" "Bags"}, :detail-pics 6}
   {:name "Planet Bag 2", :id "bag-planet-2", :tags #{"Fiber" "Felt" "Bags"}, :detail-pics 3}
   {:name "Pink Bag", :id "bag-pink-purple", :tags #{"Fiber" "Felt" "Bags"}, :detail-pics 3}
   {:name "Milky Way Purse", :id "milkyway-purse", :tags #{"Fiber" "Felt" "Bags"}, :detail-pics 3}
   {:name "Neptune Purse", :id "neptune-purse", :tags #{"Fiber" "Felt" "Bags"}, :detail-pics 2}
   {:name "Big White", :id "big-white", :tags #{"Fiber" "Felt" "Room Decor"}, :detail-pics 3}
   {:name "Sofa", :id "sofa", :tags #{"Fiber" "Felt" "Room Decor"}, :detail-pics 2}
   {:name "Qing", :id "qing-hat", :tags #{"Yarn" "Knit & Crochet" "Hats"}, :detail-pics 4}
   {:name "Song", :id "song-hat", :tags #{"Yarn" "Knit & Crochet" "Hats"}, :detail-pics 2}
   {:name "Rockstar", :id "rockstar-hat", :tags #{"Yarn" "Knit & Crochet" "Hats"}, :folder :only-details, :detail-pics 2}
   {:name "ZhuGe L", :id "zhuge-hat", :tags #{"Yarn" "Knit & Crochet" "Hats"} :detail-pics 2, :folder true}
   {:name "Female Glove", :id "pink-glove", :tags #{"Yarn" "Knit & Crochet" "Accessories"}, :detail-pics 4, :folder true}
   {:name "Male Glove", :id "gray-glove", :tags #{"Yarn" "Knit & Crochet" "Accessories"} :detail-pics 9, :folder :only-details}
   {:name "Surgeon Glove", :id "surgeon-glove", :tags #{"Yarn" "Knit & Crochet" "Accessories"}, :detail-pics 2, :folder true}
   {:name "Fingerless Glove", :id "fingerless-glove", :tags #{"Yarn" "Fur" "Knit & Crochet" "Accessories"}, :detail-pics 2, :folder true}
   {:name "Baby Shoe", :id "baby-shoe", :tags #{"Yarn" "Knit & Crochet" "Accessories"}, :detail-pics 4, :folder true}
   {:name "Sandals", :id "sandals", :tags #{"Yarn" "Knit & Crochet" "Accessories"}, :detail-pics 1, :folder true}
   {:name "Corona Virus", :id "corona-virus", :tags #{"Yarn" "Knit & Crochet" "Accessories"} :detail-pics 0}
   {:name "Rose", :id "rose", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 2, :folder true}
   {:name "Orchid", :id "orchid", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 1}
   {:name "Lily of Valley", :id "lily-of-valley", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 1}
   {:name "Balloon Dog", :id "balloon-dog", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 2, :folder true}
   {:name "Dinosaur", :id "dinosaur", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 4, :folder true}
   {:name "Penguin", :id "penguin", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 4, :folder true}
   {:name "Sheep", :id "sheep", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 15, :folder true}
   {:name "Small Dog", :id "small-dog", :tags #{"Yarn" "Knit & Crochet" "Room Decor"}, :detail-pics 4, :folder true}
   {:name "Baby Blanket", :id "baby-blanket", :tags #{"Yarn" "Knit & Crochet" "Accessories"}, :detail-pics 1, :folder true}
   {:name "Knee Warmer", :id "knee-warmer", :tags #{"Yarn" "Knit & Crochet" "Accessories"}, :detail-pics 1, :folder true}
   {:name "Sweater", :id "sweater", :tags #{"Yarn" "Knit & Crochet" "Haute Couture"}, :detail-pics 1, :folder true}
   {:name "Panda Bamboo Tapestry", :id "panda-bamboo-tapestry", :tags #{"Yarn" "Weave & PomPom" "Room Decor"}, :detail-pics 2}
   {:name "Monkey Tapestry", :id "monkey-tapestry", :tags #{"Yarn" "Weave & PomPom" "Room Decor"}, :detail-pics 4}
   {:name "Autumn Dress" :id "autumn-dress", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 2}
   {:name "Bear Coat" :id "bear-coat", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 3}
   {:name "Cheonsam" :id "cheonsam", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 2}
   {:name "Green Coat" :id "green-coat", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 2}
   {:name "Joy Dress" :id "joy-dress", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 4}
   {:name "Princess Dress" :id "princess-dress", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 4, :folder true}
   {:name "Princess Line Dress" :id "princess-line", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 2, :folder :only-details}
   {:name "Season Dress" :id "season-dress", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 1, :folder :only-details}
   {:name "Spring Coat" :id "spring-coat", :tags #{"Fabric" "Haute Couture" "Garment" "Unikko"}, :detail-pics 1}
   {:name "Japanese Dress" :id "japanese-dress", :tags #{"Fabric" "Haute Couture" "Garment" "Unikko"}, :folder :only-details, :detail-pics 2}
   {:name "Mother Daughter Dress" :id "mother-daughter-dress", :tags #{"Fabric" "Haute Couture" "Garment" "Unikko"}, :folder true, :detail-pics 2}
   {:name "Red Dress" :id "red-dress", :tags #{"Fabric" "Haute Couture" "Garment" "Unikko"}, :folder :only-details, :detail-pics 2}
   {:name "Double Side Dress" :id "double-side-dress", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 2, :folder :only-details}
   {:name "Trench Coat" :id "trench-coat", :tags #{"Fabric" "Haute Couture" "Garment" "Unikko"}, :detail-pics 3}
   {:name "Long Winter Coat" :id "winter-ao", :tags #{"Fabric" "Haute Couture" "Garment" "Unikko"}, :detail-pics 4}
   {:name "Winter Coat" :id "winter-coat", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 1}
   {:name "Han Black Coat" :id "han-black-coat", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 4}
   {:name "Emperors Yellow Jacket" :id "emperors-yellow-jacket", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 4}
   {:name "Kimono" :id "kimono", :tags #{"Fabric" "Haute Couture" "Garment"}, :detail-pics 1, :folder :only-details}
   {:name "Baby Body" :id "baby-body", :tags #{"Fabric" "Garment"}, :detail-pics 0, :folder true}
   {:name "Plant Handbag" :id "plant-handbag", :tags #{"Fabric" "Bags"}, :detail-pics 3}
   {:name "Phone Bag" :id "phone-bag", :tags #{"Fabric" "Bags" "Unikko"}, :detail-pics 1, :folder true}
   {:name "Shoulder Bag" :id "shoulder-bag", :tags #{"Fabric" "Bags"}, :detail-pics 3, :folder :only-details}
   {:name "Backpack" :id "backpack", :tags #{"Fabric" "Bags"}, :detail-pics 1, :folder true}
   {:name "Cosmetic Bag 1" :id "cosmetic-bag-1", :tags #{"Fabric" "Bags"}, :detail-pics 3, :folder true}
   {:name "Cosmetic Bag 2" :id "cosmetic-bag-2", :tags #{"Fabric" "Bags"}, :detail-pics 3, :folder true}
   {:name "Cosmetic Bag 3" :id "cosmetic-bag-3", :tags #{"Fabric" "Bags"}, :detail-pics 2, :folder true}
   {:name "Pillow Cover", :id "pillow-cover", :tags #{"Room Decor" "Fabric" "Unikko"}, :detail-pics 2, :folder :only-details}
   {:name "Stone Pillow", :id "stone-pillow", :tags #{"Room Decor" "Fabric" "Unikko"}, :detail-pics 2, :folder :only-details}
   {:name "Fox Hat 1" :id "fox-hat-1", :tags #{"Leather & Fur" "Hats"}, :detail-pics 7}
   {:name "Small Hat" :id "small-hat", :tags #{"Fabric" "Hats" "Unikko"}, :detail-pics 5, :folder :only-details}
   {:name "Gray Mink Glove" :id "gray-mink-glove", :tags #{"Leather & Fur" "Accessories"}, :detail-pics 6}
   {:name "White Mink Glove" :id "white-mink-glove", :tags #{"Leather & Fur" "Accessories"}, :detail-pics 4}
   {:name "Orange Mitten" :id "orange-mitten", :tags #{"Leather & Fur" "Accessories"}, :detail-pics 1}
   {:name "Fox Bag Charm" :id "fox-key-charm", :tags #{"Leather & Fur" "Accessories"}, :detail-pics 5}
   {:name "Hare Bag" :id "hare-bag", :tags #{"Leather & Fur" "Bags"}, :detail-pics 5}
   {:name "Lamp Cover 1" :id "lamp-cover-1", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 1}
   {:name "Lamp Cover 2" :id "lamp-cover-2", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 7}
   {:name "Organic Lamp Cover" :id "organic-lamp-cover", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 2}
   {:name "Asian Lamp Cover" :id "asian-lamp-cover", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 4}
   {:name "Bunny and Chicken" :id "bunny-and-chicken", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 3}
   {:name "Cactus" :id "cactus", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 3}
   {:name "Ruby" :id "ruby", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 4, :folder true}
   {:name "Yellow Diamond" :id "yellow-diamond", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 2, :folder true}
   {:name "Night Lamp" :id "night-lamp", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 2, :folder true}
   {:name "Fūrin" :id "furin", :tags #{"Paper" "Origami" "Room Decor"}, :detail-pics 4, :folder true}
   ;; Hardware
   {:name "Egg holder" :id "egg-holder", :tags #{"Stone"}, :detail-pics 1}
   {:name "Mushroom 1" :id "mushroom1", :tags #{"Stone"}, :detail-pics 6}
   {:name "Mushroom 2" :id "mushroom2", :tags #{"Stone"}, :detail-pics 2}
   {:name "Mushroom 3" :id "mushroom3", :tags #{"Stone"}, :detail-pics 3}
   {:name "Pool" :id "pool", :tags #{"Stone"}, :detail-pics 1}
   {:name "Zen Pumpkin" :id "pumpkins", :tags #{"Stone"}, :detail-pics 5}
   {:name "Big Flower Pot" :id "big-flower-pot", :tags #{"Stone"}, :detail-pics 1}
   {:name "Eco Pot 0" :id "eco-pot-0", :tags #{"Stone"}, :detail-pics 2}
   {:name "Flower Pot" :id "flower-pot", :tags #{"Stone"}, :detail-pics 2}
   {:name "Jewellery Box" :id "jewellery-box", :tags #{"Stone"}, :detail-pics 5}
   {:name "Oasis Platter" :id "oasis-platter", :tags #{"Stone"}, :detail-pics 1}
   {:name "Tray" :id "tray", :tags #{"Stone"}, :detail-pics 1}
   ;; Painting
   {:name "Acrylic" :id "acrylic" :tags #{"Painting"}, :folder :only-details
    :detail-pics [{:name "detail1.jpg" :title "Water lily"}
                  {:name "detail2.jpg" :title "Water lily"}
                  {:name "detail3.jpg" :title "Lighthouse"}
                  {:name "detail4.jpg" :title "Aalto tile"}
                  {:name "detail5.jpg" :title "Aalto tile"}
                  {:name "detail6.jpg" :title "Grass and mask"}]}
   {:name "Watercolor" :id "water-color" :tags #{"Painting"}, :detail-pics 5, :folder :only-details
    :description "Olive glaze"}
   {:name "Sketch" :id "sketch" :tags #{"Painting"}, :folder :only-details
    :detail-pics [{:name "woman-in-hat.jpg", :title "Woman in hat"}
                  {:name "hair-bun.jpg", :title "Hair bun"}
                  {:name "sleeping-daughter.jpg", :title "Sleeping daughter"}
                  {:name "korean-girl.jpg", :title "Korean girl"}
                  {:name "detail5.jpg", :title ""}
                  {:name "saima.jpg", :title "Saima"}
                  {:name "woman-lunch.jpg", :title "Woman having lunch"}
                  {:name "fight-for-right.jpg", :title "Fight for your right"}
                  {:name "nordic-me.jpg", :title "Nordic me"}
                  {:name "le-corb.jpg", :title "Le Corb"}
                  {:name "to-the-moon.jpg", :title "To the moon"}
                  {:name "female-sumo.jpg", :title "Female sumo wrestler"}]}
   ;; CGI
   {:name "Kids painting" :id "kids-painting" :tags #{"CGI"}, :folder :only-details
    :detail-pics [{:name "circus-original.jpg", :title "Circus original"}
                  {:name "circus-render.jpg", :title "Circus render"}
                  {:name "dream-original.jpg", :title "Dream original"}
                  {:name "dream-render.jpg", :title "Dream render"}
                  {:name "head-original.jpg", :title "Head original"}
                  {:name "head-render.jpg", :title "Head render"}]}
   {:name "House rendering" :id "house-rendering" :tags #{"CGI"}, :folder :only-details
    :detail-pics [{:name "house1a.jpg", :title ""}
                  {:name "house1b.jpg", :title ""}
                  {:name "house1c.jpg", :title ""}
                  {:name "room2a.jpg", :title ""}
                  {:name "room2b.jpg", :title ""}
                  {:name "room2c.jpg", :title ""}
                  {:name "room2d.jpg", :title ""}
                  {:name "room2e.jpg", :title ""}]}
   ;; Studio
   {:name "Studio", :id "studio", :tags #{"Studio"}, :detail-pics 16, :folder true}
   ;; Travel log
   {:name "Valencia", :id "valencia", :tags #{"Travel Log"}, :folder :only-details, :detail-pics 3,
    :description [:<> [:div "History is always astonishingly similar, one step forward, is cliff, do you jump?
                  There is no turning back, wolves behind."]
                  [:div "Answer is simple:"]
                  [:div "On horse back, turn; On foot, swim."]]}
   {:name "Frank Gehry", :id "frank-gehry", :tags #{"Travel Log"}, :folder :only-details, :detail-pics 5
    :description [:<> [:div "You could have your Disney on,"]
                  [:div "You could wear your Louis Vuitton,"]
                  [:div "But even with nothing on,"]
                  [:div "Yet you made me look, master Frank G."]]}
   {:name "Portugal", :id "portugal", :tags #{"Travel Log"}, :folder :only-details, :detail-pics 3
    :description "Skeleton of architecture vs. Natural chamber with windows."}
   {:name "England", :id "england" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 6,
    :description [:<> [:div "The Castle Theatre of Corfe"]
                  [:div "Stage One: The Lord of the Ring part III: Return of the King"]
                  [:div "Seat: Sir Hatton & Lady Bankes"]]}
   {:name "Stonehenge", :id "stonehenge" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 2
    :description "Solar angle has not changed in 5000 years, people have changed a lot."}
   {:name "Hadid or BIG", :id "hadid-and-big" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 4
    :description [:<> [:div "There are 360 degrees, so why stick to one?"]
                  [:div "It all depends on what structure you use and what materials you choose,
                  have you ever seen a tree that is not straight? In history, trunks were bent
                  to be built, but only for “Heaven”, for the place where they can afford the costs."]]}
   {:name "Jean Nouvel", :id "jean-nouvel" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 4
    :description [:<> [:div "Grey roof, White walls, Green Water, Master Nouvel seems to know a lot about “Gangnam” style.
    The pattern on the roof reveals local islamic culture without a doubt, standing beneath it feels like
    under some futuristic alien spaceship, meanwhile surroundings are full of human history.
    What a masterpiece!"]
                  [:hr]
                  [:div "More than happy to see Jean Nouvel in “Middle East”."]]}
   {:name "Frankfurt", :id "frankfurt" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 2,
    :description [:<> [:div "Finally let’s talk about artificial intelligence. Why are we afraid?
                  Do we need to be afraid? Is it going to take over us? To answer those,
                  we need to travel back in time,
                  back to the time man invented screwdriver for the first time."]
                  [:div "Why did man invent screwdriver? Because we wanted to tighten a screw.
                  If we give a screwdriver and a bunch of screws to a monkey, what would happen?
                  Those metals look like very interesting shiny toys to the monkey, it wants to
                  play with it, maybe testing it with hands and mouth, take them back home, share
                  with other monkeys, but do they know the correct way to tighten a screw with the
                  tool in its hand? How long does it take for it to discover the true purpose of
                  those elegant metalware? If monkeys never discover the right way of using screw driver,
                  will they abuse them?"]
                  [:div "Human is different from monkeys and other animals, for the abilities of being
                  able to use tools. We will not be afraid of screw driver, because we know what we
                  can do with it, and why we invent it. AI is a tool human invented, but now only few
                  are able to master this tool, many of us are similar to monkeys playing with screwdriver:
                  It brings fun, excitement, and occasionally fear for uncertainty. In order to master
                  this tool, we need to keep asking ourself: Why we invent AI? What is its purpose for us?
                  How can we master this tool? Which part of what we are doing now with AI is like monkey
                  playing screw driver? Is there a “manual” of this advanced fancy “screwdriver” ? When all
                  these questions are properly answered, there should be no more need to fear of AI,
                  it will be just another handy tool that free us “cave man”."]]}
   {:name "Hamburg", :id "hamburg" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 2
    :description "I admire Herzog & de Meuron with my full heart."}
   {:name "Beijing", :id "beijing" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 5
    :description "Who does not want a glazed fireplace and marble grave?"}
   {:name "Mars", :id "mars" :tags #{"Travel Log"}, :folder :only-details, :detail-pics 6
    :description [:<> [:div "Barbie: Shall we begin our work on Mars?"]
                  [:div "Starting from a seed, bring our farm animals, and also, the Flamingo!"]]}])

(def products (mapv amend-product products-raw))

(def products-index (index-by :id products))

(rf/reg-event-db ::initialize-db
  (fn [_] {}))