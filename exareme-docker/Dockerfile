FROM ubuntu:16.04
MAINTAINER Yannis Chronis <i.chronis@di.uoa.gr>

# update
RUN  apt-get -y update

# install
RUN apt-get install -y git maven curl python python-apsw openssh-server

# ssh
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# java
RUN mkdir -p /usr/java/default && \
    curl -Ls 'http://download.oracle.com/otn-pub/java/jdk/7u51-b13/jdk-7u51-linux-x64.tar.gz' -H 'Cookie: oraclelicense=accept-securebackup-cookie' | \
    tar --strip-components=1 -xz -C /usr/java/default/

ENV JAVA_HOME /usr/java/default/
ENV PATH $PATH:$JAVA_HOME/bin

ADD ./requirements.txt /requirements.txt
RUN sudo apt-get install -y python-dev build-essential python-pip libblas-dev liblapack-dev libatlas-base-dev gfortran
RUN sudo pip install -r requirements.txt

RUN git clone -b mip https://github.com/madgik/exareme.git /root/exareme-src
WORKDIR /root/exareme-src
RUN mvn clean install
RUN mv /root/exareme-src/exareme-distribution/target/exareme* /root/exareme
RUN rm /root/exareme/etc/exareme/gateway.properties
ADD gateway.properties /root/exareme/etc/exareme/


RUN  git clone https://github.com/madgik/mip-algorithms.git /root/mip-algorithms
RUN rm /root/mip-alogirthms/properties.json
ADD properties.json /root/mip-alogirthms/

EXPOSE 9090
WORKDIR /root/exareme

ADD bootstrap.sh /root/exareme/bootstrap.sh
ENTRYPOINT /bin/bash bootstrap.sh