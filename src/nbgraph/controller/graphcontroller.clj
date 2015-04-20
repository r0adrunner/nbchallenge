(ns nbgraph.controller.graphcontroller
  (:require [nbgraph.model.dbutils.mongodbconfig :refer :all]
            [ring.util.response :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.graphmodel :refer :all]))

;;; Model setup ==============================================================
;;; Load or populate for the first
;;; time the graph on the 'edges' example file

(defonce g (atom nil))

(defn- populate-example-graph! [g]
  (with-open [rdr (clojure.java.io/reader "resources/edges")]
    (doall (map #(let [edges (clojure.string/split % #" ")]
                   (create-edge! g (first edges) (second edges)))
                (line-seq rdr))))
  ;; This causes the update of the distance matrix and score tables
  (closeness g (first (get-nodes g)))
  (println "Init DB ok."))

(defn init-example-graph-if-not-already [mode]
  (connect! mode)
  (if (graph-exists? "nbexample")
    (reset! g (load-graph! "nbexample"))
    (do
      (reset! g (create-graph! "nbexample"))
      (populate-example-graph! @g))))

;;; Request handlers ====================================================

(defn all-nodes
  ([]
     (response (vec (get-all-nodes-details @g))))
  ([rank limit]
     (let [rank-keyword
           (case rank
             "closeness" :closeness
             "score" :score
             nil)]
       (if (not= rank-keyword nil)
         ;; 200 (Ok)
         (response (get-all-nodes-details-ranked @g rank-keyword limit))
         ;; 400 (bad request)
         (status (response "Invalid rank type") 400)))))

(defn add-edge [n1 n2]
  (create-edge! @g n1 n2)
  ;; 201 (created)
  (status (response "Edge created") 201))

(defn update-node [params]
  (set-fraudulent-status @g (:id params) (= "true" (:is-fraudulent? params)))
  (status (response "Marked!") 200))
