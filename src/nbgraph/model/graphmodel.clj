(ns nbgraph.model.graphmodel)

(defprotocol GraphModel
  "Contract for a Graph object"
  (create-node-if-not-exists! [graph node]
    "Creates 'node' if not already created")
  (get-nodes [graph]
    "Returns a set containing all the nodes of the graph")
  (get-nodes-ranked [graph rank-type limit]
    "Returns a vector containing limit number of graph nodes ranked by rank-type.
     At the moment the supported rank-types are: :closeness")
  (create-edge! [graph node1 node2]
    "Creates 'node1' and 'node2' if not already created and add an edge between them")
  (neighbors [graph node]
    "Returns a collection with all the nodes that a given node connects to")
  (closeness [graph node]
    "Returns the closeness of the given node"))
