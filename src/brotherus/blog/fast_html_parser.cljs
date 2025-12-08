(ns brotherus.blog.fast-html-parser
  (:require ["htmlparser2" :as htmlparser]))

;; Fast HTML to Hiccup converter using htmlparser2
;; This is ~10x faster than taipei-404/html-to-hiccup

(defn parse-attributes
  "Convert JS object of attributes to Clojure map"
  [attrs]
  (when attrs
    (js->clj attrs :keywordize-keys true)))

(defn dom-node->hiccup
  "Convert a htmlparser2 DOM node to hiccup format"
  [node]
  (let [node-type (.-type node)]
    (cond
      ;; Text node
      (= node-type "text")
      (.-data node)

      ;; Element node
      (= node-type "tag")
      (let [tag (keyword (.-name node))
            attrs (parse-attributes (.-attribs node))
            children (.-children node)
            child-hiccups (when children
                            (map dom-node->hiccup (array-seq children)))]
        (if (seq attrs)
          (into [tag attrs] child-hiccups)
          (into [tag] child-hiccups)))

      ;; Comment or other node types - ignore
      :else
      nil)))

(defn html->hiccup
  "Fast conversion of HTML string to hiccup using htmlparser2.
   This is significantly faster than taipei-404/html-to-hiccup."
  [html-string]
  (let [;; Parse HTML to DOM
        dom (.parseDocument htmlparser html-string)
        ;; Get children of the document node
        children (.-children dom)
        ;; Convert all children to hiccup
        hiccup-nodes (keep dom-node->hiccup (array-seq children))]
    ;; Return wrapped in a div if multiple root nodes
    (if (= (count hiccup-nodes) 1)
      (first hiccup-nodes)
      (into [:div] hiccup-nodes))))
