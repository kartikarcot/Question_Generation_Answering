# we will use openjdk 8 with alpine as it is a very small linux distro
#FROM openjdk:11-jdk-slim
FROM ubuntu:18.04

# Set UTF-8 encoding
ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

# install java 11
RUN apt-get -y update
RUN apt-get -y install openjdk-11-jre-headless

# copy the packaged jar file into our docker image
RUN mkdir /QA
ADD ask.jar /QA
ADD ask /QA
ADD simplelogger.properties /QA

# Change the permissions of programs
RUN chmod 777 /QA/*

# set the startup command to execute the jar
WORKDIR /QA
ENTRYPOINT ["/bin/bash", "-c"]