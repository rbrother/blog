(ns brotherus.blog.config)

(def debug?
  ^boolean goog.DEBUG)

(def localhost?
  (some #{"localhost" "127.0.0.1"} [(.-hostname js/location)]))