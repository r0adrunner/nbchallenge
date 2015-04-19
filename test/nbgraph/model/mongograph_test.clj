(ns nbgraph.model.mongograph-test
  (:require [clojure.test :refer :all]
            [nbgraph.model.dbutils.mongodbconfig :refer :all]
            [nbgraph.model.mongograph :refer :all]
            [monger.collection :as mc]))

(defn wrap-mongo-setup
  "Fixture used to setup and teardown a mongodb testing environment. "
  [f]
  (connect! :testing)
  (f))

(deftest mongo-connectivity
  (connect! :testing)
  (is (connected?)))
