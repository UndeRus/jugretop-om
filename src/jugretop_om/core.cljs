(ns ^:figwheel-always jugretop-om.core
    (:require[om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]))

(enable-console-print!)

(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {
                          :text "Hello asdasdworld!"
                          :posts [1,2,3,4,5,6,7]
                          }))


(defn hw-widget [data owner]
  (reify
    om/IRender
    (render [this]
            (dom/h1 nil (:text data)))))


(defn post-widget [data owner]
  (reify
    om/IRender
    (render [this]
            (dom/div nil (dom/div #js {:className "jumbotron"}
                                  (dom/h1 nil "AUTISM"))))))

(om/root post-widget app-state
  {:target (. js/document (getElementById "app"))})


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;;(swap! app-state update-in [:__figwheel_counter] inc)
)

