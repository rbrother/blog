(ns brotherus.blog.config)

;; Static assets configuration
;; This should be updated to match your S3 bucket configuration
(def static-assets-base-url
  "https://brotherus-blog-blog-static-assets.s3.eu-north-1.amazonaws.com")

(defn static-asset-url
  "Generate URL for a static asset (image, CSS, etc.)"
  [path]
  (str static-assets-base-url "/" path))

(defn image-url
  "Generate URL for an image asset"
  [filename]
  (static-asset-url (str "images/" filename)))
