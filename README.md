# Nearest Neighbor In Graphs

Implementation for the paper [Reactive Proximity Data Structures for Graphs](https://arxiv.org/pdf/1803.04555.pdf)

This project considers the classic question of finding nearest neighbors in a graph-based setting.
It implements a data structure that maintains a subset of the nodes of a graph, called sites, and allow the following operations:
- Updates: add or remove a node from the set of sites.
- Queries: given a query node, return the closest site to the query node.

The data structure is based on a [separator hierarchy](https://en.wikipedia.org/wiki/Planar_separator_theorem).


