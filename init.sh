#!/bin/bash

ES_HOST=data01
#ES_HOST=mcrk-docker-1
ES_URL=http://$ES_HOST:9200
echo Hostname: $ES_HOST

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Wait for Elasticsearch to start up before doing anything.
until curl -s ${ES_URL}/_cat/health -o /dev/null; do
    echo -e ${YELLOW}Waiting for Elasticsearch...${NC}
    sleep 3
done
echo -e ${GREEN}Elasticsearch is up${NC}

# Wait for Kibana to start up before doing anything.
until curl -s http://$ES_HOST:5601/ -o /dev/null; do
    echo -e ${YELLOW}Waiting for Kibana...${NC}
    sleep 3
done
echo -e ${GREEN}Kibana is up${NC}

echo Loading cluster.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_cluster/settings -d@cluster.json
echo -e \\nLoading pmon_ilm_policy.json
curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_ilm/policy/pmon_ilm_policy -d@pmon_ilm_policy.json
echo -e \\n${GREEN}DONE${NC}

java -jar -Dlogback.configurationFile=logback.xml cdrpr-2.0-jar-with-dependencies.jar


