#!/bin/bash

# docker network create --driver=overlay --attachable=true cdrsim_net

docker stack deploy -c compose-es.yml es
sleep 15
docker stack deploy -c compose-pg.yml pg
sleep 15
docker stack deploy -c compose-cdrsim.yml cdrsim


