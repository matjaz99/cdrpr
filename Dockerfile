FROM maven:3.6-jdk-8-alpine AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:8-jre
RUN mkdir -p /opt/cdr
RUN mkdir -p /opt/dump
RUN mkdir -p /opt/config
COPY --from=build /usr/src/app/target/cdrpr-3.0-jar-with-dependencies.jar /opt/cdrpr-3.0-jar-with-dependencies.jar
COPY config/call_release_causes.properties /opt/config/call_release_causes.properties
COPY config/severities.properties /opt/config/severities.properties
COPY config/logback.xml /opt/config/logback.xml
COPY init.sh /opt/init.sh

RUN apt install iputils-ping

WORKDIR /opt

EXPOSE 9099

CMD java -jar -Dlogback.configurationFile=config/logback.xml cdrpr-3.0-jar-with-dependencies.jar
#CMD sh init.sh
