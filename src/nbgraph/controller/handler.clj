(ns nbgraph.controller.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as middleware]
            [nbgraph.controller.graphcontroller :as graphcontroller]))

(defroutes main-routes
  (GET "/" [] "Server up")
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
      (wrap-defaults site-defaults)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))

;;; Init function for the server =============================================

(defn init
  []
  (println "Initializing server...")
  (graphcontroller/init-example-graph-if-not-already))
