(ns ^:figwheel-always jugretop-om.utils
  (:require [clojure.string :refer [replace] :as s]))

(defn unescape-html [escaped-string]
    (-> escaped-string
      (s/replace #"&amp;" "&")
      (s/replace #"&lt;" "<")
      (s/replace #"&gt;" ">")
      (s/replace #"&quot;" "\"")))


(defn by-id [tag-id]
  (. js/document (getElementById tag-id)))
