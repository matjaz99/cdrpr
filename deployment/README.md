# CDRAGGS in PG

https://www.postgresql.org/docs/12/index.html

visualization for PG:
https://chartio.com/product/data-sources/postgresql/


https://docs.timescale.com/latest/getting-started

## Using Postgres

Connect to PG:

```
$ psql -h 192.168.0.170 -p 5432 -U postgres -d cdraggs
```

```sql
SELECT COUNT(*) FROM public.m_countbycrc;
SELECT NOW() as time, COUNT(*) FROM public.m_countbycrc
```


## Grafana built-in macros

https://grafana.com/docs/grafana/latest/features/datasources/postgres/

Grafana provides some built-in macros that make time operations much simpler.

```sql
SELECT $__time(timestamp), COUNT(*) AS count FROM public.m_durationbytg WHERE $__unixEpochFilter("timestamp"/1000) GROUP BY time
```

`$__time(timestamp)` equals to `timestamp AS time` and timestamp is a column in milliseconds

`$__unixEpochFilter("timestamp"/1000)` function works with unix time in seconds, so you need to convert millis to 
seconds to make use of this function


## Querying metrics

Number of rows in table:

```sql
SELECT COUNT(*) FROM m_countbycrc;
```


## Aggregating by node

Get max number of calls (in single metric) on each node:

```sql
SELECT nodeid, max(value) FROM m_countbycrc GROUP BY nodeid;
```


Total number of calls on each node:

```sql
SELECT nodeid, SUM(value) FROM m_countbycrc GROUP BY nodeid;
```


## Aggregating by cause

Total number of calls for each cause:

```sql
SELECT cause, SUM(value) FROM m_countbycrc GROUP BY cause;
```

Get number of calls by cause for each timestamp:

```sql
SELECT timestamp AS time, COUNT(cause), cause FROM m_countbycrc GROUP BY time, cause;
```



## Trunk statistics

Lahko probaš tud brez time, sicer traja predolgo:

```sql
SELECT timestamp AS time, SUM(value), inctg, outtg, nodeid FROM m_durationbytg GROUP BY time, nodeid, inctg, outtg;
```

Which node has most traffic:

```sql
SELECT nodeid, SUM(value) AS "Total duration" FROM m_durationbytg GROUP BY nodeid ORDER BY "Total duration" DESC;
```


## KPIs

#### ASR

Answer to seizure ratio:

(SELECT COUNT(*) FROM m_countbycrc WHERE cause='Answered')/(SELECT COUNT(*) FROM m_countbycrc)


## Indexing

```sql
CREATE INDEX ON m_countbycrc (nodeId, cause);
```

## Metric estimation

For CDR:
100 nodov x 10 causes x 2 directions = 2000 metrics


## Data capacity

m_countByCrc ima polja: id, node, cause, incTg, outTg, timestamp in value
m_durationbytg ima polja: nodeId, incTG, outTG, timestamp in value

#### ElasticSearch

450M vse metrike skupaj .... docker volume ima 29 GB

#### Postgres

vpisovanje (70k pri 64 thredov, 350k pri 128 thredov)

m_countByCrc: velikost pg data direktorija: 24 GB

140M vrstic ..... 11 GB

m_durationbytg: 56M vrstic ...... 3,3 GB


En scrape/bulk vsebuje cca. 70k byCrc, 40k byDuration, skupaj 110k metrik. V fajlu je to 20 MB.


