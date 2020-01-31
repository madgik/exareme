FROM alpine:3.6
MAINTAINER Sofia Karvounari <karvoun@di.uoa.gr>

# Here we install GNU libc (aka glibc) and set C.UTF-8 locale as default.
RUN ALPINE_GLIBC_BASE_URL="https://github.com/sgerrand/alpine-pkg-glibc/releases/download" && \
    ALPINE_GLIBC_PACKAGE_VERSION="2.23-r2" && \
    ALPINE_GLIBC_BASE_PACKAGE_FILENAME="glibc-$ALPINE_GLIBC_PACKAGE_VERSION.apk" && \
    ALPINE_GLIBC_BIN_PACKAGE_FILENAME="glibc-bin-$ALPINE_GLIBC_PACKAGE_VERSION.apk" && \
    ALPINE_GLIBC_I18N_PACKAGE_FILENAME="glibc-i18n-$ALPINE_GLIBC_PACKAGE_VERSION.apk" && \
    apk add --no-cache --virtual=build-dependencies wget ca-certificates && \
    wget \
        "$ALPINE_GLIBC_BASE_URL/$ALPINE_GLIBC_PACKAGE_VERSION/$ALPINE_GLIBC_BASE_PACKAGE_FILENAME" \
        "$ALPINE_GLIBC_BASE_URL/$ALPINE_GLIBC_PACKAGE_VERSION/$ALPINE_GLIBC_BIN_PACKAGE_FILENAME" \
        "$ALPINE_GLIBC_BASE_URL/$ALPINE_GLIBC_PACKAGE_VERSION/$ALPINE_GLIBC_I18N_PACKAGE_FILENAME" && \
    apk add --allow-untrusted --no-cache \
        "$ALPINE_GLIBC_BASE_PACKAGE_FILENAME" \
        "$ALPINE_GLIBC_BIN_PACKAGE_FILENAME" \
        "$ALPINE_GLIBC_I18N_PACKAGE_FILENAME" && \
    /usr/glibc-compat/bin/localedef --force --inputfile POSIX --charmap UTF-8 C.UTF-8 || true && \
    echo "export LANG=C.UTF-8" > /etc/profile.d/locale.sh && \
    apk del glibc-i18n && \
    apk del build-dependencies && \
    rm \
        "$ALPINE_GLIBC_BASE_PACKAGE_FILENAME" \
        "$ALPINE_GLIBC_BIN_PACKAGE_FILENAME" \
        "$ALPINE_GLIBC_I18N_PACKAGE_FILENAME"

ENV LANG=C.UTF-8

########################################################
# Install Java (Shamelessly copy pasted from develar/java,
# https://github.com/develar/docker-java/blob/master/Dockerfile)
#
# LSC: Updated for new URLs schemes on the Oracle website.
ENV JAVA_VERSION_MAJOR=8 \
    JAVA_VERSION_MINOR=141 \
    JAVA_VERSION_BUILD=15 \
    JAVA_VERSION_HASH=336fa29ff2bb4ef291e347e091f7f4a7 \
    JAVA_PACKAGE=server-jre \
    JAVA_HOME=/jre \
    PATH=${PATH}:/jre/bin \
    LANG=C.UTF-8

# about nsswitch.conf - see https://registry.hub.docker.com/u/frolvlad/alpine-oraclejdk8/dockerfile/
    #/usr/glibc/usr/bin/ldconfig /lib /usr/glibc/usr/lib && \

RUN apk add --update curl ca-certificates && \
        cd /tmp && \
    echo 'hosts: files mdns4_minimal [NOTFOUND=return] dns mdns4' >> /etc/nsswitch.conf && \
    curl -jksSLH "Cookie: oraclelicense=accept-securebackup-cookie" \
        "http://download.oracle.com/otn-pub/java/jdk/${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-b${JAVA_VERSION_BUILD}/${JAVA_VERSION_HASH}/${JAVA_PACKAGE}-${JAVA_VERSION_MAJOR}u${JAVA_VERSION_MINOR}-linux-x64.tar.gz" \
        | gunzip -c - | tar -xf - && \
    apk del curl ca-certificates && \
    mv jdk1.${JAVA_VERSION_MAJOR}.0_${JAVA_VERSION_MINOR}/jre /jre && \
    rm /jre/bin/jjs && \
    rm /jre/bin/keytool && \
    rm /jre/bin/orbd && \
    rm /jre/bin/pack200 && \
    rm /jre/bin/policytool && \
    rm /jre/bin/rmid && \
    rm /jre/bin/rmiregistry && \
    rm /jre/bin/servertool && \
    rm /jre/bin/tnameserv && \
    rm /jre/bin/unpack200 && \
    rm /jre/lib/ext/nashorn.jar && \
    rm /jre/lib/jfr.jar && \
    rm -rf /jre/lib/jfr && \
    rm -rf /jre/lib/oblique-fonts && \
    rm -rf /tmp/* /var/cache/apk/*

# Some extra python libraries for the mip-algorithms, which needs to be
# compiled by hand
ADD files/requirements.txt /root/requirements.txt
RUN apk add --update py-psycopg2 py-pip ca-certificates gcc musl-dev python-dev lapack-dev g++ gfortran && \
    pip install -r /root/requirements.txt && \
    pip install scipy==1.2.1 scikit-learn==0.20.3 && \
    pip install pandas && \
    pip install liac-arff && \
    apk del py-pip ca-certificates gcc musl-dev python-dev lapack-dev gfortran && \
    rm -rf /tmp/* /var/cache/apk/*

# Runtime dependencies for Exareme
RUN apk add --update rsync curl bash jq python py-requests lapack --no-cache procps && \
    rm -rf /tmp/* /var/cache/apk/*


# Add Exareme
ADD src/exareme/exareme-distribution/target/exareme /root/exareme

# Add the algorithms
ADD src/mip-algorithms /root/mip-algorithms

# Exareme configuration, ssh keys and so on
# This has to be done after copying in the algorithms and exareme, as some
# files are placed in folders created by those two steps.
ADD files/java.sh /etc/profile.d/java.sh
RUN chmod 755 /etc/profile.d/java.sh
ADD files/root /root
RUN chmod -R 755 /root/exareme/*.py /root/exareme/*.sh

EXPOSE 9090
EXPOSE 22

ENV USER=root
ENV PYTHONPATH "${PYTONPATH}:/root/mip-algorithms"
WORKDIR /root/exareme

CMD ["/bin/bash","bootstrap.sh"]
# While debugging
#ENTRYPOINT /bin/sh
