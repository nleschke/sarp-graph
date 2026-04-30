# SARP-Graph
This repository provides a protoypical implementation of a SARP-graph converter, analyzer, integration and visualization.

The code accompanies the paper "Alohomora! Facilitating Personal Data Access Through Automated Graph Extraction and Integration", accepted for the IWPE 2026.

## Requirements
For running the code, you need Java 17 (and Maven for developing).

In order to use OpenNLP for the named entity recognition, download the models by calling `download_models.sh`

## Build & Run
- install requirements
- create demo dir if not exists (and wanted)
- run Main.java

## Structure of this repository
This repository is mainly structured into the source files and the resulting artifacts, stored in the validation directory.
### Visualizations referenced in the paper
Results, in particular the visualizations can be found under `validation\results`, including svg files that make it easier to explore the full graphs.
### Source Code
We offer three components: the (file) readers, the (graph) analyzers and the (graph) visualizer, structured in packages. These packages are accompanied by helper packages defining the (SARP) graph structures, offering file utils and errors.
