#!/bin/bash -ex

#ES_HOST=alpinelinux
ES_HOST=mcrk-docker-1
ES_URL=http://$ES_HOST:9200

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

# Load the relevant settings for ILM
# curl -s -H 'Content-Type: application/json' -XPUT http://alpinelinux:9200/_cluster/settings -d@/opt/setup/cluster.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_cluster/settings -d@cluster.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_ilm/policy/pmon_ilm -d@ilm.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_template/pmon_template -d@template_pmon.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/pmon-000000 -d@index.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_index_template/pmon_template -d@template_mapping.json

# Load the relevant settings for SLM
#curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_snapshot/my_repository -d@/opt/setup/snapshot_repository.json
#curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_slm/policy/ten_min_snapshot -d@/opt/setup/slm.json

# Load the relevant settings for Rollups
#curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_template/rollup -d@/opt/setup/template_rollup.json
#sleep 5m # Wait until (hopefully) there is Metricbeat data, which is needed to target fields in the rollup
#curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_rollup/job/metricbeat -d@/opt/setup/rollup.json
#curl -s -H 'Content-Type: application/json' -XPOST ${ES_URL}/_rollup/job/metricbeat/_start
