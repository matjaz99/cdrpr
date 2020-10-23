#FROM maven:3.5.2-jdk-9 AS build
FROM maven:3.6-jdk-8-alpine AS build
COPY src /usr/src/app/src
COPY pom.xml /usr/src/app
RUN mvn -f /usr/src/app/pom.xml clean package

FROM openjdk:8-jre
RUN mkdir -p /opt/cdr
RUN mkdir -p /opt/dump
#COPY target/cdrpr-2.0-jar-with-dependencies.jar /opt/cdrpr-2.0-jar-with-dependencies.jar
COPY --from=build /usr/src/app/target/cdrpr-2.0-jar-with-dependencies.jar /opt/cdrpr-2.0-jar-with-dependencies.jar
COPY call_release_causes.properties /opt/call_release_causes.properties

WORKDIR /opt

EXPOSE 9099

CMD java -jar cdrpr-2.0-jar-with-dependencies.jar