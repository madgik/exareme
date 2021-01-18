FROM ubuntu:20.04
MAINTAINER Thanasis Karampatsis <tkarabatsis@athenarc.gr>

ENV LANG=C.UTF-8

# Setting up timezone
ENV TZ=Europe/Athens
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN apt update

# Installing python
RUN apt install -y --no-install-recommends python2
RUN ln -s /usr/bin/python2 /usr/bin/python

# Installing Exareme requirements
RUN apt install -y openjdk-8-jdk curl jq iputils-ping

# Installing pip
RUN curl -O https://raw.githubusercontent.com/pypa/get-pip/master/get-pip.py
RUN python get-pip.py
RUN apt-get install -y python-dev  \
     build-essential libssl-dev libffi-dev \
     libxml2-dev libxslt1-dev zlib1g-dev

ADD files/requirements.txt /root/requirements.txt
RUN pip install -r /root/requirements.txt
RUN pip install scipy==1.2.1 scikit-learn==0.20.3
RUN pip install pandas
RUN pip install lifelines
RUN pip install liac-arff
RUN pip install sqlalchemy
RUN pip install pathlib
RUN pip install tqdm
RUN pip install colour
RUN pip install tornado
RUN pip install statsmodels==0.10.2

# Installing R
RUN apt install -y  software-properties-common
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9
RUN add-apt-repository 'deb https://cloud.r-project.org/bin/linux/ubuntu focal-cran40/'
RUN apt update
RUN apt install -y r-base
RUN Rscript -e 'install.packages("randomForest", repos="https://cloud.r-project.org")'

RUN Rscript -e 'install.packages("caret")'
RUN Rscript -e 'install.packages("e1071")'
RUN pip install rpy2==2.8.6

# Add Madis Server
ADD src/madisServer /root/madisServer

# Add Exareme
ADD src/exareme/exareme-distribution/target/exareme /root/exareme
ADD files/root /root
RUN chmod -R 755 /root/exareme/*.py /root/exareme/*.sh

# Add the algorithms
ADD src/mip-algorithms /root/mip-algorithms

EXPOSE 9090
EXPOSE 22

ENV USER=root
ENV PYTHONPATH "${PYTHONPATH}:/root/mip-algorithms"
WORKDIR /root/exareme

CMD ["/bin/bash","bootstrap.sh"]

