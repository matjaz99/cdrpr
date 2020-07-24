# MetricsLib

MetricsLib is swiss knife for aggregating metrics and pushing them to various data storage.

MetricsLib supports pushing metrics to:
- ElasticSearch
- Postgres
- Prometheus
- InfluxDB (not supported yet)

With further integration, metrics can be presented on dashboards in Kibana and Grafana.

The main purpose of MetricsLib is to provide developers a simple model for collecting metrics, without the 
required knowledge of how ElasticSearch works and how to get the data there and what api to use.
MetricsLib will do it for them, developers just need to know how to count.

## Quick start

Let's collect some metrics in Java.

First initialize the MetricsLib (this will start Jetty server on port 9099):

```
MetricsLib.init();
```

If you want to start Jetty on different port, then use `MetricsLib.init(8080);` method.

Then register a metric (set its name, set short description and add label names):

```
public static PMetric icecream_sold_total = PMetric.build()
    .setName("icecream_sold_total")
    .setHelp("Count sold icecreams by taste and quantity")
    .setLabelNames("taste", "quantity")
    .register();
```

and start counting:

```
for (Icecream i : iceList) {
    icecream_sold_total.setLabelValues(i.getTaste(), i.getSize()).inc();
}
```

The `PMetric` object will automatically create new time-series point and set the value for each combination 
of labels.

The `inc()` method increases the value of metric by 1. You can also increase the value by a certain amount with 
method `inc(5)`. In such way, the metric behaves as a Counter whose value always increases.
The value can also be set to desired value with `set(3)`. This way the metric can behave as a Gauge.
This will be important later when creating visualizations in Grafana or Kibana, so use the metrics properly and 
never mix the `inc()` and `set()` method on the same metric.

> The data type of metric value is `double`.

So far we filled the metrics (time series values). Now we need to push them to DB.
By default, all metrics are exposed in prometheus format on the URI endpoint `/metrics` waiting to be scraped by Prometheus.

Send metric into ElasticSearch:

```
EsClient es = new EsClient(urlEndpoint);
es.sendBulkPost(icecream_sold_total);
```

where `urlEndpoint` is pointing to bulk api of selected index (eg. `http://elasticvm:9200/icecream/_bulk`).

Let's quickly insert the same metric into Postgres:

```
PgClient pg = new PgClient(urlEndpoint, user, pass);
pg.createTable(icecream_sold_total);   // only first time!
pg.sendBulk(icecream_sold_total);
```

where `urlEndpoint` is pointing to postgres on `jdbc:postgresql://elasticvm:5432/icecream`.

> Currently, the PgClient does not provide any checks if tables exist, what to do if label 
names are changing or being added, because PgClient does not do ALTER TABLE. The table must be dropped manually in such cases.
This will be fixed someday and somehow. PgClient is still a prototype serving as a PoC.


## Metrics model

MetricsLib offers a simple model for creating metrics and filling the time-series data.

Each metric consists of:
- metric name
- help
- list of labels
- timestamp
- time-series points and their values

A metric name should reflect what kind of data metric contains.

Help provides details about the metric; description what is being measured.

Labels are a map of key-value pairs. Labels describe circumstances when the metric was taken.

Timestamp is UNIX timestamp in milliseconds when metric has been sampled.

Example (in prometheus format):

```
icecream_sold_total{"taste"="chocolate", "quantity"="big"} 10
icecream_sold_total{"taste"="chocolate", "quantity"="small"} 20
icecream_sold_total{"taste"="strawberry", "quantity"="big"} 5
icecream_sold_total{"taste"="strawberry", "quantity"="small"} 15
icecream_sold_total{"taste"="vanilla", "quantity"="big"} 10
```

You can see that metric name is the same in all cases, also label names are the same in all time-series points. 
It's the label values that are changing and each time-series point has its own value.

If you know the number of possible values for each label, then you can estimate the final number of time-series 
points that will be written in the DB.

> Increasing the cardinality of metric by adding more labels, will make the time-series points grow exponentially.



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

For each metric a table will be created in Postgres (with the same name as the metric name) with columns 
for each label and two more for a timestamp and value.

```roomsql
CREATE TABLE cdr_calls_by_cause_count (node VARCHAR, cause VARCHAR, subscriberGroup VARCHAR, timestamp BIGINT, value NUMERIC);
```

Time series will be then inserted in bulks as rows.

````roomsql
INSERT INTO cdr_calls_by_cause_count (node, cause, subscriberGroup, timestamp, value) VALUES (Node1, Answered, sg1, 1234567890, 100);
````

### Prometheus metric format

The `PMetric` model actually encapsulates the `Gauge` object from Prometheus library which enables the 
MetricsLib to expose metrics in Prometheus format on URI endpoint `/metrics`.

```
cdr_calls_by_cause_count{"node"="Node1", "cause"="Answered", "subscriberGroup"="sg1"} 100
```

Timestamp is omitted here because Prometheus will append it when metric will be scraped.

> You can still use `Counter`, `Gauge` and `Histogram` objects directly from java prom client. They will be exposed too.

### InfluxDB metric format

TODO not implemented yet


## Configuration

MetricsLib can be configured with properties file, with environment variables or inline in the code.

Properties file is used for configuring MetricsLib (if exists). Configuration will be 
then overwritten with environment variables (if they exist), or they can be overwritten anytime 
in the code.



## Registry

MetricsLib supports many registries for groups of metrics, each may serve a different purpose.
All metrics are stored in the `default` registry if no registry is specified when registering metric.

```
public static PMetric icecream_sold_total = PMetric.build()
    .setName("icecream_sold_total")
    .setHelp("Count sold icecreams by taste and quantity")
    .setLabelNames("taste", "quantity")
    .register("iceRegistry");
```



## Exporter

MetricsLib exposes internal metrics to Prometheus:

[http://hostname:9099/metrics](http://hostname:9099/metrics)




