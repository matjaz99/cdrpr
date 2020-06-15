FROM openjdk:8-jre

RUN mkdir -p /opt/cdr

COPY target/cdrpr-2.0-jar-with-dependencies.jar /opt/cdrpr-2.0-jar-with-dependencies.jar
COPY call_release_causes.properties /opt/call_release_causes.properties

WORKDIR /opt

EXPOSE 9099

CMD java -jar cdrpr-2.0-jar-with-dependencies.jar