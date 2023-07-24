# Openllet: An Open Source OWL DL reasoner for Java

## Openllet Temporal Query

This is a fork of Openllet containing support for answering temporal queries.

## Installation

### Requirements

- Java >= 17 and a compatible Maven version.
- `mltl2ltl` in `$PATH`: https://github.com/lu-w/mltl2ltl
- `lydia` in `$PATH`: https://github.com/whitemech/lydia

### Build

To build, clone this repository and run `mvn install -DskipTests` from the top level directory.
The `openllet` and `openllet.exe` binaries are then located in `tools-cli/target/openlletcli/bin`.
For `bash`, you can add it to your global path by adding 

`export PATH="/path/to/your/openllet/location/tools-cli/target/openlletcli/bin:$PATH"`

to your `~/.bashrc`.

### Tests

You can run the tests for the temporal query component by calling `mvn -pl tests test -Dtest=TCQTestSuite`

## Examples

(TODO)

## Openllet is an OWL 2 DL reasoner:

Openllet can be used with [Jena](https://jena.apache.org/) or [OWL-API](http://owlcs.github.io/owlapi/) libraries. Openllet provides functionality to check consistency of ontologies, compute the classification hierarchy, 
explain inferences, and answer SPARQL queries.
