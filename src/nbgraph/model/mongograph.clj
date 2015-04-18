(ns nbgraph.model.mongograph
  (:require [nbgraph.model.graphmodel :refer :all]
            [nbgraph.model.graphutils :refer :all]
            [monger.collection :as mc]
            [monger.query :as mq]))

(declare nodes-collection-ext-name)
(declare distance-matrix-collection-ext-name)
(declare set-scores-updated-status)
(declare set-distance-matrix-updated-status)
(declare node-exists?)
(declare update-scores)

;;; MongoDB based graph implementation ===================================

(defrecord MongoGraph [graph-name]
  GraphModel
  (get-nodes [this]
    (set (map #(:id %)
              (mc/find-maps
               (nodes-collection-ext-name this)
               {:id {"$exists" true}}))))

  (create-node-if-not-exists! [this node]
    ;; Don't do anything if node already exists
    (when-not (node-exists? this node)
      (mc/insert
       (nodes-collection-ext-name this)
       {:id node :is-updated-in-distance-matrix false})
      ;; Adding one node changes the scores of all others
      (set-scores-updated-status this false)
      (set-distance-matrix-updated-status this false)))

  (create-edge! [this node1 node2]
    ;; Create nodes if not exists yet
    (create-node-if-not-exists! this node1)
    (create-node-if-not-exists! this node2)
    ;; Update node's neighbors array
    ;; and change the need to update in distance
    ;; matrix status
    (mc/update
     (nodes-collection-ext-name this)
     {:id node1}
     {"$addToSet" {:neighbors node2}
      "$set" {:is-updated-in-distance-matrix false}})
    (mc/update
     (nodes-collection-ext-name this)
     {:id node2}
     {"$addToSet" {:neighbors node1}
      "$set" {:is-updated-in-distance-matrix false}})
    ;; Adding one edge changes the scores of all others
    (set-scores-updated-status this false)
    (set-distance-matrix-updated-status this false))
  
  (neighbors [this node]
    (set
     (:neighbors
      (mc/find-one-as-map
       (nodes-collection-ext-name this)
       {:id node}))))

  (closeness [this node]
    (update-scores this)
    (:closeness
     (mc/find-one-as-map
      (nodes-collection-ext-name this)
      {:id node})))

  (get-nodes-ranked
    [this rank-type limit]
    (update-scores this)
    (vec
     (map #(:id %)
          (mq/with-collection (nodes-collection-ext-name this)
            (mq/find {:id {"$exists" true}})
            (mq/sort { rank-type -1})
            (mq/limit limit))))))

;;; DB collection names ==============================================

(defn- nodes-collection-ext-name [g]
  (str "nodes-" (:graph-name g)))

(defn- distance-matrix-collection-ext-name [g]
  (str "distances-" (:graph-name g)))

;;; Auxiliary functions ==============================================

(defn- are-scores-updated? [g]
  (if-let [info-record
           (mc/find-one-as-map
            (nodes-collection-ext-name g)
            {:info true})]
    (true? (:are-scores-updated? info-record))
    false))

(defn- set-scores-updated-status [g status]
  (mc/update (nodes-collection-ext-name g)
             {:info true}
             {"$set" {:are-scores-updated? (true? status)}}
             :upsert true))

(defn- is-distance-matrix-updated? [g]
  (if-let [info-record
           (mc/find-one-as-map
            (distance-matrix-collection-ext-name g)
            {:info true})]
    (true? (:is-matrix-updated? info-record))
    false))

(defn- set-distance-matrix-updated-status [g status]
  (mc/update (distance-matrix-collection-ext-name g)
             {:info true}
             {"$set" {:is-matrix-updated? (true? status)}}
             :upsert true))

(defn- update-node-in-distance-matrix [g node]
  (doall
   (map
    #(do 
       (mc/update (distance-matrix-collection-ext-name g)
                  {:to (:node %) :from node}
                  {"$set" {:distance (:distance %)}}
                  :upsert true)
       (mc/update (distance-matrix-collection-ext-name g)
                  {:from (:node %) :to node}
                  {"$set" {:distance (:distance %)}}
                  :upsert true))
    (breadth-first-search g node)))
  ;; Mark as registered
  (mc/update (nodes-collection-ext-name g)
             {:id node}
             {"$set" {:is-updated-in-distance-matrix true}}
             :upsert false))

(defn- update-matrix [g]
  (when-not (is-distance-matrix-updated? g)
    (doall
     (map  #(update-node-in-distance-matrix g (:id %))
           (mc/find-maps
            (nodes-collection-ext-name g)
            {:is-updated-in-distance-matrix false})))
    ;; Mark as updated
    (set-distance-matrix-updated-status g true)))

(defn- distances-from [g node]
  (map #(:distance %)
       (mc/find-maps
        (distance-matrix-collection-ext-name g)
        {:from node})))

(defn- update-closeness [g node closeness]
  (mc/update (nodes-collection-ext-name g)
             {:id node}
             {"$set" {:closeness closeness}}
             :upsert false))

(defn- update-scores [g]
  (when-not (are-scores-updated? g)
    (update-matrix g)
    (doall
     (for [node (get-nodes g)]
       (let [farness (reduce + (distances-from g node))
             closeness (if (= 0 farness) 0 (/ 1 farness))]
         (update-closeness g node closeness))))
    ;; Mark as updated
    (set-scores-updated-status g true)))

;;; Graph manipulation fn's ==========================================

(defn node-exists? [g node]
  (mc/find-one
   (nodes-collection-ext-name g)
   {:id node}))

;;; Graph object ==============================================

(defn delete-graph!
  "Deletes the collections on the database related to g"
  [g]
  (mc/drop
   (nodes-collection-ext-name g))
  (mc/drop
   (distance-matrix-collection-ext-name g)))

(defn create-graph!
  "Returns a graph object with given name"
  [graph-name]
  (let [g (MongoGraph. graph-name)]
    ;; Node ID should be unique
    (mc/ensure-index (nodes-collection-ext-name g)
                     (array-map :id 1) { :unique true })
    ;; Index the distance matrix for better performance
    (mc/ensure-index (distance-matrix-collection-ext-name g)
                     (array-map :from 1))
    g))

