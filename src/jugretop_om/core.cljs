(ns ^:figwheel-always jugretop-om.core
    (:require
        [om.core :as om :include-macros true]
        [om.dom :as dom :include-macros true]
        [kioo.om :refer [set-style set-attr do-> substitute listen] :as kio :include-macros true]
        [kioo.core :refer [handle-wrapper]]
        [ajax.core :refer [GET POST json-response-format]])
    (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

(enable-console-print!)

;;(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {
    :text "Hello asdasdworld!"
    :posts []
}))


(defsnippet post-view "public/template.html" [:div#post]
    [{mid :mid replies :replies body :body {uname :uname uid :uid} :user}]
    {
        [:.post-mid] (kio/content (str "#" mid))
        [:.nickname] (kio/content (str "@" uname))
        [:.avatar] (kio/set-attr :src (str "http://i.juick.com/a/" uid ".png"))
        [:.post-text] (kio/content body)
        [:.post-replies] (kio/content replies)
    })


(defn post-widget [data]
    (om/component (post-view data)))


(defn posts []
    (om/ref-cursor (:posts (om/root-cursor app-state))))



(defn post-list [data owner]
  (reify
      om/IWillMount
      (will-mount [_]
          (GET "http://s.jugregator.org/api"
              :response-format (json-response-format {:keywords? true})
              :handler (fn [response] (swap! app-state assoc :posts response))))
      om/IRender
      (render [_]
          (dom/div #js {:className "container"}
            (dom/div nil (dom/h2 nil "Juick React Om"))
            (om/build-all post-widget (posts) {:key :id})))))



(comment

  (defn contact-view [contact owner]
    (reify
      om/IRender
      (render [this]
              (dom/li nil (str contact)))))

  (defn post-list [data owner]
    (reify
      om/IRender
      (render [this] (apply dom/ul nil
                            (om/build-all contact-view (:posts data))))))

  )



(om/root post-list app-state
  {:target (. js/document (getElementById "app"))})

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;;(swap! app-state update-in [:__figwheel_counter] inc)
)
