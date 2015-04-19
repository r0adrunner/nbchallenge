(ns nbgraph.model.graphutils-test
  (:require [clojure.test :refer :all]
            [nbgraph.model.graphmodel :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.mongograph-test :refer :all]
            [nbgraph.model.graphutils :refer :all]))

(use-fixtures :once wrap-mongo-setup)

(def mongo-graph-name "graphutils-test")

(defn- startup []
  (def g (create-graph! mongo-graph-name))
  (create-edge! g "2" "1")
  (create-edge! g "3" "1")
  (create-edge! g "4" "2")
  (create-edge! g "2" "5"))

(defn- teardown []
  (delete-graph! g))

(deftest breadth-first-search-test
  (startup)

  (is (= #{{:node "1" :distance 1}
           {:node "2" :distance 0}
           {:node "3" :distance 2}
           {:node "4" :distance 1}
           {:node "5" :distance 1}}
         (set (breadth-first-search g "2"))))

  (teardown))
