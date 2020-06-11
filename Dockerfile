FROM openjdk:8-jre

RUN mkdir -p /opt/cdr

COPY target/cdrpr-1.0-jar-with-dependencies.jar /opt/cdrpr-1.0-jar-with-dependencies.jar
COPY call_release_causes.properties /opt/call_release_causes.properties

WORKDIR /opt

CMD java -jar cdrpr-1.0-jar-with-dependencies.jar