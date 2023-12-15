FROM whitemech/lydia
LABEL version="0.1"
LABEL authors="Lukas Westhofen <lukas.westhofen@dlr.de>"
LABEL description="Builds and runs Topllet, an answering engine for Metric Temporal Conjunctive Queries."

USER root
RUN mkdir -p /var/cache/apt/archives/partial
RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y \
       python3 \
       python3-pip \
       openjdk-17-jdk \
       maven && \
    apt-get clean && \
    rm -rf /var/cache

# Dependency: MLTL2LTLf

WORKDIR /mltl2ltlf
ENV MLTL2LTLF_VERSION="0.2"
RUN wget https://github.com/lu-w/mltl2ltlf/archive/refs/tags/v${MLTL2LTLF_VERSION}.tar.gz && \
	tar -xf v${MLTL2LTLF_VERSION}.tar.gz && \
	cd mltl2ltlf-${MLTL2LTLF_VERSION} && \
	pip install .

WORKDIR /

# Correct Maven version for Ubuntu 20.04
ENV MAVEN_VERSION="3.9.6"
RUN wget https://apache.org/dist/maven/maven-3/$MAVEN_VERSION/binaries/apache-maven-$MAVEN_VERSION-bin.tar.gz -P /tmp
RUN tar xf /tmp/apache-maven-*.tar.gz -C /opt
RUN rm /tmp/apache-maven-*-bin.tar.gz
RUN ln -s /opt/apache-maven-$MAVEN_VERSION /opt/maven

# Tool: Topllet
ENV TOPLLET_VERSION="v-t.0.0.2"
RUN git clone https://github.com/lu-w/topllet.git && \
	cd topllet && \
	git checkout ${TOPLLET_VERSION} && \
	/opt/maven/bin/mvn -DskipTests install

USER default
WORKDIR /home/default
ENTRYPOINT ["/topllet/tools-cli/target/openlletcli/bin/topllet"]
