#!/bin/bash

ES_HOST=localhost
ES_URL=http://$ES_HOST:9200
echo URL: $ES_URL

export ES_JAVA_HOME=$JAVA_HOME
export ES_JAVA_OPTS="-Xms4g -Xmx4g"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

HOME=$(pwd)

cd $HOME/elasticsearch-7.12.1/bin
./elasticsearch &

# Wait for Elasticsearch to start up before doing anything.
until curl -s ${ES_URL}/_cat/health -o /dev/null; do
    echo -e ${YELLOW}Waiting for Elasticsearch...${NC}
    sleep 3
done
echo -e ${GREEN}Elasticsearch is up${NC}

#cd $HOME/kibana-7.12.1-linux-x86_64/bin
#./kibana &
#
# Wait for Kibana to start up before doing anything.
#until curl -s http://$ES_HOST:5601/ -o /dev/null; do
#    echo -e ${YELLOW}Waiting for Kibana...${NC}
#    sleep 3
#done
#echo -e ${GREEN}Kibana is up${NC}

cd $HOME
#echo Loading cluster settings
#curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_cluster/settings -d@cluster.json
#echo -e \\nLoading ILM policy
#curl -s -H 'Content-Type: application/json' -XPUT ${ES_URL}/_ilm/policy/metrics_ilm_policy -d@metrics_ilm_policy.json
echo -e \\n${GREEN}DONE${NC}