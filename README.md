# Title

```
$ mvn install:install-file -Dfile=cdr-parser-2.0.37.jar -DgroupId=si.matjazcerkvenik -DartifactId=cdr-parser -Dversion=2.0.37 -Dpackaging=jar
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




26-05-2021 10:05:33.699 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_metric [size=1062]
26-05-2021 10:05:33.699 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:34.481 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 781ms]
26-05-2021 10:05:34.484 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_multivalue_metric [size=162
26-05-2021 10:05:34.484 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:34.682 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 198ms]
26-05-2021 10:05:34.683 [main] INFO  si.matjazcerkvenik.datasims.cdrpr.xml.XmlParser.main - Moving file to new location: /var/lib/Elastic/xml_processed_dir/56010020210322040000.xml
26-05-2021 10:05:34.683 [main] INFO  si.matjazcerkvenik.datasims.cdrpr.xml.XmlParser.main - Reading file: /var/lib/Elastic/xml_input_dir/56010020210309110000.xml
26-05-2021 10:05:34.706 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_metric [size=1062]
26-05-2021 10:05:34.706 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:34.712 [qtp1381713434-13] WARN  org.eclipse.jetty.server.HttpChannel.handleException - /metrics
java.util.ConcurrentModificationException: null
        at java.util.HashMap$HashIterator.nextNode(HashMap.java:1437) ~[na:1.8.0_131]
        at java.util.HashMap$ValueIterator.next(HashMap.java:1466) ~[na:1.8.0_131]
        at java.util.AbstractCollection.toArray(AbstractCollection.java:141) ~[na:1.8.0_131]
        at java.util.ArrayList.<init>(ArrayList.java:177) ~[na:1.8.0_131]
        at si.matjazcerkvenik.metricslib.PMetric.getTimeSeries(PMetric.java:117) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at si.matjazcerkvenik.metricslib.PMetricRegistry.collectPrometheusMetrics(PMetricRegistry.java:90) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at si.matjazcerkvenik.metricslib.MetricsLib$MetricsServletExtended.doGet(MetricsLib.java:191) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:687) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at javax.servlet.http.HttpServlet.service(HttpServlet.java:790) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.servlet.ServletHolder.handle(ServletHolder.java:841) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:543) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.ScopedHandler.nextHandle(ScopedHandler.java:188) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1253) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.ScopedHandler.nextScope(ScopedHandler.java:168) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:481) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.ScopedHandler.nextScope(ScopedHandler.java:166) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1155) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:141) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:132) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.Server.handle(Server.java:564) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:317) ~[cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:251) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:279) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:110) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.io.ChannelEndPoint$2.run(ChannelEndPoint.java:124) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.util.thread.Invocable.invokePreferred(Invocable.java:128) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.util.thread.Invocable$InvocableExecutor.invoke(Invocable.java:222) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.doProduce(EatWhatYouKill.java:294) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.run(EatWhatYouKill.java:199) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:672) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:590) [cdrpr-2.0-jar-with-dependencies.jar:na]
        at java.lang.Thread.run(Thread.java:748) [na:1.8.0_131]
26-05-2021 10:05:35.105 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 398ms]
26-05-2021 10:05:35.109 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_multivalue_metric [size=162
26-05-2021 10:05:35.109 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:35.161 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 52ms]
26-05-2021 10:05:35.162 [main] INFO  si.matjazcerkvenik.datasims.cdrpr.xml.XmlParser.main - Moving file to new location: /var/lib/Elastic/xml_processed_dir/56010020210309110000.xml
26-05-2021 10:05:35.162 [main] INFO  si.matjazcerkvenik.datasims.cdrpr.xml.XmlParser.main - Reading file: /var/lib/Elastic/xml_input_dir/56010020210314060000.xml
26-05-2021 10:05:35.174 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_metric [size=1062]
26-05-2021 10:05:35.174 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:35.282 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 107ms]
26-05-2021 10:05:35.285 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_multivalue_metric [size=162
26-05-2021 10:05:35.285 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:35.323 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 37ms]
26-05-2021 10:05:35.324 [main] INFO  si.matjazcerkvenik.datasims.cdrpr.xml.XmlParser.main - Moving file to new location: /var/lib/Elastic/xml_processed_dir/56010020210314060000.xml
26-05-2021 10:05:35.324 [main] INFO  si.matjazcerkvenik.datasims.cdrpr.xml.XmlParser.main - Reading file: /var/lib/Elastic/xml_input_dir/56010020210313190000.xml
26-05-2021 10:05:35.335 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_metric [size=1062]
26-05-2021 10:05:35.335 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: >>> POST /_bulk
26-05-2021 10:05:35.852 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.executeHttpRequest - EsClient[2]: <<< 200 - [took 516ms]
26-05-2021 10:05:35.855 [main] INFO  si.matjazcerkvenik.metricslib.EsClient.sendBulkPost - EsClient[2]: sending metric: pm_xml_multivalue_metric [size=162


