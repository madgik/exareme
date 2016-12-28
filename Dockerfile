FROM ubuntu:14.04

# update
RUN sudo apt-get -y update

# install
RUN sudo apt-get -y install python python-apsw
RUN apt-get install -y  openssh-server
RUN apt-get install -y curl
RUN sudo apt-get -y install nano

# ssh
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# java
RUN mkdir -p /usr/java/default && \
    curl -Ls 'http://download.oracle.com/otn-pub/java/jdk/7u51-b13/jdk-7u51-linux-x64.tar.gz' -H 'Cookie: oraclelicense=accept-securebackup-cookie' | \
    tar --strip-components=1 -xz -C /usr/java/default/

ENV JAVA_HOME /usr/java/default/
ENV PATH $PATH:$JAVA_HOME/bin

RUN sudo apt-get -y update
ADD ./requirements.txt /requirements.txt
RUN sudo apt-get install -y python-dev build-essential python-pip libblas-dev liblapack-dev libatlas-base-dev gfortran
RUN sudo pip install -r requirements.txt



ADD ./exareme-distribution/target/exareme /root/exareme
WORKDIR /root/exareme
ADD bootstrap.sh /root/exareme/bootstrap.sh

ENTRYPOINT /bin/bash bootstrap.sh
