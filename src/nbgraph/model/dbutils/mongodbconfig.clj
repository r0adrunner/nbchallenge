(ns nbgraph.model.dbutils.mongodbconfig
  (:require [monger core]))

(defonce connected (atom false))

(defn connected?
  []
  @connected)

(defn connect!
  "Connects to mongoDB. Accepted parameters are :testing or :production"
  [mode]
  (when-not (connected?)
    (do
      (case mode

        :testing
        ;; localhost, default port:
        (do (monger.core/connect! {:host "127.0.0.1" :port 27017})
            (monger.core/set-db! (monger.core/get-db "nbgraph_test")))

        :production
        ;; localhost, default port:
        (do (monger.core/connect! {:host "127.0.0.1" :port 27017})
            (monger.core/set-db! (monger.core/get-db "nbgraph_prod"))))
      
      (reset! connected true))))
