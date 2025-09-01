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

(defn set-link-new-tab [a]
  (let [[tag attr children] (parse-node a)]
    (into [tag (assoc attr :target "_blank")] children)))

(def content-base
  "https://raw.githubusercontent.com/rbrother/articles/refs/heads/main")

(defn relative-link? [url] (not (re-find #"^https?://" url)))

(defn make-absolute-link [src]
  (str content-base "/" (:item-id *rendering-context*) "/" src))

(defn fix-image-src [src]
  (cond-> src
    (relative-link? src) make-absolute-link))

(defn fix-image-links [img]
  (update-in img [1 :src] fix-image-src))

(defn postprocess [hiccup]
  (->> hiccup
       (walk/postwalk
        (fn [node]
          (cond-> node
            (is-element? node :a) set-link-new-tab
            (is-element? node :img) fix-image-links)))))

(def marked-options
  #js {:emptyLangClass "hljs"
       :langPrefix "hljs language-"
       :highlight (fn [code lang]
                    (.-value (.highlight
                              hljs code #js {:language (if (= lang "") "plaintext" lang)})))})

(defn markdown-to-hiccup [markdown context]
  (binding [*rendering-context* context]
    (print "Parsing markdown...")
    (let [mark (Marked. (markedHighlight marked-options))]
      (some->> markdown
               ;; marked/parse Uses GitHub-flavored markdown spec https://github.github.com/gfm/ ,
               ;; a superset of CommonMark. This is good since GitHub can be used as a backup for
               ;; rendering and reading the blog-articles in absence of this webapp.
               (.parse mark)
               html->hiccup
               (into [:div])
               postprocess))))
