# set ubuntu as the base image
FROM ubuntu:14.04

MAINTAINER Alexandros Papadopoulos alpap@di.uoa.gr

# add repositories
RUN sudo apt-get -y install software-properties-common
RUN sudo add-apt-repository -y ppa:webupd8team/java
RUN sudo apt-get -y update

# accept oracle license
RUN echo debconf shared/accepted-oracle-license-v1-1 select true | \
      sudo debconf-set-selections

# install exareme dependencies
RUN sudo apt-get -y install python python-apsw
RUN sudo apt-get -y install oracle-java7-installer

# install madis dependencies
RUN sudo apt-get -y install python-dev python-pip
RUN sudo apt-get -y install libblas-dev liblapack-dev libatlas-base-dev gfortran

ADD ./requirements.txt /root/requirements.txt
RUN pip install -r /root/requirements.txt

# copy
ADD ./exareme-distribution/target/exareme /root/exareme

