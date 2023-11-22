# Openllet: An Open Source OWL DL reasoner for Java

## Openllet Temporal Query

This is a fork of Openllet v2.6.6 containing support for answering temporal queries.
For more information on the Temporal Query component, please refer to [this readme](module-mtcq/README.md).
For example, information on the input languages is located there.

## Installation

The installation is tested on UNIX-systems.
This installation assumes a Ubuntu system with `bash`, however, it will work analogously on other Linux distributions or shells.

### Prerequisites

We require the following software to be installed on your system:

- Java version >= 17 (you can check your version with `java --version`)
- Maven (fitting to the Java version, you can check your version with `mvn --version`)
- Python 3 with PIP
- Docker (optional, if Lydia shall not be compiled from source)

To install Python, PIP, Java, and Maven, call `sudo apt-get update && sudo apt-get install python3 python3-pip openjdk-17-jdk maven`.

For the optional Docker dependency, follow the [official instructions](https://docs.docker.com/engine/install/ubuntu/#install-using-the-repository) to install Docker.
After installation, you need to start the Docker daemon using `sudo systemctl start docker`.
Later, we will use `docker run` without root rights, therefore, please add your user to the `docker` group as per the [documentation](https://docs.docker.com/engine/install/linux-postinstall).

### Step-by-step instructions

We first install the dependencies MLTL2LTLf and Lydia.

#### Installing MLTL2LTLf

1. Get a copy of MLTL2LTLf from https://github.com/lu-w/mltl2ltlf and navigate into the folder.
2. Install via: `pip install .`
3. The script may be installed to a location not in your `$PATH`. PIP warns you about that. If so, please add the path that pip outputs to your `$PATH` by calling `echo "export PATH=\"/path/pip/warned/you/about:$PATH\"" >> ~/.bashrc && source ~/.bashrc`.
4. `mltl2ltlf` should now be callable from your command line.

#### Installing Lydia

0. Navigate into an appropriate, persistent directory.
1. Pull the Docker image: `docker pull whitemech/lydia:latest`
2. Make an executable for Lydia: `mkdir -p bin && cd bin && echo "docker run --mount src=/tmp,target=/tmp,type=bind -v$(pwd):/home/default whitemech/lydia lydia \"\$@\"" > lydia && chmod +x lydia && echo "export PATH=$(pwd):$PATH" >> ~/.bashrc && source ~/.bashrc && cd ..`
3. `lydia` should now be callable from your command line.

If you do not have Docker, you can also build Lydia from the source, as documented at https://github.com/whitemech/lydia.

#### Installing Openllet

1. Call `mvn -DskipTests install` from this directory.
2. Add `openllet` to your `$PATH`, as the binary is now located in `tools-cli/target/openlletcli/bin`: Call `echo "export PATH=\"$(pwd)/tools-cli/target/openlletcli/bin:$PATH\"" >> ~/.bashrc && source ~/.bashrc`.
3. `openllet` should now be callable from your command line.

## Running Openllet Temporal Query on an example

We provide a very small introductory example.
For this, navigate into the examples directory: `cd openllet/examples/src/main/resources/data/mtcq`
and call `openllet temporal-query -c catalog-v001.xml -q simple.mtcq abox.kbs`.

## Running Openllet Temporal Query on the test cases

We also provide a suite of test cases.
To run those, call `mvn -pl tests test -Dtest=MTCQTestSuite` from the `openllet` directory.

## Logging

In contrast to the upstream version of `openllet`, this version allows a fine-tuned control over logging.
For this, instead of running the `openllet` from `tools-cli/target/openlletcli/bin/openllet`, use
`export JAVA_OPTS="-Djava.util.logging.config.file=path/to/logging.properties"; openllet`.

## Openllet is an OWL 2 DL reasoner:

Openllet can be used with [Jena](https://jena.apache.org/) or [OWL-API](http://owlcs.github.io/owlapi/) libraries. Openllet provides functionality to check consistency of ontologies, compute the classification hierarchy, 
explain inferences, and answer SPARQL queries.
