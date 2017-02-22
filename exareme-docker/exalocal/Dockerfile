FROM ubuntu:16.04
MAINTAINER Yannis Chronis <i.chronis@di.uoa.gr>

# update
RUN  apt-get -y update

# install
RUN apt-get install -y git maven curl python python-apsw openssh-server
RUN apt-get install -y net-tools
RUN apt-get -y install nano

# ssh
RUN ssh-keygen -q -N "" -t rsa -f /root/.ssh/id_rsa
RUN cp /root/.ssh/id_rsa.pub /root/.ssh/authorized_keys

# java
RUN mkdir -p /usr/java/default && \
    curl -Ls 'http://download.oracle.com/otn-pub/java/jdk/7u51-b13/jdk-7u51-linux-x64.tar.gz' -H 'Cookie: oraclelicense=accept-securebackup-cookie' | \
    tar --strip-components=1 -xz -C /usr/java/default/

ENV JAVA_HOME /usr/java/default/
ENV PATH $PATH:$JAVA_HOME/bin

ADD requirements.txt /requirements.txt
RUN apt-get install -y python-dev build-essential python-pip libblas-dev liblapack-dev libatlas-base-dev gfortran
RUN pip install -r requirements.txt

RUN git clone -b mip https://github.com/madgik/exareme.git /root/exareme-src #4442a2
WORKDIR /root/exareme-src
RUN mvn clean install
RUN mv /root/exareme-src/exareme-distribution/target/exareme* /root/exareme

#uncomment to use the local code of exareme -- (mvn install first)
#ADD ./exareme-distribution/target/exareme /root/exareme

#uncomment to use the algorithms from the repo
#RUN  git clone https://github.com/madgik/mip-algorithms.git /root/mip-algorithms
#RUN rm /root/mip-algorithms/properties.json
#ADD properties.json /root/mip-algorithms/

RUN cp -R /root/exareme/lib/algorithms-dev /root/mip-algorithms

RUN sed -i '/"username"/c\{ "name" : "username", "desc" : "", "value":"<raw_username>" },' /root/mip-algorithms/properties.json
RUN sed -i '/"password"/c\{ "name" : "username", "desc" : "", "value":"<raw_password>" },' /root/mip-algorithms/properties.json
RUN sed -i '/"host"/c\{ "name" : "username", "desc" : "", "value":"<raw_host>" },' /root/mip-algorithms/properties.json
RUN sed -i '/"port"/c\{ "name" : "username", "desc" : "", "value":"<raw_port>" },' /root/mip-algorithms/properties.json
RUN sed -i '/"api"/c\{ "name" : "endpoint", "desc" : "", "value":"<raw_endpoint>" },' /root/mip-algorithms/properties.json
RUN sed -i '/"resultsPerPage"/c\{ "name" : "resultsPerPage", "desc" : "", "value":"<raw_resultsperpage>" },' /root/mip-algorithms/properties.json
RUN sed -i '/"datakey"/c\{ "name" : "resultsPerPage", "desc" : "", "value":"<raw_datakey>" }' /root/mip-algorithms/properties.json

WORKDIR /root/exareme

RUN sed -i '/demo.repository.path=/c\demo.repository.path=/root/mip-algorithms/' /root/exareme/etc/exareme/gateway.properties
RUN sed -i '/static.path/c\static.path=/root/exareme/static/' /root/exareme/etc/exareme/gateway.properties


EXPOSE 9090

RUN export TERM=xterm

ADD bootstrap.sh /root/exareme/bootstrap.sh
ADD rawreq.sh /root/exareme/rawreq.sh
ENTRYPOINT /bin/bash bootstrap.sh
