(ns spreadsheet.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [accountant.core :as accountant]
            [spreadsheet.sheet :refer [sheet-component]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<!]]
            [cognitect.transit :as t]
            [re-frame.core :refer [dispatch
                                   dispatch-sync]]
            [spreadsheet.data]))

;; -------------------------
;; Views

(defn home-page []
  [:div {:on-click #(dispatch [:cell-lose-focus])}
   [:h1 "Spreadsheet"]
   [:h3 "A single page app test in ClojureScript"]
   [sheet-component 3 3]
   [:br]
   [:div.col-xs-3
     [:input.form-control {:type "button"
                           :value "Save spreadsheet"
                           :on-click (fn [e]
                                       (dispatch [:save-spreadsheet])
                                       (.stopPropagation e))}]]
   [:div [:a {:href "/about"} "go to about page"]]])

(defn about-page []
  [:div [:h1 "About Spreadsheet"]
   [:div
    [:p "This app is intended to be a clone of Google Sheets
        to show how to implement a single page app in a
        reactive functional manner"]]
   [:div [:a {:href "/"} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

(defn not-found-page []
  [:div [:h1 "Resource not found"]])

;; -------------------------
;; Routes

(secretary/defroute "/" []
                    (session/put! :current-page #'home-page))

(secretary/defroute "/:id" {id :id}
                    (do
                      (go
                        (let [response (<! (http/get (str "/api/cell/" id) {}))]
                          (if-let [sheet-str (:cell (:body response))]
                            (let [r (t/reader :json)
                                  sheet (t/read r sheet-str)]
                              (dispatch [:load-spreadsheet sheet]))
                            (js/alert "Spreadsheet not found"))))
                      (session/put! :current-page #'home-page)))

(secretary/defroute "/about" []
                    (session/put! :current-page #'about-page))

;; -------------------------
;; Initialize app

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (dispatch-sync [:initialize])
  (accountant/configure-navigation!)
  (accountant/dispatch-current!)
  (mount-root))
