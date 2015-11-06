(ns ^:figwheel-always jugretop-om.core
    (:require [om.core :as om :include-macros true]
              [om.dom :as dom :include-macros true]
              [ajax.core :refer [GET POST json-response-format raw-response-format]]))

(enable-console-print!)

;;(println "Edits to this text should show up in your developer console.")

;; define your app data so that it doesn't get over-written on reload

(def app-state (atom {
                          :text "Hello asdasdworld!"
                          :posts []
                          }))


(defn hw-widget [data owner]
  (reify
    om/IRender
    (render [this]
            (dom/h1 nil (:text data)))))


(defn post-widget [{mid :mid body :body {uname :uname} :user} owner]
  (reify
    om/IRender
    (render [this]
        (dom/div nil
            (dom/div #js {:className "panel panel-default"}
                (dom/div #js {:className "panel-heading"}
                    (dom/div #js {:className "panel-title"}
                        (str "@" uname)))
                (dom/div #js {:className "panel-body"} body)
                (dom/div #js {:className "panel-footer"} (str "#" mid)))))))


(def testdata [{:body "темно http://www.nasa.gov/multimedia/nasatv/ это вам не кино", :user {:uname "den-po", :uid 15152}, :replies 6, :timestamp "2015-11-06 21:01:40", :mid 2809764, :daypart "18:00-24:00", :day "06-11-2015"}])


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
          (dom/div nil (dom/h2 nil "HEllo, codingteam")
          (om/build-all post-widget (posts) {:key :id})
          ))))



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
