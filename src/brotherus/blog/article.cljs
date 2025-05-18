(ns brotherus.blog.article
  (:require [clojure.walk :as walk]
            ["marked" :as marked]
            [taipei-404.html :refer [html->hiccup]]
            ["he" :as he]))

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

(defn preprocess [hiccup]
  (->> hiccup
       (walk/postwalk
         (fn [node]
           (cond-> node
                   (is-element? node :a) set-link-new-tab
                   (string? node) (he/decode node) ;; HTML entities produced by marked like &lt; &gt; etc. to actual chars
                   )))))

(defn markdown-to-hiccup [markdown]
  (some->> markdown
           ;; marked/parse Uses GitHub-flavored markdown spec https://github.github.com/gfm/ ,
           ;; a superset of CommonMark. This is good since GitHub can be used as a backup for
           ;; rendering and reading the blog-articles in absence of this webapp.
           marked/parse
           html->hiccup
           (into [:div])
           preprocess))