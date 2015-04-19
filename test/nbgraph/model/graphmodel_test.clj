(ns nbgraph.model.graphmodel-test
  (:require [clojure.test :refer :all]
            [nbgraph.model.dbutils.mongodbconfig :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.graphmodel :refer :all])
  (:import [nbgraph.model.mongograph MongoGraph]))

(declare test-mongo)
(declare test-imp)

(def mongo-graph-name "g-model-test")
(def g)

(defn test-ns-hook
  "(run-tests) calls this function when it is defined. Used when there is composition of tests"
  []
  ;; Add each graph implementation here:
  (test-mongo))

;;; You can create one function like this for each graph implementation,
;;; and call test-imp to run the tests on them
(defn- test-mongo
  []
  (connect! :testing)
  (def g (create-graph! mongo-graph-name))
  (test-imp)
  (delete-graph! g))

(deftest test-imp
  (testing "Add and retrieve node"
    (create-node-if-not-exists! g "1")
    (is (= (get-nodes g) #{"1"})))

  (testing "Edges and neighbors test"
    (create-edge! g "2" "1")
    (create-edge! g "3" "1")
    (create-edge! g "4" "2")
    (create-edge! g "2" "5")
    (is (= (neighbors g "2") #{"4" "1" "5"}))
    (is (= (neighbors g "3") #{"1"})))

  (testing "Closeness test"
    (is (= (closeness g "2") 0.2))
    (is (= (closeness g "5") 0.125)))

  (testing "Rank test"
    (is (= ["2" "1" "4" "5" "3"] (get-nodes-ranked g :closeness 5)))))

