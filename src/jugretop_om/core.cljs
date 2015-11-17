(ns ^:figwheel-always jugretop-om.core
    (:require
        [om.core :as om :include-macros true]
        [om.dom :as dom :include-macros true]
        [om-tools.core :refer-macros [defcomponent]]
        [kioo.om :refer [set-style set-attr do-> substitute listen] :as kio :include-macros true]
        [kioo.core :refer [handle-wrapper]]
        [ajax.core :refer [GET POST json-response-format]]
        [clojure.string :refer [replace] :as s]
        [jugretop-om.state :refer [app-state]]
        [jugretop-om.utils :refer [unescape-html]]
        )
    (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

;;(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload







;;Ajax
(defn posts [] (om/ref-cursor (:posts (om/root-cursor app-state))))

(defn page [] (om/ref-cursor (:page (om/root-cursor app-state))))

(defn load-feed [page callback]
  (GET (str "http://s.jugregator.org/api?page=" page)
      :response-format (json-response-format {:keywords? true})
      :handler callback))
;;end



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
  (display-name [_] "Post")
  (render [_]
    (post-view data)))


(defsnippet root-view "public/template.html" [:#root]
  [_]
  {
    [:#content] (do-> (kio/content (om/build-all post-widget (posts) {:key :id}))
                      (listen :on-mount
                        (fn [] (let [cursor (posts)]
                                      (load-feed 1 #(om/update! cursor %))))))
    ;[:#refresh] (listen :onClick (fn [] (do (let [cursor-page (page)
    ;                                              cursor-posts (posts)]
    ;                                          (om/update! cursor-page (+ cursor-page 1))
    ;                                          (load-feed (page) #(swap! app-state assoc :posts %))))))
  })





(defn root [data]
  (om/component (root-view data)))


(om/root root app-state
  {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;;(swap! app-state update-in [:__figwheel_counter] inc)
)
