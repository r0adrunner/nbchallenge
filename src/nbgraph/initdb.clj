(ns nbgraph.initdb
  (:require [nbgraph.dbutils.mongodbconfig :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.graphmodel :refer :all]))

(defn init-example-db []
  (connect! :production)
  (def g (create-graph! "nbexample"))
  (with-open [rdr (clojure.java.io/reader "resources/edges")]
    (doall (map #(let [edges (clojure.string/split % #" ")]
                   (create-edge! g (first edges) (second edges)))
                (line-seq rdr))))
  (closeness g (first (get-nodes g)))
  (println "Init DB ok."))
