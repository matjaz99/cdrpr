# Title

```
$ mvn install:install-file -Dfile=cdr-parser-2.0.37.jar -DgroupId=si.iskratel -DartifactId=cdr-parser -Dversion=2.0.37 -Dpackaging=jar
```



```
docker run -d --network es_elastic -e CDRPR_DIRECTORY=/opt/cdr -e CDRPR_THREADS=4 -e CDRPR_BULK_SIZE=10000 -e CDRPR_DEBUG_ENABLED=false -e CDRPR_ES_URL=http://es01:9200/cdrs/_bulk?pretty --name cdrpr matjaz99/cdrpr:1.0
```


Število zapisov če hranimo vse klice:

100k/15m == 400k/h == 9,6M/d == 288M/M == 3,456B/Y



