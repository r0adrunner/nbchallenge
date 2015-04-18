(ns nbgraph.model.graphutils
  (:require [nbgraph.model.graphmodel :refer :all]))

(defn breadth-first-search
  "Returns a lazy sequence of node info maps resulted from a breadth first traversal of graph g
   starting at the 'start' node.
   Each map contains a 'node' with the node name and a 'distance' key,
   referring to the distance from the start node."
  [g start]
  ((fn bfsaux [queue visited]
     (lazy-seq
      (when-let [node-info (peek queue)]
        (let [unvisited-neighbors (remove visited (neighbors g (:node node-info)))]
          (cons node-info
                (bfsaux (into (pop queue)
                              (map #(hash-map :node % :distance (inc (:distance node-info)))
                                   unvisited-neighbors))
                        (into (conj visited (:node node-info)) unvisited-neighbors)))))))
   (conj (clojure.lang.PersistentQueue/EMPTY) {:node start :distance 0}) #{}))
