FROM ubuntu:24.04
LABEL version="0.1"
LABEL authors="Lukas Westhofen <lukas.westhofen@dlr.de>"
LABEL description="Builds and runs Topllet, an answering engine for Metric Temporal Conjunctive Queries."

USER root
RUN mkdir -p /var/cache/apt/archives/partial
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
       openjdk-17-jdk \
       maven \
       git && \
    apt-get clean && \
    rm -rf /var/cache

WORKDIR /

ENV TOPLLET_VERSION="feature/streaming"
RUN git clone https://github.com/lu-w/topllet.git && \
	cd topllet && \
	git checkout ${TOPLLET_VERSION} && \
	mvn -DskipTests install

ENTRYPOINT ["/topllet/tools-cli/target/openlletcli/bin/topllet"]
