# Title

```
$ mvn install:install-file -Dfile=cdr-parser-2.0.37.jar -DgroupId=si.iskratel -DartifactId=cdr-parser -Dversion=2.0.37 -Dpackaging=jar
```



```
docker run -d --network es_elastic -e CDRPR_DIRECTORY=/opt/cdr -e CDRPR_THREADS=4 -e CDRPR_BULK_SIZE=10000 -e CDRPR_DEBUG_ENABLED=false -e CDRPR_ES_URL=http://es01:9200/cdrs/_bulk?pretty --name cdrpr matjaz99/cdrpr:1.0
```


Število metrik:

100k/15m == 400k/h == 9,6M/d == 288M/M == 3,456B/Y

Data:

500M ... 25GB
3,5B ... 175GB (?)

1 primary, 0 replicas:
Total documents: 1447477788
Total size: 67.47001953124995 GB


