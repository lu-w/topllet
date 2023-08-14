# `openllet-tcq` Module

This module implements an answering engine for metric temporal conjunctive queries.
It takes as inputs a temporal knowledge base in form of a `.kbs` file and a query in form of a `.tcq` file.
It outputs a list of answers, i.e., mappings from individuals to the query's answer variables.

# Usage

The temporal query functionality of `openllet` is executed like this:

`openllet temporal-query -q query.tcq aboxes.kbs`

The example files can be found in `../examples/src/main/resources/data/tcq`.
Just navigate to the folder and execute `openllet temporal-query` for a quick demo.

We now explain what both inputs are.

## Inputs

As seen above, `openllet temporal-query` takes two inputs:

1. A query
2. A temporal knowledge base

### Temporal Conjunctive Queries

We use `.tcq` files to specify temporal conjunctive queries.
An example is the following query:

```
PREFIX t: <http://www.semanticweb.org/mtcq/example#>

G(t:A(?x)) & F(t:C(?y))
```

It thus consists of a list of prefixes (which can be empty), of the format

`PREFIX string <string>`

This is analogous to the SPARQL prefix syntax.
It is followed by an MTCQ, which roughly adheres to [this](https://github.com/marcofavorito/tl-grammars/blob/main/content/04.ltlf.md) LTLf grammar, with two exceptions.

Firstly, it allows for additional operators bounded by non-negative integers `a` and `b` with `b >= a`:

- `x U_[a,b] y`
- `G_[a,b] x`
- `F_[a,b] x`
- `x U_<=a y`
- `G_<=a x`
- `F_<=a x`

Secondly, instead of Boolean formulae over atomic propositions, you can specify Conjunctive Queries (CQs).
An example CQ is `(t:A(?x) ^ t:C(?y) ^ t:A(t:a))`.
CQs always need to be enclosed by brackets and the single conjuncts (called query atoms) are joined by the `^` operator.
For the query atoms, you can use all concepts and roles existing in the given ontology, using the appropriate prefix, preceeded by a bracketed name (in case of a concept) or a tuple of names (in case of a role).
These names are either answer variables, individuals, or existentially quantified variables.
Answer variables are preceeded by a `?`.
If you want to refer to a certain individual, just use the individual's name (`t:a` in the example).
If you specify a non-`?`-preceeded, non-individual variable in some query atom, it is interpreted as an existentially quantified (also called undistinguished) variable.

You can add comments by `# my comment`.
For inline comments, note that the `#` needs to be succeeded by a whitespace (i.e., `#mycomment` is not a valid comment).

### Temporal Knowledge Bases

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

## Output

Query results (i.e., the certain ansewrs) are displayed to the user after the execution has finished.

Note that, when answering the given query, the implementation assumes `x != y` for all answer variables `x` and `y`, as this seems to be the more natural behavior by default.
This means that if `x` is answered by the individual `a`, `y` can not be mapped to `a` anymore.

Use the `-v` option to get some more feedback during computation.
Even more granular control can be applied by setting a `logging.properties`, as explained in the top-level `README.md`.

## Algorithms

If you wish to comprehend the implementation, the `TCQEngine` class in `src/main/java/openllet/tcq/engine` is a good starting point.
From there, the relevant steps are documented.
