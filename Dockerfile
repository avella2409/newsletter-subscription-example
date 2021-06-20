FROM debian:latest as deb
RUN apt-get update && \
    apt-get install --assume-yes curl && \
    apt-get install --assume-yes gnupg && \
    echo "deb https://packages.fluentbit.io/debian/buster buster main" >> /etc/apt/sources.list && \
    curl https://packages.fluentbit.io/fluentbit.key | apt-key add - && \
    apt-get update && \
    apt-get install --assume-yes td-agent-bit
COPY td-agent-bit.conf /etc/td-agent-bit/.
ENV PATH "$PATH:/opt/td-agent-bit/bin"

RUN curl https://download.java.net/java/GA/jdk16/7863447f0ab643c585b9bdebf67c69db/36/GPL/openjdk-16_linux-x64_bin.tar.gz --output jdk16 && \
    tar xzvf jdk16 && \
    rm -rf jdk16
ENV PATH "$PATH:/jdk-16/bin"

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

COPY startup.sh /.
ENTRYPOINT ["/bin/bash", "/startup.sh"]
