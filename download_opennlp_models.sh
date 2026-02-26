#/bin/bash

RESDIR=src/main/resources/models
WGETPARAMS="-nc -P $RESDIR"

mkdir -p $RESDIR

wget https://dlcdn.apache.org/opennlp/models/ud-models-1.0/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin $WGETPARAMS
wget https://dlcdn.apache.org/opennlp/models/langdetect/1.8.3/langdetect-183.bin $WGETPARAMS