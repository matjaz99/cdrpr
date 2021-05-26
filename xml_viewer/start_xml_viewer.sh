#!/bin/bash

mkdir -p xml_input_dir
mkdir -p xml_processed_dir
mkdir -p dump
mkdir -p log

java -jar -Dlogback.configurationFile=logback.xml -DxmlViewer.configurationFile=xml_viewer.properties cdrpr-2.0-jar-with-dependencies.jar

