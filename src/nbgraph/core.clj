(ns nbgraph.core
  (:require [nbgraph.initdb :refer :all]))

(defn -main
  "Main function. Call it with args 'initdb' to initialize the
   example graph"
  [& args]
  (when (= (first args) "initdb")
    (init-example-db)))
