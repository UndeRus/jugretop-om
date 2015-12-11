(ns ^:figwheel-always jugretop-om.core
    (:require
        [clojure.string :refer [replace] :as s]

        ;;Views
        [om.core :as om :include-macros true]
        [om.dom :as dom :include-macros true]
        [om-tools.core :refer-macros [defcomponent]]
        [kioo.om :refer [set-style set-attr do-> substitute listen] :as kio :include-macros true]
        [kioo.core :refer [handle-wrapper]]
        ;;Ajax
        [ajax.core :refer [GET POST json-response-format]]

        ;;Modules
        [jugretop-om.state :refer [app-state]]
        [jugretop-om.utils :refer [unescape-html]]
        )
    (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

;;(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload







;;Ajax

(defn root-cursor [] (om/root-cursor app-state))

(defn posts []
  (om/ref-cursor
    (:posts (om/root-cursor app-state))))

(defn page []
  (om/ref-cursor
    (:page (om/root-cursor app-state))))

(defn load-feed [page callback]
  (let [cursor (om/root-cursor app-state)]
  (om/update! cursor [:loading] true)
  (GET (str "http://s.jugregator.org/api?page=" page)
      :response-format (json-response-format {:keywords? true})
      :handler (fn [result]
                (callback result)
                (om/update! cursor [:loading] false)
                ))
  ))

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


(defsnippet empty-view "public/template.html" [:div#empty]
  [_]
  {})

(defcomponent empty-widget [data _]
  (display-name [_] "Empty")
  (render [_]
    (empty-view data)))


(defsnippet root-view "public/template.html" [:#root]
  [data]
  {
    [:#content] (do-> (kio/content
                          (let [posts-available (-> (count (posts))
                                                    (> 0))]
                            (if posts-available (om/build-all post-widget (posts) {:key :id})
                                                (om/build empty-widget nil))))
                      (listen :on-mount
                        (fn [] (let [cursor (posts)]
                                      (load-feed 1 #(om/update! cursor %))))))
    [:#refresh] (do-> (listen :onClick (fn [] (let [cursor (om/root-cursor app-state)]
                                              (om/transact! cursor :page inc)
                                              (load-feed (get cursor :page)
                                                (fn [result]
                                                  (om/transact! cursor :posts #(vec (concat % result))))))))
                      (kio/set-attr :disabled (:loading (om/root-cursor app-state))))

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
