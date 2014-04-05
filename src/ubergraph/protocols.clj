(ns ubergraph.protocols)

(defprotocol Graph
  (nodes [g] "Return a collection of the nodes in graph g")
  ; Add ability to enumerate all edges between n1 and n2
  (edges [g] [g n1 n2] "Edges in g. May return each edge twice in an undirected graph
                        (edges g n1 n2) returns all edges from n1 to n2")
  (has-node? [g node] "Return true when node is in g")
  (has-edge? [g n1 n2] "Return true when an edge from n1 to n2 is in g")
  (successors [g] [g node] "Return direct successors of node, or (partial successors g)")
  (out-degree [g node] "Return the number of direct successors of node")
  ; new
  (out-edges [g] [g node] "Return outgoing edges of node, or (partial out-edges g)"  )
  )

; Note that with the above extension to the edges protocol function,
; has-edge? can now be implemented as (seq (edges g n1 n2)).
; It doesn't necessarily need to be part of the Graph protocol.

; Still don't like the name Digraph for this, maybe Bigraph would be better
(defprotocol Digraph
  (predecessors [g] [g node] "Return direct predecessors of node, or (partial predecessors g)")
  (in-degree [g node] "Return the number direct predecessors to node")  
  (transpose [g] "Return a graph with all edges reversed")
  ; new
  (in-edges [g] [g node] "Return the incoming edges of node, or (partial in-edges g)")  
)
  
; Loom currently hardcodes the notion of edge to be a [node1 node2] pair.
; This is not good enough for multigraphs with weights and attributes.
; An Edge is something from which you can get the src and dest.
; Let's represent this as a protocol, so we are not tied to a specific representation.
; That way, we have the option to store richer information in the edge if we so choose.
  
; NEW PROTOCOL
(defprotocol Edge
  (src [edge] "Return the source end of the edge")
  (dest [edge] "Return the destination end of the edge"))

; Modify the weight protocol function to take an edge
; (weight n1 n2) remains for backwards compatibility, but would be discouraged
; since algorithms using that form won't work properly with multigraphs.
; Or scrap the WeightedGraph protocol altogether (see my comment at the bottom of the file)

(defprotocol WeightedGraph
  (weight [g] [g e] [g n1 n2] 
       "Return weight of edge e, or arbitrary edge from n1 to n2, or (partial weight g)"))

(defprotocol EditableGraph
  (add-nodes* [g nodes] "Add nodes to graph g. See add-nodes")
  (add-edges* [g edges] "Add edges to graph g. See add-edges")
  (remove-nodes* [g nodes] "Remove nodes from graph g. See remove-nodes")
  (remove-edges* [g edges] "Removes edges from graph g. See remove-edges")
  (remove-all [g] "Removes all nodes and edges from graph g"))

; Modify AttrGraph protocol to also take edge
(defprotocol AttrGraph
  (add-attr [g node-or-edge k v] [g n1 n2 k v] "Add an attribute to node or edge")
  (remove-attr [g node-or-edge k] [g n1 n2 k] "Remove an attribute from a node or edge")
  (attr [g node-or-edge k] [g n1 n2 k] "Return the attribute on a node or edge")
  (attrs [g node-or-edge] [g n1 n2] "Return all attributes on a node or edge"))

; NEW CONCEPTS

; For undirected graph algorithms, it is useful to know the connection between the
; two sides of an undirected edge, so we can keep attributes and weight in sync
; and add/remove or otherwise mark them in sync.
(defprotocol UndirectedEdge
  (reverse-edge [g edge] "Returns the other direction of this edge in graph g"))

; We need a way to retrieve edges that is friendly to both regular and multi-graphs,
; but especially important for multi-graphs.
(defprotocol Query
  (find-edges [g query] "Returns all edges that match the query")
  (find-edge [g query] "Returns first edge that matches the query"))

; Sample queries
; (find-edges g {:src 1, :dest 3, :color :red})  
;    finds all edges from 1 to 3 with :color :red in edge attributes
;
; (find-edges g {:src 1, :weight 5})
;    finds all edges from 1 with a weight of 5
;
; (find-edges g {:dest :Chicago, :airline :Delta, :time :afternoon})
;    finds all edges leading to :Chicago with :airline :Delta and 
;    :time :afternoon in edge attributes
