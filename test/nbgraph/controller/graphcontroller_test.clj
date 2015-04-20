(ns nbgraph.controller.graphcontroller-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [nbgraph.model.graphmodel :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [nbgraph.model.mongograph-test :refer :all]
            [nbgraph.controller.graphcontroller :refer :all]
            [nbgraph.controller.handler :refer :all]
            [cheshire.core :refer :all]))

(use-fixtures :once wrap-mongo-setup)

(def mongo-graph-name "graphcontroller-test")

(defn- startup []
  (reset! g (create-graph! mongo-graph-name))
  (create-edge! @g "2" "1")
  (create-edge! @g "3" "1")
  (create-edge! @g "4" "2"))

(defn- teardown []
  (delete-graph! @g))

(deftest graphcontroller-test
  (startup)

  (testing "Adding an edge"
    (let [response (app (mock/body
                         (mock/request :post "/edge")
                         {:node1 "2" :node2 "5"}))]
      (is (= (:status response) 201))))

  (testing "Marking nodes as fraudulent"
    (let [response1 (app (mock/body
                         (mock/request :put "/node")
                         {:id 2 :isfraudulent true}))
          response2 (app (mock/body
                         (mock/request :put "/node")
                         {:id 5 :isfraudulent true}))]
      (is (= (:status response1) 200))
      (is (= (:status response2) 200))))

  (testing "Get all nodes"
    (let [response (app (mock/request :get "/nodes"))]
      (is (= (:status response) 200))
      (is (= (set
              (map
               #(assoc % :neighbors (set (:neighbors %)))
               (parse-string (:body response) true)))
             #{{:is-fraudulent? false, :score 0.0625, :closeness 0.1667, :neighbors #{"3" "2"}, :id "1"}
               {:is-fraudulent? false, :score 0.0729, :closeness 0.1111, :neighbors #{"1"}, :id "3"}
               {:is-fraudulent? true, :score 0, :closeness 0.2, :neighbors #{"4" "5" "1"}, :id "2"}
               {:is-fraudulent? true, :score 0, :closeness 0.125, :neighbors #{"2"}, :id "5"}
               {:is-fraudulent? false, :score 0.0469, :closeness 0.125, :neighbors #{"2"}, :id "4"}}))))

  (testing "Bad request"
    (let [response (app (mock/request :get "/nodes/rank-by/invalid-type/limit/3"))]
      (is (= (:status response) 400))))

  (testing "Get all nodes ranked by closeness"
    (let [response (app (mock/request :get "/nodes/rank-by/closeness/limit/3"))]
      (is (= (:status response) 200))
      (is (= (map
              #(assoc % :neighbors (set (:neighbors %)))
              (parse-string (:body response) true))
             '({:is-fraudulent? true, :score 0, :closeness 0.2, :neighbors #{"4" "5" "1"}, :id "2"}
               {:is-fraudulent? false, :score 0.0625, :closeness 0.1667, :neighbors #{"3" "2"}, :id "1"}
               {:is-fraudulent? false, :score 0.0469, :closeness 0.125, :neighbors #{"2"}, :id "4"})))))

  (testing "Get all nodes ranked by score"
    (let [response (app (mock/request :get "/nodes/rank-by/score/limit/3"))]
      (is (= (:status response) 200))
      (is (= (map
              #(assoc % :neighbors (set (:neighbors %)))
              (parse-string (:body response) true))
             '({:is-fraudulent? false, :score 0.0729, :closeness 0.1111, :neighbors #{"1"}, :id "3"}
               {:is-fraudulent? false, :score 0.0625, :closeness 0.1667, :neighbors #{"3" "2"}, :id "1"}
               {:is-fraudulent? false, :score 0.0469, :closeness 0.125, :neighbors #{"2"}, :id "4"})))))

  (teardown)
  
  )
