# CDRAGGS in PG

https://www.postgresql.org/docs/12/index.html
https://grafana.com/docs/grafana/latest/features/datasources/postgres/


visualization for PG:
https://chartio.com/product/data-sources/postgresql/


Deploy PG with pgadmin and grafana:

```
docker stack deploy -c compose-cdraggs-with-pg.yml pg
```

Connect to PG:

```
$ psql -h 192.168.0.170 -p 5432 -U postgres -d cdraggs
```

```sql
SELECT COUNT(*) FROM public.m_countbycrc;
SELECT NOW() as time, COUNT(*) FROM public.m_countbycrc
```

## Queriying metrics

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


## Data capacity

m_countByCrc ima polja: id, node, cause, incTg, outTg, timestamp in value

vpisovanje (70k pri 64 thredov, 350k pri 128 thredov)

velikost data direktorija: 24 GB

140M vrstic ..... 11 GB

m_durationbytg ima polja: nodeId, incTG, outTG, timestamp in value

56M vrstic ...... 3,3 GB


En scrape/bulk vsebuje cca. 70k byCrc, 40k byDuration, skupaj 110k metrik. V fajlu je to 20 MB.


