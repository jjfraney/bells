FROM armv7/armhf-debian:8

MAINTAINER jjfraney@gmail.com <John J. Franey>

ADD jdk-8u131-linux-arm32-vfp-hflt.tar.gz /
RUN ln -s /jdk1.8.0_131 /jdk
ENV PATH=/jdk/bin:$PATH
VOLUME /jdk

