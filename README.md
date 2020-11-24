# Title

```
$ mvn install:install-file -Dfile=cdr-parser-2.0.37.jar -DgroupId=si.iskratel -DartifactId=cdr-parser -Dversion=2.0.37 -Dpackaging=jar
```



```
docker run -d --network es_elastic -e CDRPR_DIRECTORY=/opt/cdr -e CDRPR_THREADS=4 -e CDRPR_BULK_SIZE=10000 -e CDRPR_DEBUG_ENABLED=false -e CDRPR_ES_URL=http://es01:9200/cdrs/_bulk?pretty --name cdrpr matjaz99/cdrpr:1.0
```



Dodaj severity=6 za evente

[{"nodeId":1048888,"alarmId":"55566699No connection1048888No connection to ElasticSearch","timestamp":1606170707527,"alarmCode":55566699,"alarmName":"No connection","severity":1,severityString":null,"sourceInfo":"No connection to ElasticSearch","additionalInfo":"Unknown host"}]


PMetric model:
{
	"metric_name": "pmon_calls_by_cause",
	"nodeName": "node1",
	"cause": "Answered",
	"timestamp": 164763762843,
	"value": 123
}


{
	"metric_name": "pmon_prefix",
	"nodeName": "node1",
	"prefix_out": "00389",
	"prefix_in": "00386",
	"prefix_out_country": "MK",
	"prefix_in_country": "SLO",
	"timestamp": 164763762843,
	"value": 123
}


PMultiValueMetric:
{
	"metric_name": "pmon_xml_measurements",
	"nodeName": "node1",
	"cause": "Answered",
	"IT.SuccSession": 100,
	"IT.AttSession": 200,
	"timestamp": 164763762843,
	"value": 0
}





Število metrik:

100k/15m == 400k/h == 9,6M/d == 288M/M == 3,456B/Y

Data:

500M ... 25GB
3,5B ... 175GB (?)

1 primary, 0 replicas:  
Total documents: 1447477788  
Total size: 67.47001953124995 GB

Total documents: 2257371568  
Total size: 105.73261718749987 GB

