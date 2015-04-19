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
      (is (= (:status response) 200))))

  (testing "Get all nodes"
    (let [response (app (mock/request :get "/nodes"))]
      (is (= (:status response) 200))
      (is (= (set
              (map
               #(assoc % :neighbors (set (:neighbors %)))
               (parse-string (:body response) true)))
             #{{:closeness 0.125, :neighbors #{"2"}, :id "4"}
               {:closeness 0.1111111111111111, :neighbors #{"1"}, :id "3"}
               {:closeness 0.1666666666666667, :neighbors #{"3" "2"}, :id "1"}
               {:closeness 0.2, :neighbors #{"4" "5" "1"}, :id "2"}
               {:closeness 0.125, :neighbors #{"2"}, :id "5"}}))))

  (testing "Get all nodes ranked"
    (let [response (app (mock/request :get "/nodes/rank-by/closeness/limit/3"))]
      (is (= (:status response) 200))
      (is (= (map
              #(assoc % :neighbors (set (:neighbors %)))
              (parse-string (:body response) true))
             '({:closeness 0.2, :neighbors #{"4" "1" "5"}, :id "2"}
               {:closeness 0.1666666666666667, :neighbors #{"2" "3"}, :id "1"}
               {:closeness 0.125, :neighbors #{"2"}, :id "4"})))))

  (teardown)
  
  )
