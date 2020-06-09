# Title

$ mvn install:install-file -Dfile=cdr-parser-2.0.37.jar -DgroupId=si.iskratel -DartifactId=cdr-parser -Dversion=2.0.37 -Dpackaging=jar


## Storage

Collecting 18 params in CDR

```
1M ......... 160 MB
16M ........ 3.9 GB
21M ........ 4.9 GB
36M ........ 8.3 GB
39M ........ 9.7 GB
54M ....... 12.3 GB
86M ....... 19.6 GB
102M ...... 23.6 GB
118M ...... 26.7 GB
134M ...... 30.2 GB
```

## Performances

iMac2 (i7 4 CPU, 8 GB RAM, CentOS 7 in VirtualBox + (monis + es) + parser in InteliJ)

```
15M .... threads=2, bulk=10k, rate=8600/s, vlc=on
15M .... threads=4, bulk=10k, rate=8600/s, vlc=on
15M .... threads=4, bulk=10k, rate=9300/s, vlc=off
15M .... threads=4, bulk=10k, rate=23500/s, retries=1, vlc=off, why so fast (time since last run = 3 hours)? - Disk se je zafilu in se ni vse zapisalo
15M .... threads=2, bulk=20k, rate=8900/s, retries=13, vlc=off
15M .... threads=4, bulk=20k, rate=9000/s, retries=95, vlc=off
15M .... threads=4, bulk=20k, rate=10000/s, retries=72, vlc=off
15M .... threads=8, bulk=5k, rate=16000/s, retries=28, vlc=off
15M .... threads=8, bulk=10k, rate=15000/s, retries=82, vlc=off
15M .... threads=4, bulk=5k, rate=12000/s, resend=0, vlc=on
15M .... threads=4, bulk=10k, rate=12000/s, retries=0, vlc=off, commented toString
```



docker run -d --network es_elastic -e CDRPR_DIRECTORY=/opt/cdr -e CDRPR_THREADS=4 -e CDRPR_BULK_SIZE=10000 -e CDRPR_DEBUG_ENABLED=false -e CDRPR_ES_URL=http://es01:9200/cdrs/_bulk?pretty --name cdrpr matjaz99/cdrpr:1.0
