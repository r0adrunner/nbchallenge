(ns nbgraph.model.graphmodel-test
  (:require [clojure.test :refer :all]
            [nbgraph.model.dbutils.mongodbconfig :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.graphmodel :refer :all]
            [nbgraph.model.graphutils :refer :all])
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

  (testing "Flagging a node as fraudulent"
    (set-fraudulent-status @g "2" true)
    (set-fraudulent-status @g "5" true)
    (is (= (get-fraudulent-nodes @g) #{"2" "5"})))

  (testing "Closeness test"
    (is (= (closeness @g "2") 0.2))
    (is (= (closeness @g "5") 0.125)))

  (testing "Score test"
    (is (= (score @g "2") 0))
    (is (= (round (score @g "3")) 0.0729))
    (is (= (round (score @g "1")) 0.0625))
    (is (= (score @g "5") 0)))

  (testing "Nodes details test"
    (is (=
         #{{:score 0.0625, :closeness 0.1667, :neighbors #{"3" "2"}, :id "1", :is-fraudulent? false}
           {:score 0, :closeness 0.125, :neighbors #{"2"}, :id "5", :is-fraudulent? true}
           {:score 0.0729, :closeness 0.1111, :neighbors #{"1"}, :id "3", :is-fraudulent? false}
           {:score 0.0469, :closeness 0.125, :neighbors #{"2"}, :id "4", :is-fraudulent? false}
           {:score 0, :closeness 0.2, :neighbors #{"4" "5" "1"}, :id "2", :is-fraudulent? true}}
         (get-all-nodes-details @g))))

  (testing "Closeness rank test"
    (is (= [{:score 0, :closeness 0.2, :neighbors #{"4" "5" "1"}, :id "2", :is-fraudulent? true}
            {:score 0.0625, :closeness 0.1667, :neighbors #{"3" "2"}, :id "1", :is-fraudulent? false}
            {:score 0.0469, :closeness 0.125, :neighbors #{"2"}, :id "4", :is-fraudulent? false}]
           (get-all-nodes-details-ranked @g :closeness 3))))

  (testing "Score rank test"
    (is (= [{:score 0.0729, :closeness 0.1111, :neighbors #{"1"}, :id "3", :is-fraudulent? false}
            {:score 0.0625, :closeness 0.1667, :neighbors #{"3" "2"}, :id "1", :is-fraudulent? false}
            {:score 0.0469, :closeness 0.125, :neighbors #{"2"}, :id "4", :is-fraudulent? false}]
           (get-all-nodes-details-ranked @g :score 3)))))

