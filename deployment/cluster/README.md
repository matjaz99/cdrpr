# Elasticsearch cluster


Get cluster state:

```
curl -XGET 'http://localhost:9200/_cluster/state?pretty'
curl -XGET 'localhost:9200/_cluster/health?pretty'
curl -XGET localhost:9200/_cluster/stats?human&pretty
```

All nodes:

```
curl -XGET 'localhost:9200/_nodes/stats?pretty'
```

A specific node:
```
curl -XGET 'localhost:9200/_nodes/node-1/stats?pretty'
```

Index-only stats:

```
curl -XGET 'localhost:9200/_nodes/stats/indices?pretty'
```

You can get any of the specific metrics for any single node with the following structure:

```
curl -XGET 'localhost:9200/_nodes/stats/ingest?pretty'
```

Or multiple nodes with the following structure:

```
curl -XGET 'localhost:9200/_nodes/stats/ingest,fs?pretty'
```

Or all metrics with either of these two formats:

```
curl -XGET 'localhost:9200/_nodes/stats/_all?pretty'
curl -XGET 'localhost:9200/_nodes/stats?metric=_all?pretty'
```

Nodes Info
If you want to collect information on any or all of your cluster nodes, use this API.

Retrieve for a single node:

```
curl -XGET ‘localhost:9200/_nodes/?pretty’
```

Or multiple nodes:

```
curl -XGET ‘localhost:9200/_nodes/node1,node2?pretty’
```

Retrieve data on plugins or ingest:

```
curl -XGET ‘localhost:9200/_nodes/plugins
curl -XGET ‘localhost:9200/_nodes/ingest
```








