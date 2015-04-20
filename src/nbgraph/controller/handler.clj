(ns nbgraph.controller.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.middleware.json :as middleware]
            [ring.util.response :refer :all]
            [nbgraph.controller.graphcontroller :as graphcontroller]))

(defroutes main-routes
  (GET "/" [] (redirect "home.html"))
  (GET "/nodes" [] (graphcontroller/all-nodes))
  (GET "/nodes/rank-by/:rank/limit/:limit" [rank limit]
       (graphcontroller/all-nodes rank (Integer. limit)))
  (POST "/edge" {nodes :params}
        (graphcontroller/add-edge (:node1 nodes) (:node2 nodes)))
  (PUT "/node" {params :params}
        (graphcontroller/update-node params))
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> main-routes
      (wrap-defaults
       ;; Using no anti-forgery tokens while testing
       ;; TODO: Use it
       (assoc-in site-defaults [:security :anti-forgery] false)
       ;; site-defaults
       )
      (middleware/wrap-json-params)
      (middleware/wrap-json-response)))

;;; Init function for the server =============================================

(defn init
  []
  (println "Initializing server...")
  (graphcontroller/init-example-graph-if-not-already :production))
