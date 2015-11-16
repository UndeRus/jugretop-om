(ns ^:figwheel-always jugretop-om.core
    (:require
        [om.core :as om :include-macros true]
        [om.dom :as dom :include-macros true]
        [om-tools.core :refer-macros [defcomponent]]
        [kioo.om :refer [set-style set-attr do-> substitute listen] :as kio :include-macros true]
        [kioo.core :refer [handle-wrapper]]
        [ajax.core :refer [GET POST json-response-format]]
        [clojure.string :refer [replace] :as s]
        )
    (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

;;(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {
    :text "Hello asdasdworld!"
    :posts []
}))


(defn unescape-html [escaped-string]
    (-> escaped-string
      (s/replace #"&amp;" "&")
      (s/replace #"&lt;" "<")
      (s/replace #"&gt;" ">")
      (s/replace #"&quot;" "\"")))



(defsnippet post-view "public/template.html" [:div#post]
    [{mid :mid replies :replies body :body {uname :uname uid :uid} :user}]
    {
        [:.post-mid] (do-> (kio/content (str "#" mid))
                           (kio/set-attr :href (str "https://juick.com/" mid)
                                         :target "_blank"))
        [:.post-url] (kio/set-attr :href (str "http://jugregator.org/p/" mid)
                                   :target "_blank")
        [:.nickname] (kio/content (str "@" uname))
        [:.avatar] (kio/set-attr :src (str "http://i.juick.com/a/" uid ".png"))
        [:.post-text] (kio/html-content (unescape-html body))
        [:.post-replies] (kio/content replies)
    })


(defcomponent post-widget [data _]
  (render [_]
    (post-view data)))


(defn posts []
    (om/ref-cursor (:posts (om/root-cursor app-state))))



(defn load-feed [page callback]
  (GET (str "http://s.jugregator.org/api?page=" page)
      :response-format (json-response-format {:keywords? true})
      :handler callback))

(defn post-list [data owner]
  (reify
      om/IWillMount
      (will-mount [_]
        (load-feed 1 #(swap! app-state assoc :posts %)))
      om/IRender
      (render [_]
          (dom/div #js {:className "container"}
            (dom/div nil (dom/h2 nil "Juick React Om"))
            (om/build-all post-widget (posts) {:key :id})))))



(om/root post-list app-state
  {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;;(swap! app-state update-in [:__figwheel_counter] inc)
)
