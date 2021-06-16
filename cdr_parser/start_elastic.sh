#!/bin/bash

ES_HOST=localhost
ES_URL=http://$ES_HOST:9200
echo ES_URL: $ES_URL

export ES_JAVA_HOME=$JAVA_HOME
export ES_JAVA_OPTS="-Xms4g -Xmx4g"

ulimit -u 4096

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

HOME=$(pwd)

cd $HOME/elasticsearch-7.12.1/bin
./elasticsearch -d -p $HOME/elasticsearch.pid

# Wait for Elasticsearch to start up before doing anything.
until curl -s ${ES_URL}/_cat/health -o /dev/null; do
    echo -e ${YELLOW}Waiting for Elasticsearch...${NC}
    sleep 3
done
echo -e ${GREEN}Elasticsearch is up${NC}

mkdir -p $HOME/kibana-7.12.1-linux-x86_64/log
cd $HOME/kibana-7.12.1-linux-x86_64/bin
./kibana &

# Wait for Kibana to start up before doing anything.
until curl -s http://$ES_HOST:5601/ -o /dev/null; do
    echo -e ${YELLOW}Waiting for Kibana...${NC}
    sleep 3
done
echo -e ${GREEN}Kibana is up${NC}

echo -e \\n${GREEN}DONE${NC}
