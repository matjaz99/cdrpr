#!/bin/bash

ES_URL=http://localhost:9200
echo URL: $ES_URL

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
#until curl -s http://$ES_HOST:5601/ -o /dev/null; do
#    echo -e ${YELLOW}Waiting for Kibana...${NC}
#    sleep 3
#done
#echo -e ${GREEN}Kibana is up${NC}

echo Loading cluster.json
curl -s -H 'Content-Type: application/json' -u admin:admin -XPUT ${ES_URL}/_cluster/settings -d@cluster.json
echo -e \\nLoading pmon_ilm_policy.json
curl -s -H 'Content-Type: application/json' -u admin:admin -XPUT ${ES_URL}/_ilm/policy/pmon_ilm_policy -d@pmon_ilm_policy.json
echo -e \\n${GREEN}DONE${NC}
