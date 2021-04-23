# we will use openjdk 8 with alpine as it is a very small linux distro
#FROM openjdk:11-jdk-slim
FROM ubuntu:18.04

# install java 11
RUN apt-get -y update
RUN apt-get -y install openjdk-11-jre-headless

# copy the packaged jar file into our docker image
COPY ask.jar /ask.jar

# set the startup command to execute the jar
CMD ["java", "-jar", "/ask.jar"]