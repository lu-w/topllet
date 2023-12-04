# Topllet

Topllet is an engine for answering Metric Temporal Conjunctive Queries over OWL2-based Temporal Knowledge Bases.
Topllet is a fork of Openllet v2.6.6.

## Table of Contents

1. [Installation](#installation)
	1. [Prerequisites](#prerequisites)
	2. [Step-by-Step Instructions](#step-by-step-instructions)
2. [Usage](#usage)
	1. [Inputs](#inputs)
	2. [Output](#output)
3. [Even More Examples](#even-more-examples)
	1. [Right of Way Example](#right-of-way-example)
	2. [Automotive Urban Traffic Ontology Example](#automotive-urban-traffic-ontology-example)
	3. [API Example](#api-example)
4. [Details](#details)
	1. [Tests](#tests)
	2. [Logging](#logging)
	3. [Algorithms](#algorithms)

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

### Step-by-Step Instructions

We first have to install the dependencies MLTL2LTLf and Lydia.

#### Installing MLTL2LTLf

1. Get a copy of MLTL2LTLf from https://github.com/lu-w/mltl2ltlf and navigate into the folder.
2. Install via: `pip install .`
3. The script may be installed to a location not in your `$PATH`. PIP warns you about that. If so, please add the path that pip outputs to your `$PATH` by calling `echo "export PATH=\"/path/pip/warned/you/about:$PATH\"" >> ~/.bashrc && source ~/.bashrc`.
4. `mltl2ltlf` should now be callable from your command line.

#### Installing Lydia

1. Navigate into an appropriate, persistent directory.
2. Pull the Docker image: `docker pull whitemech/lydia:latest`
3. Make an executable for Lydia: `mkdir -p bin && cd bin && echo "docker run --mount src=/tmp,target=/tmp,type=bind -v$(pwd):/home/default whitemech/lydia lydia \"\$@\"" > lydia && chmod +x lydia && echo "export PATH=$(pwd):$PATH" >> ~/.bashrc && source ~/.bashrc && cd ..`
4. `lydia` should now be callable from your command line.

If you do not have Docker, you can also build Lydia from the source, as documented at https://github.com/whitemech/lydia.

#### Installing Topllet

1. Call `mvn -DskipTests install` from this directory.
2. Add `topllet` to your `$PATH`, as the binary is now located in `tools-cli/target/openlletcli/bin`: Call `echo "export PATH=\"$(pwd)/tools-cli/target/openlletcli/bin:$PATH\"" >> ~/.bashrc && source ~/.bashrc`.
3. `topllet` should now be callable from your command line.

## Usage

For a simple example, navigate to `examples/src/main/resources/data/mtcq/simple`.
The tool is executed like this:

`topllet -c catalog-v001.xml simple.mtcq abox.kbs`

Type `topllet -h` for a help message.

### Inputs

As seen above, `topllet` takes three inputs:

1. A query
2. A temporal knowledge base
3. A catalog file (optional)

#### 1. Temporal Conjunctive Queries

We use `.mtcq` or `.tcq` files to specify temporal conjunctive queries.
An example is the following query:

```
PREFIX t: <http://www.semanticweb.org/mtcq/example#>

G(t:A(?x)) & F(t:C(?y))
```

It thus consists of a list of prefixes (which can be empty), of the format

`PREFIX string <string>`

This is analogous to the SPARQL prefix syntax.
It is followed by an MMTCQ, which roughly adheres to [this](https://github.com/marcofavorito/tl-grammars/blob/main/content/04.ltlf.md) LTLf grammar, with two exceptions.

Firstly, it allows for additional operators bounded by non-negative integers `a` and `b` with `b >= a`:

- `x U_[a,b] y`
- `G_[a,b] x`
- `F_[a,b] x`
- `x U_<=a y`
- `G_<=a x`
- `F_<=a x`

Secondly, instead of atomic propositions, you can specify Conjunctive Queries (CQs).
An example CQ is `(t:A(?x) & t:C(?y) & t:A(t:a))`.
CQs always need to be enclosed by brackets and the single conjuncts (called query atoms) are joined by the `&` operator.
For the query atoms, you can use all concepts and roles existing in the given ontology, using the appropriate prefix, preceeded by a bracketed name (in case of a concept) or a tuple of names (in case of a role).
These names are either answer variables, individuals, or existentially quantified variables.
Answer variables are preceeded by a `?`.
If you want to refer to a certain individual, just use the individual's name (`t:a` in the example).
If you specify a non-`?`-preceeded, non-individual variable in some query atom, it is interpreted as an existentially quantified (also called undistinguished) variable.

You can add comments by `# my comment`.
For inline comments, note that the `#` needs to be succeeded by a whitespace (i.e., `#mycomment` is not a valid comment).

Right now, the tool checks only temporal queries whose CQs are tree-shaped, i.e., the induced query graph is acyclic w.r.t. the existentially quantified variables and each node has at most one incoming edge.

#### 2. Temporal Knowledge Bases

Temporal knowledge bases are modeled simply as a list of standard OWL2 files.
An example is the following temporal knowledge base:

```
abox_1.owl
abox_2.owl
```

Any line starting with `#` is ignored.
We assume a certain shape of the listed OWL2 files:

1. each OWL2 file contains exactly the same individuals with the same names (and ontology IRI for those),
2. all of these individuals are already present in the first OWL2 file, and
3. all OWL2 files in the list import exactly the same ontologies (especially, the shared TBox).

Right now, we do not store information on the actual time stamps of the temporal data.

#### 3. Catalog files

In contrast to the upstream version, `topllet` allows to add OASIS XML catalog files to resolve e.g. local imports.
For this, use the `-c catalog-v001.xml` option.

### Output

Query results (i.e., the certain answers) are displayed to the user after the execution has finished.

Note that, when answering the given query, the implementation assumes `x != y` for all answer variables `x` and `y`, as this seems to be the more natural behavior by default.
This means that if `x` is answered by the individual `a`, `y` can not be mapped to `a` anymore.

Use the `-v` option to get some more feedback during computation.
Even more granular control can be applied by setting a `logging.properties`, as explained in the top-level `README.md`.

## Even More Examples

Besides the simple example from above, there are also more complex examples.
For this, navigate into the examples directory: `cd examples/src/main/resources/data/mtcq`.

### Right of Way Example

The example is in the traffic domain and models an intersection situation with two-wheelers. The corresponding query asks for systems not granting right of way to those two-wheelers.

We have two versions of this example, one where the right of way is granted ('good'), and one where it is not ('bad').

For the 'good' example, call:
 `topllet -c right_of_way/catalog-v001.xml right_of_way/row.tcq right_of_way/good/aboxes.kbs`.
The output is one answer tuple, indicating that right of way was granted.

For the 'bad' example, call:
 `topllet -c right_of_way/catalog-v001.xml right_of_way/row.tcq right_of_way/bad/aboxes.kbs`.
The output shows no results, indicating that right of way was not granted.

### Automotive Urban Traffic Ontology Example

You can also use highly complex ontologies for the traffic domain.
An example of such an ontology is the [Automotive Urban Traffic Ontology](https://github.com/lu-w/auto).
This ontology contains, among others, axioms on parking vehicles, two-lane roads, and dynamical objects.
The example thus asks for vehicles that pass parking vehicles on a two-lane road in the data based on some rudimentary physical and geometrical information.

To run this example, call:
 `topllet -c auto/tbox/catalog-v001.xml auto/pvs.tcq auto/abox/aboxes.kbs`.
The output is one answer, indicating that the vehicle passed some parking vehicle.

### Oedipus

A classical example for the complexity of querying under Description Logics is the tale of Oedipus.
Here, we have four events: 1) Iokaste having her son, Oedipus, 2) Oedipus killing his father and thus becoming a patricide, 3) Oedipus and Iokaste having a child, Polyneikes, and 4) Polyneikes also having a child, Thersandros.
These four events are modeled in four ABoxes.

We can now ask four any `x` having a child which is a patricide having again a child which is not a patricide.
Globally in all ABoxes, there is no answer to this query.
In the last ABox, however, it has an answer.

Therefore, running `topllet -c oedipus/catalog-v001.xml oedipus/f.tcq oedipus/aboxes.kbs` yields Iokaste as the only answer, whereas `topllet -c oedipus/catalog-v001.xml oedipus/g.tcq oedipus/aboxes.kbs` returns no answer.

However, if we replace the undistinguished variables in the query with answer variables (`topllet -c oedipus/catalog-v001.xml oedipus/f_anwer.tcq oedipus/aboxes.kbs`) the query has again no answers.
This is due to the fact that the query is entailed by the union of two different classes of models: one where the undistinguished variables are Polyneikes nd Thersandros, and one where they are Oedipus and Polyneikes.
A single answer can never satisfy this query, highlighting the difference between undistinguished and answer variables in an open-world setting.

### API Example

`topllet` also offers a Java API. 
An example API usage is given in `examples/src/main/java/openllet/examples/MTCQExample.java`.

## Details

### Tests

We provide a suite of test cases for MTCQ answering.
To run the full suite, call `mvn -pl tests test -Dtest=MTCQTestSuite` from this folder.

### Logging

In contrast to the upstream version, this fork allows a fine-tuned control over logging.
For this, instead of directly running `topllet`, use
`export JAVA_OPTS="-Djava.util.logging.config.file=path/to/logging.properties"; topllet`.

### Algorithms

If you wish to comprehend the implementation, the `MTCQEngine` class in [`src/main/java/openllet/mtcq/engine/MTCQEngine.java`](src/main/java/openllet/mtcq/engine/MTCQEngine.java) is a good starting point.
From there, the relevant steps are documented.
