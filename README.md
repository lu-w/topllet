Openllet: An Open Source OWL DL reasoner for Java
-----------------------------------------------

Openllet Temporal Query
-----------------------

This is a fork of Openllet containing support for answering temporal queries.

Installation
------------

Requirements are Java >= 17 and a compatible Maven version.
To build, clone this repository and run `mvn install -DskipTests` from the top level directory.
The `openllet` binary is then located in `tools-cli/target/openlletcli/bin`.
You can add it to your global path by adding 
`export PATH="/path/to/your/openllet/location/tools-cli/target/openlletcli/bin:$PATH"`
to your `~/.bashrc` (or equivalent for your shell).

Tests
-----

You can run the tests for the temporal query component by calling `mvn -pl tests test -Dtest=TCQTestSuite`

Openllet is an OWL 2 DL reasoner: 
--------------------------------

Openllet can be used with [Jena](https://jena.apache.org/) or [OWL-API](http://owlcs.github.io/owlapi/) libraries. Openllet provides functionality to check consistency of ontologies, compute the classification hierarchy, 
explain inferences, and answer SPARQL queries.

Feel free to fork this repository and submit pull requests if you want to see changes, new features, etc. in Openllet.
We need a lot more tests, send your samples if you can.

There are some code samples in the [examples/](https://github.com/Galigator/openllet/tree/integration/examples) directory.
Issues are on [Github](http://github.com/galigator/openllet/issues).
Pellet community is on [pellet-users mailing list](https://groups.google.com/forum/?fromgroups#!forum/pellet-users).


Openllet 2.6.X:
---------------

* Refactor modules dependencies.
* Enforce interface usage in the core system.
* Lighter hash functions and less conflict when use in multi-thread environnement.
* since 2.6.5 : full java 11 support, java 11 is a requirement.

### Migration :

* lots of com.clarkparsia.* / com.mindswap.* are refactored into openllet.* to avoid conflicts and have typing changed a lot.
* dependencies on modern libs.

```xml
	<dependency>
		<groupId>com.github.galigator.openllet</groupId>
		<artifactId>openllet-owlapi</artifactId>
		<version>2.6.5</version>
	</dependency>
	<dependency>
		<groupId>com.github.galigator.openllet</groupId>
		<artifactId>openllet-jena</artifactId>
		<version>2.6.5</version>
	</dependency>
```

NB, the Protege plugin need a Protege that work with an 5.1.X version of the OWL-API, so the main branch of Protege isn't compatible with Openllet.

### Roadmap :

* Fullify strong typing in openllet core (2.7.X).
* Add support for rdf-database reasoning (2.8.X).

### Examples :

Play with the Owl-Api:
```java
try (final OWLManagerGroup group = new OWLManagerGroup())
{
	final OWLOntologyID ontId = OWLHelper.getVersion(IRI.create("http://myOnotology"), 1.0);
	final OWLHelper owl = new OWLGenericTools(group, ontId, true);

	final OWLNamedIndividual x1 = OWL.Individual("#I1");
	final OWLNamedIndividual x2 = OWL.Individual("#I2");

	owl.addAxiom(OWL.equivalentClasses(ClsA, OWL.some(propB, OWL.restrict(XSD.STRING, OWL.facetRestriction(OWLFacet.PATTERN, OWL.constant("A.A"))))));
	owl.addAxiom(OWL.propertyAssertion(x1, propB, OWL.constant("AAA")));
	owl.addAxiom(OWL.propertyAssertion(x2, propB, OWL.constant("BBB")));
	owl.addAxiom(OWL.differentFrom(x1, x2));

	final OpenlletReasoner r = owl.getReasoner();
	assertTrue(r.isEntailed(OWL.classAssertion(x1, ClsA)));
	assertFalse(r.isEntailed(OWL.classAssertion(x2, ClsA)));
}
```

Play with Jena:
```java
	final String ns = "http://www.example.org/test#";

	final OntModel model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
	model.read(_base + "uncle.owl");

	final Individual Bob = model.getIndividual(ns + "Bob");
	final Individual Sam = model.getIndividual(ns + "Sam");

	final Property uncleOf = model.getProperty(ns + "uncleOf");

	final Model uncleValues = ModelFactory.createDefaultModel();
	addStatements(uncleValues, Bob, uncleOf, Sam);
	assertPropertyValues(model, uncleOf, uncleValues);
```

Openllet 2.5.X:
-----------

* full java 8 support, java 8 is a requirement.
* speed and stability improvement

Changes :
* Update versions of libs : owlapi 5, jena3 and lots more. Some old libs have been integrated and cleaned, strongly typed into openllet.
* Corrections : all tests works, no more warnings with high level of reports in Eclipse.

Migration :
* pellet/owlapi/src/main/java/com/clarkparsia/owlapiv3/ is now  pellet/owlapi/src/main/java/com/clarkparsia/owlapi/
* groupId   com.clarkparsia.pellet   is now   com.github.galigator.openllet


Pellet 1..2.3] Licences and supports: 
-------------------------------------
 
* [open source](https://github.com/complexible/pellet/blob/master/LICENSE.txt) (AGPL) or commercial license
* Historically developed and commercially supported by Complexible Inc; Maybe now https://www.stardog.com/


Thanks for using Openllet.

### Others experimentals stuffs

[![CircleCI Build Status](https://circleci.com/gh/Galigator/openllet.svg?style=shield)](https://circleci.com/gh/Galigator/openllet)
[![codecov](https://codecov.io/gh/Galigator/openllet/branch/integration/graph/badge.svg)](https://codecov.io/gh/Galigator/openllet)
