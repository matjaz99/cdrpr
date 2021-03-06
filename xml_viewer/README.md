# XML viewer

## Prepare Linux (CentOS)

Disable or minimize swapping

```
$ sysctl vm.swappiness=1
$ echo "vm.swappiness=1" >> /etc/sysctl.conf
```

Set the number of open file handles

Check current settings with ulimit -a; then set:

```
$ ulimit -n 65535
```

To make ulimit permanent, edit file:

```
$ vi /etc/security/limits.conf
// add at the end
*                hard    nofile          65535
*                soft    nofile          65535
```

Increase the limits of mmapfs to store its indices:

```
$ sysctl -w vm.max_map_count=262144
$ echo "vm.max_map_count=262144" >> /etc/sysctl.conf
```

Make sure that the number of threads that the Elasticsearch user can create is at least 4096. 
This can be done by setting ulimit -u 4096 as root before starting Elasticsearch, or by setting nproc to 4096 in /etc/security/limits.conf.


Set java heap size for Elasticsearch via environment variable (this step is optional, unless you are an expert):

```
ES_JAVA_OPTS=-Xms2g -Xmx2g
```


## Install and configure ElasticSearch and Kibana

Untar ElasticSearch and Kibana

```
$ tar -zxf elasticsearch-7.12.1-linux-x86_64.tar.gz
$ tar -zxf kibana-7.12.1-linux-x86_64.tar.gz
```

Configure ElasticSearch:

```
$ vi config/elasticsearch.yml

node.name: node-1
network.host: 0.0.0.0
discovery.seed_hosts: ["localhost"]
cluster.initial_master_nodes: ["node-1"]
```

Configure Kibana:

```
$ vi config/kibana.yml

# listen on all IP addresses
server.host: "0.0.0.0"
# urls of Elasticsearch instances
elasticsearch.hosts: ["http://localhost:9200"]
# kibana server name
server.name: "my-kibana"
```


### Create non-root user

Elastic search can only be run as non-root user. Create new user if needed:

```
$ adduser elasticsearch
$ passwd elasticsearch
```

And change ownership of Elastiscearch and Kibana directory:

```
$ chown -R elasticsearch:elasticsearch elasticsearch-7.12.1
$ chown -R elasticsearch:elasticsearch kibana-7.12.1-linux-x86_64
```


### Start ElasticSearch and Kibana

Before running ElasticSearch and Kibana, login as new user!

```
$ sh start_elastic.sh
```

This script will run ElasticSearch and Kibana, and create cluster config and ilm policy.

If necessary configure `ES_JAVA_HOME` and `ES_JAVA_OPTS`.

> Remark: For optimal performance Java heap memory should be maximum half of the total VM memory.


## Configure xmlViewer

Before starting XML-viewer, some basic configuration must be done. All configuration is stored in a file 
`xml_viewer.properties`.

Here are most important parameters to configure:

```properties
metricslib.jetty.port=9099
metricslib.jetty.pathPrefix=/
metricslib.client.retry.count=0
metricslib.client.retry.interval.millis=1500
metricslib.client.bulk.size=10000
metricslib.dump.enabled=true
metricslib.dump.directory=dump/
metricslib.upload.interval.seconds=25
metricslib.prometheus.enable=true
metricslib.prometheus.include.registry=_all
metricslib.prometheus.exclude.registry=
metricslib.elasticsearch.default.schema=http
metricslib.elasticsearch.default.host=localhost
metricslib.elasticsearch.default.port=9200
metricslib.elasticsearch.healthcheck.interval.seconds=150
metricslib.elasticsearch.createIndexOnStart=true
metricslib.elasticsearch.numberOfShards=1
metricslib.elasticsearch.numberOfReplicas=0
metricslib.elasticsearch.ilm.policy.name=metrics_ilm_policy
xmlviewer.parser.interval.seconds=60
xmlviewer.parser.input.dir=xml_input_dir
xmlviewer.parser.output.dir=xml_processed_dir
xmlviewer.file.retention.hours=168
```




## Run xmlViewer




## TODO

Run as service
Installation package
History of processed XMLs


## Read

https://www.elastic.co/blog/elasticsearch-as-a-time-series-data-store

https://www.elastic.co/blog/querying-and-aggregating-time-series-data-in-elasticsearch


