# openllet-tcq

TODO Lukas: fill README

## Install

### Requirements

- `lydia` and `mltl2ltl` are in the `PATH`

## Tests

From the parent folder of this directory (i.e., the root of the `openllet` repository), call `mvn -pl tests test -Dtest=TCQTestSuite`.


## Run

### Logging

In contrast to the upstream version of `openllet`, this version allows a fine-tuned control over logging.
For this, instead of running the `openllet` from `tools-cli/target/openlletcli/bin/openllet`, use
`export JAVA_OPTS="-Djava.util.logging.config.file=path/to/logging.properties"; openllet`.
