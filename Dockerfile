FROM ubuntu:14.04

# update
RUN sudo apt-get -y update

# install
RUN apt-get install -y curl python python-apsw openssh-server

# ssh
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# java
RUN mkdir -p /usr/java/default && \
    curl -Ls 'http://download.oracle.com/otn-pub/java/jdk/7u51-b13/jdk-7u51-linux-x64.tar.gz' -H 'Cookie: oraclelicense=accept-securebackup-cookie' | \
    tar --strip-components=1 -xz -C /usr/java/default/

ENV JAVA_HOME /usr/java/default/
ENV PATH $PATH:$JAVA_HOME/bin
