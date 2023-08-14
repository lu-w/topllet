# `openllet-tcq` Module

This module implements an answering engine for metric temporal conjunctive queries.
It takes as inputs a temporal knowledge base in form of a `.kbs` file and a query in form of a `.tcq` file.
It outputs a list of answers, i.e., mappings from individuals to the query's answer variables.

## Inputs

### Temporal Knowledge Bases

kbs files
list of OWL2 KBs
right now, we assume all individuals to be present in the first KB

### Temporal Conjunctive Queries

Grammatik, insb. refer to LTLf grammar and extend it)
convention: answer variables with ?var, undist. vars are just var (but they shall not have an individual name)
you can use prefixes

## Output

- note: we assume ?x!=?y for all variables implicitly, paper assumes otherwise

## Algorithms

- All source code implementing new methods have comments detailing the implementation, with references to the paper where each step comes from