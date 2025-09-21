(ns brotherus.blog.article
  (:require [clojure.walk :as walk]
            [clojure.string :as str]
            ["marked" :refer [Marked]]
            ["marked-highlight" :refer [markedHighlight]]
            [taipei-404.html :refer [html->hiccup]]
            ["highlight.js/lib/core" :as hljs]
            ["highlight.js/lib/languages/clojure" :as clj]
            ["highlight.js/lib/languages/basic" :as basic]
            ["highlight.js/lib/languages/plaintext" :as plaintext]
            ["highlight.js/lib/languages/css" :as css]
            ["highlight.js/lib/languages/bash" :as bash]
            ["highlight.js/lib/languages/delphi" :as delphi]
            ["highlight.js/lib/languages/csharp" :as csharp]))

(.registerLanguage hljs "clojure" clj)
(.registerLanguage hljs "basic" basic)
(.registerLanguage hljs "plaintext" plaintext)
(.registerLanguage hljs "css" css)
(.registerLanguage hljs "bash" bash)
(.registerLanguage hljs "delphi" delphi)
(.registerLanguage hljs "csharp" csharp)

(def ^:dynamic *rendering-context* nil)

(defn parse-node [[tag attr & children]]
  (if (map? attr)
    [tag attr (or children [])]
    [tag {} (cons attr children)]))

(defn is-element? [node tag]
  (and (vector? node)
       (= tag (first node))))

(defn local-link? [url]
  (str/starts-with? url "#"))

(defn set-link-new-tab [a]
  (let [[_tag attr _children] (parse-node a)]
    (cond-> a
            (not (local-link? (:href attr)))
            (assoc-in [1 :target] "_blank"))))

(def content-base
  "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main")

(defn relative-link? [url] (not (re-find #"^https?://" url)))

(defn make-absolute-link [src]
  (str content-base "/" (:item-id *rendering-context*) "/" src))

(defn fix-image-src [src]
  (cond-> src
          (relative-link? src) make-absolute-link))

(defn fix-image-links [img]
  ;; Also wrap to <a> to allow opening the image in a new tab
  (let [url (fix-image-src (get-in img [1 :src]))]
    [:a {:href url} (assoc-in img [1 :src] url)]))

(defn create-heading-id "Create a URL-friendly ID from heading text"
  [text]
  (-> text
      (str/lower-case)
      (str/replace #"[^\w\s-]" "") ; Remove non-word chars except spaces and hyphens
      (str/replace #"\s+" "-") ; Replace spaces with hyphens
      (str/replace #"-+" "-") ; Replace multiple hyphens with single
      (str/replace #"^-|-$" ""))) ; Remove leading/trailing hyphens

(defn is-heading? "Check if a hiccup node is a heading (h1, h2, h3, h4, h5, h6)"
  [node]
  (and (vector? node)
       (keyword? (first node))
       (re-matches #"h[1-6]" (name (first node)))))

(defn add-heading-anchor "Transform a heading hiccup element to include an anchor link"
  [heading]
  (let [[_tag _attr children] (parse-node heading)
        text-content (first children)
        id (create-heading-id text-content)]
    [:a {:id id, :href (str "#" id), :class "heading-anchor"} heading]))

(defn extract-headings [hiccup]
  (->> (tree-seq coll? seq hiccup)
       (filter is-heading?)
       (map (fn [[tag text-content]]
              {:text text-content
               :id (create-heading-id text-content)
               :level (js/parseInt (subs (name tag) 1))}))
       (into [])))

(defn generate-toc "Generate table of contents hiccup from headings"
  [headings]
  (when (seq headings)
    [:div {:class "table-of-contents"}
     [:h3 "Table of Contents"]
     [:ul
      (map (fn [{:keys [text id level]}]
             [:li {:style (str "margin-left: " (* (- level 1) 20) "px")}
              [:a {:href (str "#" id)} text]])
           headings)]]))

(defn replace-toc-markers "Replace [TOC] markers with table of contents"
  [hiccup]
  (.log js/console "replace-toc-markers")
  (let [headings (extract-headings hiccup)
        xx (.log js/console (str headings))
        toc (generate-toc headings)]
    (->> hiccup
         (walk/postwalk
           (fn [node]
             (if (= node [:p "[TOC]"]) toc node))))))

(defn postprocess [hiccup]
  (->> hiccup
       (walk/postwalk
         (fn [node]
           (cond-> node
                   (is-element? node :a) set-link-new-tab
                   (is-element? node :img) fix-image-links
                   (is-heading? node) add-heading-anchor)))
       replace-toc-markers))

(def marked-options
  #js {:emptyLangClass "hljs"
       :langPrefix "hljs language-"
       :highlight (fn [code lang]
                    (.-value (.highlight
                               hljs code #js {:language (if (= lang "") "plaintext" lang)})))})

(defn markdown-to-hiccup [markdown context]
  (binding [*rendering-context* context]
    (let [mark (Marked. (markedHighlight marked-options))]
      (some->> markdown
               ;; marked/parse Uses GitHub-flavored markdown spec https://github.github.com/gfm/ ,
               ;; a superset of CommonMark. This is good since GitHub can be used as a backup for
               ;; rendering and reading the blog-articles in absence of this webapp.
               (.parse mark)
               html->hiccup
               (into [:div])
               postprocess))))
