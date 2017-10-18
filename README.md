# NearestNeighborInGraphs

Implementation of a reactive nearest neighbor data structure for graphs based on separator hierarchies
The data structure maintains a subset of the nodes of the graph, called sites, and allow the following operations:
- Queries: given any node, return the closest site to that node
- Updates: add or remove a site from the set of sites

The data structure is called reactive because it allows to turn on or off nodes as sites.

