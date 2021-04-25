# Ubuntu Linux as the base image
FROM ubuntu:18.04

ENV LANG C.UTF-8
ENV LC_ALL C.UTF-8

# Install dependencies including Python
RUN apt-get update -y && \
	apt-get install -y apt-utils \
		default-jre \
		default-jdk \
		ant \
		unzip \
		wget \
		git \
		python3-pip \
		python3-dev

# Download Stanford CoreNLP
RUN export REL_DATE="2018-10-05"; \
	wget http://nlp.stanford.edu/software/stanford-corenlp-full-${REL_DATE}.zip; \
	unzip stanford-corenlp-full-${REL_DATE}.zip; \
	mv stanford-corenlp-full-${REL_DATE} CoreNLP; \
	cd CoreNLP; \
	export CLASSPATH=""; for file in `find . -name "*.jar"`; do export CLASSPATH="$CLASSPATH:`realpath $file`"; done

# Install PIP dependencies
RUN pip3 install --upgrade pip
RUN pip3 install torch pycorenlp requests transformers
RUN pip install nltk

# Add the files into container, under QA folder, modify this based on your need
RUN mkdir /QA
ADD start.sh /QA

ADD answer_dir/* /QA/
RUN chmod 777 /QA/*


ENV PORT 9000
EXPOSE 9000

WORKDIR /QA

RUN chmod +x "/bin/bash"
RUN chmod +x "/QA/start.sh"

ENTRYPOINT /QA/start.sh 
