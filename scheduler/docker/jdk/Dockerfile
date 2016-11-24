FROM armv7/armhf-debian:8

MAINTAINER jjfraney@gmail.com <John J. Franey>

ADD jdk-8u111-linux-arm32-vfp-hflt.tar.gz /
RUN ln -s /jdk1.8.0_111 /jdk
ENV PATH=/jdk/bin:$PATH
VOLUME /jdk

#ENTRYPOINT [/jdk1.8.0_111/bin/java]

