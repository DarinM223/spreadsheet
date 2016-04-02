  (ns spreadsheet.handler
    (:require [compojure.core :refer [GET POST DELETE defroutes context]]
              [compojure.route :refer [not-found resources]]
              [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
              [ring.middleware.json :as middleware]
              [ring.middleware.params :refer [wrap-params]]
              [ring.util.response :refer [response]]
              [hiccup.core :refer [html]]
              [hiccup.page :refer [include-js include-css]]
              [crypto.random :as random]
              [spreadsheet.middleware :refer [wrap-middleware]]
              [spreadsheet.redis :refer [set-cell get-cell remove-cell]]
              [environ.core :refer [env]]))

(def mount-target
  [:div#app
   [:h3 "ClojureScript has not been compiled!"]
   [:p "please run "
    [:b "lein figwheel"]
    " in order to start the compiler"]])

(def loading-page
  (html
   [:html
    [:head
     [:meta {:charset "utf-8"}]
     [:meta {:name "viewport"
             :content "width=device-width, initial-scale=1"}]
     (include-css "css/bootstrap.min.css")
     (include-css "css/bootstrap-theme.min.css")
     (include-css (if (env :dev) "css/site.css" "css/site.min.css"))]
    [:body
     mount-target
     (include-js "js/app.js")]]))

(defn set-cell-route
  "Saves a cell and returns a identifier to the cell"
  [req]
  (if-let [cell (:cell (:params req))]
    (let [{code :retcode hash :hash} (set-cell cell)
          success (= code "OK")]
      (response {:success success :hash hash}))
    (response {:err "Could not retrieve cell"})))

(defn get-cell-route
  "Gets a cell with the specified identifier"
  [id]
  (response (get-cell id)))

(defn delete-cell-route
  "Removes a cell with the specified identifier"
  [id]
  (response (remove-cell id)))

(defroutes api-routes
  (GET "/cell/:id" [id] (get-cell-route id))
  (POST "/cell" req (set-cell-route req))
  (DELETE "/cell/:id" [id] (delete-cell-route id)))

(defroutes routes
  (GET "/" [] loading-page)
  (GET "/:id" {} loading-page)
  (GET "/about" [] loading-page)

  (resources "/")

  (context "/api" []
    (-> api-routes
        (middleware/wrap-json-body)
        (middleware/wrap-json-response)))

  (not-found "Not Found"))

(def app
  (wrap-params (wrap-defaults #'routes (assoc-in site-defaults [:security :anti-forgery] false))))
