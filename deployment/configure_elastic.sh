#!/usr/bin/env bash

# Usage:
# sh configure_elastic.sh <hostname>

ES_HOST=$0
#ES_HOST=mcrk-docker-1
ES_URL=http://$ES_HOST:9200
echo $ES_HOST

# Wait for Elasticsearch to start up before doing anything.
until curl -s ${ES_URL}/_cat/health -o /dev/null; do
    echo Waiting for Elasticsearch...
    sleep 3
done


# Wait for Kibana to start up before doing anything.
until curl -s http://$ES_HOST:5601/ -o /dev/null; do
    echo Waiting for Kibana...
    sleep 3
done

# Load the settings for ILM
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_cluster/settings -d@cluster.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_ilm/policy/pmon_ilm_policy -d@pmon_ilm_policy.json


