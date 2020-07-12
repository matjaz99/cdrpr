# MetricsLib

MetricsLib is swiss knife for aggregating metrics and pushing them to various data storage.

MetricsLib supports pushing metrics to:
- ElasticSearch
- Postgres
- Prometheus
- InfluxDB (not supported yet)

Each of these data storage enable presentation of metrics in Kibana or Grafana.

## Metrics model

MetricsLib offers a simple model for creating metrics and filling the time-series data.

Each metric consists of:
- metric name
- help
- list of labels
- timestamp
- values for each time-series

Metric name should reflect what kind of data metric contains.

Help should provide more details about the metric; description what is being measured.

Labels are a map of key-value pairs. Labels describe circumstances when the metric was taken.

Timestamp is UNIX timestamp in milliseconds when metric was recorded.

### ElasticSearch metric format

```json
{
  "metric_name": "cdr_calls_by_cause_count",
  "node": "Node1",
  "cause": "Answered",
  "subscriberGroup": "sg1",
  "timestamp": 1234567890,
  "value": 100
}
```

### Postgres metric format

MetricsLib will convert metric to format suitable for Postgres and automatically create a new 
table for each metric:

```sql
CREATE TABLE cdr_calls_by_cause_count (node VARCHAR, cause VARCHAR, subscriberGroup VARCHAR, timestamp BIGINT, value NUMERIC);
```

Time series will be then inserted in bulks as rows.

````sql
INSERT INTO cdr_calls_by_cause_count (node, cause, subscriberGroup, timestamp, value) VALUES (Node1, Answered, sg1, 1234567890, 100);
````

### Prometheus metric format

The same metric can be converted to Prometheus format and exposed via embedded exporter.

```
cdr_calls_by_cause_count{"node"="Node1", "cause"="Answered", "subscriberGroup"="sg1"} 100
```

Timestamp is omitted here because Prometheus will append it when metric will be scraped.


### InfluxDB metric format

TODO not implemented yes

### Cardinality

The problem with time-series data could be high cardinality of metrics.
It depends also on number of possible values for each label.

If you have 3 labels and each label has 100 possible values, then you might get 1 million metrics (time series).
Adding one more possible value doesn't increase number of time series as much as adding new label 
would cause exponential growth of time series.

> Always do estimation how many metrics to expect! How many combinations, how frequent?

## Configuration

MetricsLib can be configured with properties file, with environment variables or inline in the code.

Properties file is used for configuring MetricsLib (if exists). Configuration will be 
then overwritten with environment variables (if they exist), or they can be overwritten anytime 
in the code.



## Registry

MetricsLib supports many registries, each may serve a different purpose.
By default if no registry is specified when registering metric, all metrics are stored in `default` registry.


## Instructions for developers (Java)

First register a metric:

```
public static PMetric cdr_calls_by_cause_count = PMetric.build()
    .setName("cdr_calls_by_cause_count")
    .setHelp("Count calls by release cause")
    .setLabelNames("node", "cause", "subscriberGroup")
    .register();
```

Fill the metric:

```
cdr_calls_by_cause_count
    .setLabelValues(node, cause, subscriberGroup)
    .inc();
```

In background new time-series is created for this combination of labels and the value is increased by 1.



## Exporter

MetricsLib exposes internal metrics to Prometheus:

[http://hostname:9099/metrics](http://hostname:9099/metrics)




