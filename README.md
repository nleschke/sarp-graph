# sarp-graph
This repository provides a protoypical implementation of a SARP-graph converter, analyzer, integration and visualization.

The code accompanies the paper "Alohomora! Facilitating Personal Data Access
Through Automated Graph Extraction and Integration".

Results, in particular the visualizations can be found under `validation\results`.

## Requirements
Java 17, Maven

In order to use OpenNLP, download the models by calling `download_models.sh`

## Build & Run
- install requirements
- create demo dir if not exists
- run Main.java

## Visualization

Visualize the Graph using [GraphViz](https://graphviz.org).
Create a demo dir, the default method will write a .dot file containing graph data

```sh
dot demo.dot -Tsvg > demo.svg
```

