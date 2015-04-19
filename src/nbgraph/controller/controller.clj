(ns nbgraph.controller.controller
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [nbgraph.model.dbutils.mongodbconfig :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.graphmodel :refer :all]))

(declare init-example-graph-if-not-already)

(defn init
  "Init function for the server"
  []
  (println "Initializing server...")
  (init-example-graph-if-not-already))

;;; Load or populate for the first
;;; time the graph on the 'edges' example file

(defn- populate-example-graph! [g]
  (with-open [rdr (clojure.java.io/reader "resources/edges")]
    (doall (map #(let [edges (clojure.string/split % #" ")]
                   (create-edge! g (first edges) (second edges)))
                (line-seq rdr))))
  (closeness g (first (get-nodes g)))
  (println "Init DB ok."))

(defn- init-example-graph-if-not-already []
  (connect! :production)
  (if (graph-exists? "nbexample")
    (def g (load-graph! "nbexample"))
    (do
      (def g (create-graph! "nbexample"))
      (populate-example-graph! g))))

(defroutes main-routes
  (GET "/" [] "Server up")
  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (wrap-defaults main-routes site-defaults))
