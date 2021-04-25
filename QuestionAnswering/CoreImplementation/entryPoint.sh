#!/bin/sh
nohup bash -c "java -mx4g -cp '*' edu.stanford.nlp.pipeline.StanfordCoreNLPServer -port 9000 -timeout 15000 &" && sleep 4
/bin/bash -c
