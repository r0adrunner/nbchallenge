(ns nbgraph.model.graphmodel-test
  (:require [clojure.test :refer :all]
            [nbgraph.model.dbutils.mongodbconfig :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.graphmodel :refer :all])
  (:import [nbgraph.model.mongograph MongoGraph]))

(declare test-mongo)
(declare test-imp)

(def mongo-graph-name "g-model-test")
(defonce g (atom nil))

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
  (reset! g (create-graph! mongo-graph-name))
  (test-imp)
  (delete-graph! @g))

(deftest test-imp
  (testing "Add and retrieve node"
    (create-node-if-not-exists! @g "1")
    (is (= (get-nodes @g) #{"1"})))

  (testing "Check if node exists"
    (is (node-exists? @g "1")))

  (testing "Edges and neighbors test"
    (create-edge! @g "2" "1")
    (create-edge! @g "3" "1")
    (create-edge! @g "4" "2")
    (create-edge! @g "2" "5")
    (is (= (neighbors @g "2") #{"4" "1" "5"}))
    (is (= (neighbors @g "3") #{"1"})))

  (testing "Closeness test"
    (is (= (closeness @g "2") 0.2))
    (is (= (closeness @g "5") 0.125)))

  (testing "Nodes details test"
    (is (= #{
             {:closeness 0.125, :neighbors #{"2"}, :id "4"}
             {:closeness 0.1111111111111111, :neighbors #{"1"}, :id "3"}
             {:closeness 0.125, :neighbors #{"2"}, :id "5"}
             {:closeness 0.1666666666666667, :neighbors #{"2" "3"}, :id "1"}
             {:closeness 0.2, :neighbors #{"5" "4" "1"}, :id "2"}}
           (get-all-nodes-details @g))))

  (testing "Rank test"
    (is (= [{:closeness 0.2, :neighbors #{"4" "1" "5"}, :id "2"}
            {:closeness 0.1666666666666667, :neighbors #{"2" "3"}, :id "1"}
            {:closeness 0.125, :neighbors #{"2"}, :id "4"}]
           (get-all-nodes-details-ranked @g :closeness 3)))))

