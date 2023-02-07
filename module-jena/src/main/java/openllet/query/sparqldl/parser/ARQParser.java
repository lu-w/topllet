// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the _terms of the Affero General Public
// License v3.
//
// Please see LICENSE.txt for full license _terms, including the availability of
// proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.query.sparqldl.parser;

import static openllet.core.utils.TermFactory.BOTTOM;
import static openllet.core.utils.TermFactory.BOTTOM_DATA_PROPERTY;
import static openllet.core.utils.TermFactory.BOTTOM_OBJECT_PROPERTY;
import static openllet.core.utils.TermFactory.TOP;
import static openllet.core.utils.TermFactory.TOP_DATA_PROPERTY;
import static openllet.core.utils.TermFactory.TOP_OBJECT_PROPERTY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementPathBlock;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import openllet.aterm.ATerm;
import openllet.aterm.ATermAppl;
import openllet.aterm.ATermList;
import openllet.atom.OpenError;
import openllet.core.KnowledgeBase;
import openllet.core.OpenlletOptions;
import openllet.core.PropertyType;
import openllet.core.boxes.rbox.Role;
import openllet.core.exceptions.UnsupportedFeatureException;
import openllet.core.exceptions.UnsupportedQueryException;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.TermFactory;
import openllet.jena.BuiltinTerm;
import openllet.jena.JenaUtils;
import openllet.jena.vocabulary.OWL2;
import openllet.query.sparqldl.model.cq.Query;
import openllet.query.sparqldl.model.ucq.UnionQuery.VarType;
import openllet.query.sparqldl.model.cq.QueryAtomFactory;
import openllet.query.sparqldl.model.cq.QueryImpl;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: Parser for the SPARQL-DL based on ARQ
 * </p>
 * <p>
 * Description: Meanwhile does not deal with types of variables.
 * </p>
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Petr Kremen
 */
public class ARQParser implements QueryParser
{
	public static Logger _logger = Log.getLogger(ARQParser.class);

	private Set<Triple> _triples;

	private Map<Node, ATerm> _terms;

	private KnowledgeBase _kb;

	private QuerySolution _initialBinding;

	/*
	 * If this variable is true then queries with variable SPO statements are
	 * not handled by the SPARQL-DL engine but fall back to ARQ
	 */
	private boolean _handleVariableSPO = true;

	public ARQParser()
	{
		this(true);
	}

	public ARQParser(final boolean handleVariableSPO)
	{
		_handleVariableSPO = handleVariableSPO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query parse(final InputStream stream, final KnowledgeBase kb)
	{
		try
		{
			return parse(new InputStreamReader(stream), kb);
		}
		catch (final IOException e)
		{
			final String message = "Error creating a reader from the input stream.";
			_logger.log(Level.SEVERE, message, e);
			throw new OpenError(message);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Query parse(final String queryStr, final KnowledgeBase kb)
	{
		final org.apache.jena.query.Query sparql = QueryFactory.create(queryStr, Syntax.syntaxSPARQL);

		return parse(sparql, kb);
	}

	private Query parse(final Reader in, final KnowledgeBase kb) throws IOException
	{
		final StringBuffer queryString = new StringBuffer();
		final BufferedReader r = new BufferedReader(in);

		String line = r.readLine();
		while (line != null)
		{
			queryString.append(line).append("\n");
			line = r.readLine();
		}

		return parse(queryString.toString(), kb);
	}

	@Override
	public Query parse(final org.apache.jena.query.Query sparql, final KnowledgeBase kb)
	{
		_kb = kb;

		if (sparql.isDescribeType())
			throw new UnsupportedQueryException("DESCRIBE queries cannot be answered with PelletQueryEngine");

		final Element pattern = sparql.getQueryPattern();

		if (!(pattern instanceof ElementGroup))
			throw new UnsupportedQueryException("ElementGroup was _expected, but found '" + pattern.getClass() + "'.");

		final ElementGroup elementGroup = (ElementGroup) pattern;

		final List<Element> elements = elementGroup.getElements();
		final Element first = elements.get(0);
		if (elements.size() != 1 || !(first instanceof ElementTriplesBlock) && !(first instanceof ElementPathBlock))
			throw new UnsupportedQueryException("Complex query patterns are not supported yet.");

		List<Triple> triples;
		if (first instanceof ElementPathBlock)
		{
			triples = new ArrayList<>();
			for (final TriplePath path : ((ElementPathBlock) first).getPattern())
			{
				if (!path.isTriple())
					throw new UnsupportedQueryException("Path expressions are not supported yet.");
				triples.add(path.asTriple());
			}
		}
		else
			triples = ((ElementTriplesBlock) first).getPattern().getList();

		// very important to call this function so that getResultVars() will
		// work fine for SELECT * queries
		sparql.setResultVars();

		return parse(triples, sparql.getResultVars(), kb, sparql.isDistinct());
	}

	private void initBuiltinTerms()
	{
		_terms = new HashMap<>();

		_terms.put(OWL.Thing.asNode(), TOP);
		_terms.put(OWL.Nothing.asNode(), BOTTOM);
		_terms.put(OWL2.topObjectProperty.asNode(), TOP_OBJECT_PROPERTY);
		_terms.put(OWL2.topDataProperty.asNode(), TOP_DATA_PROPERTY);
		_terms.put(OWL2.bottomObjectProperty.asNode(), BOTTOM_OBJECT_PROPERTY);
		_terms.put(OWL2.bottomDataProperty.asNode(), BOTTOM_DATA_PROPERTY);
	}

	public Query parse(final BasicPattern basicPattern, final Collection<?> resultVars, final KnowledgeBase kb, final boolean isDistinct) throws UnsupportedQueryException
	{
		return parse(basicPattern.getList(), resultVars, kb, isDistinct);
	}

	public Query parse(final List<Triple> basicPattern, final Collection<?> resultVars, final KnowledgeBase kb, final boolean isDistinct) throws UnsupportedQueryException
	{
		_kb = kb;

		// This set contains predicates that are distinguished variables. The
		// elements are accumulated for PropertyValueAtom and removed if used in
		// subject position of other SPARQL-DL query atoms. If the set
		// is not empty, we throw an unsupported query exception and fall back
		// to ARQ to process the query. This solves the problem of {} ?p {}
		// queries where ?p is not used as subject in other patterns
		final Set<ATermAppl> variablePredicates = new HashSet<>();
		// This set contains subjects that are distinguished variables and is
		// used to collect variables along the way while processing triple
		// patterns. The list is used to decide whether or not the variable
		// property of a pattern {} ?p {} has to be accumulated to the
		// variablePredicates set. This avoids to add them for the case where
		// the variable in predicate position is bound to a subject of another
		// triple pattern, e.g. ?p rdf:type owl:ObjectProperty . ?s ?p ?o
		final Set<ATermAppl> variableSubjects = new HashSet<>();

		initBuiltinTerms();

		// Make sure to resolve the query parameterization first, i.e.
		// substitute the variables with initial bindings, if applicable
		_triples = new LinkedHashSet<>(resolveParameterization(basicPattern));

		final Query query = new QueryImpl(kb, isDistinct);

		for (final Object name : resultVars)
		{
			final String var = (String) name;

			query.addResultVar(ATermUtils.makeVar(var));
		}

		for (final Triple t : new ArrayList<>(_triples))
		{
			if (!_triples.contains(t))
				continue;

			final Node subj = t.getSubject();
			final Node pred = t.getPredicate();
			final Node obj = t.getObject();

			if (BuiltinTerm.isSyntax(pred) || BuiltinTerm.isSyntax(obj))
				continue;

			cache(subj);
			cache(pred);
			cache(obj);
		}

		final Set<ATermAppl> possibleLiteralVars = new HashSet<>();

		//throw exception if _triples is empty
		if (_triples.isEmpty())
			throw new UnsupportedQueryException("Empty BGT");

		for (final Triple t : _triples)
		{

			final Node subj = t.getSubject();
			final Node pred = t.getPredicate();
			final Node obj = t.getObject();

			final ATermAppl s = (ATermAppl) _terms.get(subj);
			final ATermAppl p = (ATermAppl) _terms.get(pred);
			final ATermAppl o = (ATermAppl) _terms.get(obj);

			if (pred.equals(RDF.Nodes.type))
			{
				// Map ?c rdf:type owl:Class to SubClassOf(?c owl:Thing)
				if (obj.equals(OWL.Class.asNode()))
				{
					query.add(QueryAtomFactory.SubClassOfAtom(s, TermFactory.TOP));
					if (ATermUtils.isVar(s))
					{
						ensureDistinguished(subj);
						query.addDistVar(s, VarType.CLASS);
						if (_handleVariableSPO)
						{
							variablePredicates.remove(s);
							variableSubjects.add(s);
						}
					}
				}

				//NamedIndividual(p)
				else
					if (obj.equals(OWL2.NamedIndividual.asNode()))
					{
						query.add(QueryAtomFactory.TypeAtom(s, TermFactory.TOP));
						if (ATermUtils.isVar(s))
						{
							ensureDistinguished(subj);
							query.addDistVar(s, VarType.CLASS);
							if (_handleVariableSPO)
							{
								variablePredicates.remove(s);
								variableSubjects.add(s);
							}
						}
					}

					// ObjectProperty(p)
					else
						if (obj.equals(OWL.ObjectProperty.asNode()))
						{
							query.add(QueryAtomFactory.ObjectPropertyAtom(s));
							if (ATermUtils.isVar(s))
							{
								ensureDistinguished(subj);
								query.addDistVar(s, VarType.PROPERTY);
								if (_handleVariableSPO)
								{
									variablePredicates.remove(s);
									variableSubjects.add(s);
								}
							}
							else
								ensureTypedProperty(s);
						}

						// DatatypeProperty(p)
						else
							if (obj.equals(OWL.DatatypeProperty.asNode()))
							{
								query.add(QueryAtomFactory.DatatypePropertyAtom(s));
								if (ATermUtils.isVar(s))
								{
									ensureDistinguished(subj);
									query.addDistVar(s, VarType.PROPERTY);
									if (_handleVariableSPO)
									{
										variablePredicates.remove(s);
										variableSubjects.add(s);
									}
								}
								else
									ensureTypedProperty(s);
							}

							// Property(p)
							else
								if (obj.equals(RDF.Property.asNode()))
								{
									if (ATermUtils.isVar(s))
									{
										ensureDistinguished(subj);
										query.addDistVar(s, VarType.PROPERTY);
										if (_handleVariableSPO)
										{
											variablePredicates.remove(s);
											variableSubjects.add(s);
										}
									}
									else
										ensureTypedProperty(s);
								}

								// Functional(p)
								else
									if (obj.equals(OWL.FunctionalProperty.asNode()))
									{
										query.add(QueryAtomFactory.FunctionalAtom(s));
										if (ATermUtils.isVar(s))
										{
											ensureDistinguished(subj);
											query.addDistVar(s, VarType.PROPERTY);
											if (_handleVariableSPO)
											{
												variablePredicates.remove(s);
												variableSubjects.add(s);
											}
										}
										else
											ensureTypedProperty(s);
									}

									// InverseFunctional(p)
									else
										if (obj.equals(OWL.InverseFunctionalProperty.asNode()))
										{
											query.add(QueryAtomFactory.InverseFunctionalAtom(s));
											if (ATermUtils.isVar(s))
											{
												ensureDistinguished(subj);
												query.addDistVar(s, VarType.PROPERTY);
												if (_handleVariableSPO)
												{
													variablePredicates.remove(s);
													variableSubjects.add(s);
												}
											}
											else
												ensureTypedProperty(s);
										}

										// Transitive(p)
										else
											if (obj.equals(OWL.TransitiveProperty.asNode()))
											{
												query.add(QueryAtomFactory.TransitiveAtom(s));
												if (ATermUtils.isVar(s))
												{
													ensureDistinguished(subj);
													query.addDistVar(s, VarType.PROPERTY);
													if (_handleVariableSPO)
													{
														variablePredicates.remove(s);
														variableSubjects.add(s);
													}
												}
												else
													ensureTypedProperty(s);
											}

											// Symmetric(p)
											else
												if (obj.equals(OWL.SymmetricProperty.asNode()))
												{
													query.add(QueryAtomFactory.SymmetricAtom(s));
													if (ATermUtils.isVar(s))
													{
														ensureDistinguished(subj);
														query.addDistVar(s, VarType.PROPERTY);
														if (_handleVariableSPO)
														{
															variablePredicates.remove(s);
															variableSubjects.add(s);
														}
													}
													else
														ensureTypedProperty(s);
												}

												// Asymmetric(p)
												else
													if (obj.equals(OWL2.AsymmetricProperty.asNode()))
													{
														query.add(QueryAtomFactory.AsymmetricAtom(s));
														if (ATermUtils.isVar(s))
														{
															ensureDistinguished(subj);
															query.addDistVar(s, VarType.PROPERTY);
															if (_handleVariableSPO)
															{
																variablePredicates.remove(s);
																variableSubjects.add(s);
															}
														}
														else
															ensureTypedProperty(s);
													}

													// Reflexive(p)
													else
														if (obj.equals(OWL2.ReflexiveProperty.asNode()))
														{
															query.add(QueryAtomFactory.ReflexiveAtom(s));
															if (ATermUtils.isVar(s))
															{
																ensureDistinguished(subj);
																query.addDistVar(s, VarType.PROPERTY);
																if (_handleVariableSPO)
																{
																	variablePredicates.remove(s);
																	variableSubjects.add(s);
																}
															}
															else
																ensureTypedProperty(s);
														}

														// Irreflexive(p)
														else
															if (obj.equals(OWL2.IrreflexiveProperty.asNode()))
															{
																query.add(QueryAtomFactory.IrreflexiveAtom(s));
																if (ATermUtils.isVar(s))
																{
																	ensureDistinguished(subj);
																	query.addDistVar(s, VarType.PROPERTY);
																	if (_handleVariableSPO)
																	{
																		variablePredicates.remove(s);
																		variableSubjects.add(s);
																	}
																}
																else
																	ensureTypedProperty(s);
															}

															// Annotation(s,pa,o)
															else
																if (hasObject(pred, RDF.type.asNode(), OWL.AnnotationProperty.asNode()))
																{
																	query.add(QueryAtomFactory.AnnotationAtom(s, p, o));
																	if (ATermUtils.isVar(s) || ATermUtils.isVar(p) || ATermUtils.isVar(o))
																		throw new UnsupportedQueryException("Variables in annotation atom are not supported.");
																	else
																		ensureTypedProperty(p);
																}

																// Type(i,c)
																else
																{
																	query.add(QueryAtomFactory.TypeAtom(s, o));

																	if (ATermUtils.isVar(o))
																	{
																		ensureDistinguished(obj);
																		query.addDistVar(o, VarType.CLASS);
																	}
																	else
																		if (!kb.isClass(o))
																			if (_logger.isLoggable(Level.FINE))
																				_logger.fine("Class " + o + " used in the query is not defined in the KB.");

																	if (isDistinguishedVariable(subj))
																		query.addDistVar(s, VarType.INDIVIDUAL);
																}
			}

			// SameAs(i1,i2)
			else
				if (pred.equals(OWL.sameAs.asNode()))
				{
					query.add(QueryAtomFactory.SameAsAtom(s, o));
					if (isDistinguishedVariable(subj))
						query.addDistVar(s, VarType.INDIVIDUAL);

					if (isDistinguishedVariable(obj))
						query.addDistVar(o, VarType.INDIVIDUAL);

				}

				// DifferentFrom(i1,i2)
				else
					if (pred.equals(OWL.differentFrom.asNode()))
					{
						query.add(QueryAtomFactory.DifferentFromAtom(s, o));
						if (isDistinguishedVariable(subj))
							query.addDistVar(s, VarType.INDIVIDUAL);

						if (isDistinguishedVariable(obj))
							query.addDistVar(o, VarType.INDIVIDUAL);

					}

					// SubClassOf(c1,c2)
					else
						if (pred.equals(RDFS.subClassOf.asNode()))
						{
							query.add(QueryAtomFactory.SubClassOfAtom(s, o));
							if (ATermUtils.isVar(s))
							{
								ensureDistinguished(subj);
								query.addDistVar(s, VarType.CLASS);
							}
							if (ATermUtils.isVar(o))
							{
								ensureDistinguished(obj);
								query.addDistVar(o, VarType.CLASS);
							}
						}

						// strict subclass - nonmonotonic
						else
							if (pred.equals(SparqldlExtensionsVocabulary.strictSubClassOf.asNode()))
							{
								query.add(QueryAtomFactory.StrictSubClassOfAtom(s, o));
								if (ATermUtils.isVar(s))
								{
									ensureDistinguished(subj);
									query.addDistVar(s, VarType.CLASS);
								}
								if (ATermUtils.isVar(o))
								{
									ensureDistinguished(obj);
									query.addDistVar(o, VarType.CLASS);
								}
							}

							// direct subclass - nonmonotonic
							else
								if (pred.equals(SparqldlExtensionsVocabulary.directSubClassOf.asNode()))
								{
									query.add(QueryAtomFactory.DirectSubClassOfAtom(s, o));
									if (ATermUtils.isVar(s))
									{
										ensureDistinguished(subj);
										query.addDistVar(s, VarType.CLASS);
									}
									if (ATermUtils.isVar(o))
									{
										ensureDistinguished(obj);
										query.addDistVar(o, VarType.CLASS);
									}
								}

								// EquivalentClass(c1,c2)
								else
									if (pred.equals(OWL.equivalentClass.asNode()))
									{
										query.add(QueryAtomFactory.EquivalentClassAtom(s, o));
										if (ATermUtils.isVar(s))
										{
											ensureDistinguished(subj);
											query.addDistVar(s, VarType.CLASS);
										}
										if (ATermUtils.isVar(o))
										{
											ensureDistinguished(obj);
											query.addDistVar(o, VarType.CLASS);
										}
									}

									// DisjointWith(c1,c2)
									else
										if (pred.equals(OWL.disjointWith.asNode()))
										{
											query.add(QueryAtomFactory.DisjointWithAtom(s, o));
											if (ATermUtils.isVar(s))
											{
												ensureDistinguished(subj);
												query.addDistVar(s, VarType.CLASS);
											}
											if (ATermUtils.isVar(o))
											{
												ensureDistinguished(obj);
												query.addDistVar(o, VarType.CLASS);
											}

										}

										// ComplementOf(c1,c2)
										else
											if (pred.equals(OWL.complementOf.asNode()))
											{
												query.add(QueryAtomFactory.ComplementOfAtom(s, o));
												if (ATermUtils.isVar(s))
												{
													ensureDistinguished(subj);
													query.addDistVar(s, VarType.CLASS);
												}
												if (ATermUtils.isVar(o))
												{
													ensureDistinguished(obj);
													query.addDistVar(o, VarType.CLASS);
												}
											}

											// propertyDisjointWith(p1,p2)
											else
												if (pred.equals(OWL2.propertyDisjointWith.asNode()))
												{
													ensureTypedProperty(s);
													ensureTypedProperty(o);

													query.add(QueryAtomFactory.PropertyDisjointWithAtom(s, o));

													if (ATermUtils.isVar(s))
													{
														ensureDistinguished(subj);
														query.addDistVar(s, VarType.PROPERTY);
														if (_handleVariableSPO)
														{
															variablePredicates.remove(s);
															variableSubjects.add(s);
														}
													}
													if (ATermUtils.isVar(o))
													{
														ensureDistinguished(obj);
														query.addDistVar(o, VarType.PROPERTY);
														if (_handleVariableSPO)
														{
															variablePredicates.remove(o);
															variableSubjects.add(o);
														}
													}

												}

												// SubPropertyOf(p1,p2)
												else
													if (pred.equals(RDFS.subPropertyOf.asNode()))
													{
														ensureTypedProperty(s);
														ensureTypedProperty(o);

														query.add(QueryAtomFactory.SubPropertyOfAtom(s, o));
														if (ATermUtils.isVar(s))
														{
															ensureDistinguished(subj);
															query.addDistVar(s, VarType.PROPERTY);
															if (_handleVariableSPO)
															{
																variablePredicates.remove(s);
																variableSubjects.add(s);
															}
														}
														if (ATermUtils.isVar(o))
														{
															ensureDistinguished(obj);
															query.addDistVar(o, VarType.PROPERTY);
															if (_handleVariableSPO)
															{
																variablePredicates.remove(o);
																variableSubjects.add(o);
															}
														}
													}

													// DirectSubPropertyOf(i,p) - nonmonotonic
													else
														if (pred.equals(SparqldlExtensionsVocabulary.directSubPropertyOf.asNode()))
														{
															ensureTypedProperty(s);
															ensureTypedProperty(o);

															query.add(QueryAtomFactory.DirectSubPropertyOfAtom(s, o));
															if (ATermUtils.isVar(s))
															{
																ensureDistinguished(subj);
																query.addDistVar(s, VarType.PROPERTY);
																if (_handleVariableSPO)
																{
																	variablePredicates.remove(s);
																	variableSubjects.add(s);
																}
															}
															if (ATermUtils.isVar(o))
															{
																ensureDistinguished(obj);
																query.addDistVar(o, VarType.PROPERTY);
																if (_handleVariableSPO)
																{
																	variablePredicates.remove(o);
																	variableSubjects.add(o);
																}
															}
														}

														// StrictSubPropertyOf(i,p) - nonmonotonic
														else
															if (pred.equals(SparqldlExtensionsVocabulary.strictSubPropertyOf.asNode()))
															{
																ensureTypedProperty(s);
																ensureTypedProperty(o);

																query.add(QueryAtomFactory.StrictSubPropertyOfAtom(s, o));
																if (ATermUtils.isVar(s))
																{
																	ensureDistinguished(subj);
																	query.addDistVar(s, VarType.PROPERTY);
																	if (_handleVariableSPO)
																	{
																		variablePredicates.remove(s);
																		variableSubjects.add(s);
																	}
																}
																if (ATermUtils.isVar(o))
																{
																	ensureDistinguished(obj);
																	query.addDistVar(o, VarType.PROPERTY);
																	if (_handleVariableSPO)
																	{
																		variablePredicates.remove(o);
																		variableSubjects.add(o);
																	}
																}
															}

															// EquivalentProperty(p1,p2)
															else
																if (pred.equals(OWL.equivalentProperty.asNode()))
																{
																	ensureTypedProperty(s);
																	ensureTypedProperty(o);

																	query.add(QueryAtomFactory.EquivalentPropertyAtom(s, o));
																	if (ATermUtils.isVar(s))
																	{
																		ensureDistinguished(subj);
																		query.addDistVar(s, VarType.PROPERTY);
																		if (_handleVariableSPO)
																		{
																			variablePredicates.remove(s);
																			variableSubjects.add(s);
																		}
																	}
																	if (ATermUtils.isVar(o))
																	{
																		ensureDistinguished(obj);
																		query.addDistVar(o, VarType.PROPERTY);
																		if (_handleVariableSPO)
																		{
																			variablePredicates.remove(o);
																			variableSubjects.add(o);
																		}
																	}
																}
																// Domain(p1, c)
																else
																	if (pred.equals(RDFS.domain.asNode()))
																	{
																		ensureTypedProperty(s);

																		query.add(QueryAtomFactory.DomainAtom(s, o));
																		if (ATermUtils.isVar(s))
																		{
																			ensureDistinguished(subj);
																			query.addDistVar(s, VarType.PROPERTY);
																			if (_handleVariableSPO)
																			{
																				variablePredicates.remove(s);
																				variableSubjects.add(s);
																			}
																		}
																		if (ATermUtils.isVar(o))
																		{
																			ensureDistinguished(obj);
																			query.addDistVar(s, VarType.CLASS);
																		}
																	}
																	// Range(p1, c)
																	else
																		if (pred.equals(RDFS.range.asNode()))
																		{
																			ensureTypedProperty(s);

																			query.add(QueryAtomFactory.RangeAtom(s, o));
																			if (ATermUtils.isVar(s))
																			{
																				ensureDistinguished(subj);
																				query.addDistVar(s, VarType.PROPERTY);
																				if (_handleVariableSPO)
																				{
																					variablePredicates.remove(s);
																					variableSubjects.add(s);
																				}
																			}
																			if (ATermUtils.isVar(o))
																			{
																				ensureDistinguished(obj);
																				// TODO it could also range over datatypes.
																				query.addDistVar(s, VarType.CLASS);
																			}
																		}
																		// InverseOf(p1,p2)
																		else
																			if (pred.equals(OWL.inverseOf.asNode()))
																			{
																				ensureTypedProperty(s);
																				ensureTypedProperty(o);

																				query.add(QueryAtomFactory.InverseOfAtom(s, o));
																				if (ATermUtils.isVar(s))
																				{
																					ensureDistinguished(subj);
																					query.addDistVar(s, VarType.PROPERTY);
																					if (_handleVariableSPO)
																					{
																						variablePredicates.remove(s);
																						variableSubjects.add(s);
																					}
																				}
																				if (ATermUtils.isVar(o))
																				{
																					ensureDistinguished(obj);
																					query.addDistVar(o, VarType.PROPERTY);
																					if (_handleVariableSPO)
																					{
																						variablePredicates.remove(o);
																						variableSubjects.add(o);
																					}
																				}
																			}

																			// DirectType(i,c) - nonmonotonic
																			else
																				if (pred.equals(SparqldlExtensionsVocabulary.directType.asNode()))
																				{
																					query.add(QueryAtomFactory.DirectTypeAtom(s, o));
																					if (isDistinguishedVariable(subj))
																						query.addDistVar(s, VarType.INDIVIDUAL);
																					if (ATermUtils.isVar(o))
																					{
																						ensureDistinguished(obj);
																						query.addDistVar(o, VarType.CLASS);
																					}
																				}

																				else
																					if (kb.isAnnotationProperty(p))
																					{
																						if (!OpenlletOptions.USE_ANNOTATION_SUPPORT)
																							throw new UnsupportedQueryException("Cannot answer annotation queries when PelletOptions.USE_ANNOTATION_SUPPORT is false!");

																						query.add(QueryAtomFactory.AnnotationAtom(s, p, o));
																						if (ATermUtils.isVar(s))
																						{
																							ensureDistinguished(subj);
																							query.addDistVar(s, VarType.PROPERTY);
																							if (_handleVariableSPO)
																							{
																								variablePredicates.remove(s);
																								variableSubjects.add(s);
																							}
																						}
																						if (ATermUtils.isVar(o))
																						{
																							ensureDistinguished(obj);
																							query.addDistVar(o, VarType.PROPERTY);
																							if (_handleVariableSPO)
																							{
																								variablePredicates.remove(o);
																								variableSubjects.add(o);
																							}
																						}
																						// throw new UnsupportedQueryException(
																						// "Annotation properties are not supported in queries." );
																					}

																					// PropertyValue(i,p,j)
																					else
																					{
																						if (s == null || p == null || o == null)
																							throw new UnsupportedQueryException("Atom conversion incomplete for: " + t);
																						ensureTypedProperty(p);

																						query.add(QueryAtomFactory.PropertyValueAtom(s, p, o));

																						if (ATermUtils.isVar(p))
																						{
																							ensureDistinguished(pred);
																							query.addDistVar(p, VarType.PROPERTY);

																							// If the predicate is a variable used in a subject position
																							// we don't have to consider it as it is bound to another
																							// triple pattern
																							if (!variableSubjects.contains(p))
																								variablePredicates.add(p);
																						}

																						if (isDistinguishedVariable(subj))
																							query.addDistVar(s, VarType.INDIVIDUAL);

																						if (isDistinguishedVariable(obj))
																							if (ATermUtils.isVar(p))
																								possibleLiteralVars.add(o);
																							else
																								if (kb.isObjectProperty(p))
																									query.addDistVar(o, VarType.INDIVIDUAL);
																								else
																									if (kb.isDatatypeProperty(p))
																										query.addDistVar(o, VarType.LITERAL);
																					}
		}

		for (final ATermAppl v : possibleLiteralVars)
		{
			if (!query.getDistVars().contains(v))
				query.addDistVar(v, VarType.LITERAL);
			query.addDistVar(v, VarType.INDIVIDUAL);
		}

		if (!_handleVariableSPO)
			return query;

		if (variablePredicates.isEmpty())
			return query;

		throw new UnsupportedQueryException("Queries with variable predicates are not supported " + "(add the pattern {?p rdf:type owl:ObjectProperty} or" + " {?p rdf:type owl:DatatypeProperty} to the query)");

	}

	public void setInitialBinding(final QuerySolution initialBinding)
	{
		_initialBinding = initialBinding;
	}

	private static void ensureDistinguished(final Node pred)
	{
		ensureDistinguished(pred, "Non-distinguished variables in class and predicate positions are not supported : ");
	}

	private static void ensureDistinguished(final Node pred, final String errorNonDist)
	{
		if (!isDistinguishedVariable(pred))
			throw new UnsupportedQueryException(errorNonDist + pred);
	}

	private void ensureTypedProperty(final ATermAppl pred)
	{

		if (ATermUtils.isVar(pred))
			return;

		final Role r = _kb.getRole(pred);
		if (r == null)
			throw new UnsupportedQueryException("Unknown role: " + pred);

		if (r.isUntypedRole())
			throw new UnsupportedQueryException("Untyped role: " + pred);
	}

	public static boolean isDistinguishedVariable(final Node node)
	{
		return Var.isVar(node) && (Var.isNamedVar(node) || OpenlletOptions.TREAT_ALL_VARS_DISTINGUISHED);
	}

	private Node getObject(final Node subj, final Node pred)
	{
		for (final Iterator<Triple> i = _triples.iterator(); i.hasNext();)
		{
			final Triple t = i.next();
			if (subj.equals(t.getSubject()) && pred.equals(t.getPredicate()))
			{
				i.remove();
				return t.getObject();
			}
		}

		return null;
	}

	private boolean hasObject(final Node subj, final Node pred)
	{
		for (final Triple t : _triples)
			if (subj.equals(t.getSubject()) && pred.equals(t.getPredicate()))
				return true;

		return false;
	}

	private boolean hasObject(final Node subj, final Node pred, final Node obj)
	{
		for (final Iterator<Triple> i = _triples.iterator(); i.hasNext();)
		{
			final Triple t = i.next();
			if (subj.equals(t.getSubject()) && pred.equals(t.getPredicate()))
			{
				i.remove();
				if (obj.equals(t.getObject()))
					return true;
				throw new UnsupportedQueryException("Expecting rdf:type " + obj + " but found rdf:type " + t.getObject());
			}
		}

		return false;
	}

	private ATermList createList(final Node node)
	{
		if (node.equals(RDF.nil.asNode()))
			return ATermUtils.EMPTY_LIST;
		else
			if (_terms.containsKey(node))
				return (ATermList) _terms.get(node);

		hasObject(node, RDF.type.asNode(), RDF.List.asNode());

		final Node first = getObject(node, RDF.first.asNode());
		final Node rest = getObject(node, RDF.rest.asNode());

		if (first == null || rest == null)
			throw new UnsupportedQueryException("Invalid list structure: List " + node + " does not have a " + (first == null ? "rdf:first" : "rdf:rest") + " property.");

		final ATermList list = ATermUtils.makeList(node2term(first), createList(rest));

		_terms.put(node, list);

		return list;
	}

	private ATermAppl createRestriction(final Node node) throws UnsupportedFeatureException
	{
		ATermAppl aTerm = ATermUtils.TOP;

		hasObject(node, RDF.type.asNode(), OWL.Restriction.asNode());

		final Node p = getObject(node, OWL.onProperty.asNode());

		// TODO warning message: no owl:onProperty
		if (p == null)
			return aTerm;

		final ATermAppl pt = node2term(p);
		if (!_kb.isProperty(pt))
			throw new UnsupportedQueryException("Property " + pt + " is not present in KB.");

		// TODO warning message: multiple owl:onProperty
		Node o = null;
		if ((o = getObject(node, OWL.hasValue.asNode())) != null)
		{
			if (OpenlletOptions.USE_PSEUDO_NOMINALS)
			{
				if (o.isLiteral())
					aTerm = ATermUtils.makeMin(pt, 1, ATermUtils.TOP_LIT);
				else
				{
					final ATermAppl ind = ATermUtils.makeTermAppl(o.getURI());
					if (!_kb.isIndividual(ind))
						throw new UnsupportedQueryException("Individual " + ind + " is not present in KB.");

					final ATermAppl nom = ATermUtils.makeTermAppl(o.getURI() + "_nom");

					aTerm = ATermUtils.makeSomeValues(pt, nom);
				}
			}
			else
			{
				final ATermAppl ot = node2term(o);

				aTerm = ATermUtils.makeHasValue(pt, ot);
			}
		}
		else
			if ((o = getObject(node, OWL2.hasSelf.asNode())) != null)
			{
				final ATermAppl ot = node2term(o);

				if (ATermUtils.isVar(ot))
					throw new UnsupportedQueryException("Variables not supported in hasSelf restriction");
				else
					aTerm = ATermUtils.makeSelf(pt);
			}
			else
				if ((o = getObject(node, OWL.allValuesFrom.asNode())) != null)
				{
					final ATermAppl ot = node2term(o);

					if (ATermUtils.isVar(ot))
						throw new UnsupportedQueryException("Variables not supported in allValuesFrom restriction");
					else
						aTerm = ATermUtils.makeAllValues(pt, ot);
				}
				else
					if ((o = getObject(node, OWL.someValuesFrom.asNode())) != null)
					{
						final ATermAppl ot = node2term(o);

						if (ATermUtils.isVar(ot))
							throw new UnsupportedQueryException("Variables not supported in someValuesFrom restriction");
						else
							aTerm = ATermUtils.makeSomeValues(pt, ot);
					}
					else
						if ((o = getObject(node, OWL.minCardinality.asNode())) != null)
							aTerm = createCardinalityRestriction(node, OWL.minCardinality.asNode(), pt, o);
						else
							if ((o = getObject(node, OWL2.minQualifiedCardinality.asNode())) != null)
								aTerm = createCardinalityRestriction(node, OWL2.minQualifiedCardinality.asNode(), pt, o);
							else
								if ((o = getObject(node, OWL.maxCardinality.asNode())) != null)
									aTerm = createCardinalityRestriction(node, OWL.maxCardinality.asNode(), pt, o);
								else
									if ((o = getObject(node, OWL2.maxQualifiedCardinality.asNode())) != null)
										aTerm = createCardinalityRestriction(node, OWL2.maxQualifiedCardinality.asNode(), pt, o);
									else
										if ((o = getObject(node, OWL.cardinality.asNode())) != null)
											aTerm = createCardinalityRestriction(node, OWL.cardinality.asNode(), pt, o);
										else
											if ((o = getObject(node, OWL2.qualifiedCardinality.asNode())) != null)
												aTerm = createCardinalityRestriction(node, OWL2.qualifiedCardinality.asNode(), pt, o);
											else
											{
												// TODO print warning message (invalid restriction type)
											}

		return aTerm;
	}

	private ATermAppl createCardinalityRestriction(final Node node, final Node restrictionType, final ATermAppl pt, final Node card) throws UnsupportedQueryException
	{

		try
		{
			ATermAppl c = null;
			Node qualification = null;
			if ((qualification = getObject(node, OWL2.onClass.asNode())) != null)
			{
				if (qualification.isVariable())
					throw new UnsupportedQueryException("Variables not allowed in cardinality qualification");

				if (!_kb.isObjectProperty(pt))
					return null;
				c = node2term(qualification);
			}
			else
				if ((qualification = getObject(node, OWL2.onDataRange.asNode())) != null)
				{
					if (qualification.isVariable())
						throw new UnsupportedQueryException("Variables not allowed in cardinality qualification");

					if (!_kb.isDatatypeProperty(pt))
						return null;
					c = node2term(qualification);
				}
				else
				{
					final PropertyType propType = _kb.getPropertyType(pt);
					if (propType == PropertyType.OBJECT)
						c = ATermUtils.TOP;
					else
						if (propType == PropertyType.DATATYPE)
							c = ATermUtils.TOP_LIT;
						else
							c = ATermUtils.TOP;
				}

			final int cardinality = Integer.parseInt(card.getLiteralLexicalForm());

			if (restrictionType.equals(OWL.minCardinality.asNode()) || restrictionType.equals(OWL2.minQualifiedCardinality.asNode()))
				return ATermUtils.makeMin(pt, cardinality, c);
			else
				if (restrictionType.equals(OWL.maxCardinality.asNode()) || restrictionType.equals(OWL2.maxQualifiedCardinality.asNode()))
					return ATermUtils.makeMax(pt, cardinality, c);
				else
					return ATermUtils.makeCard(pt, cardinality, c);
		}
		catch (final Exception ex)
		{
			_logger.log(Level.WARNING, "Invalid cardinality", ex);
		}

		return null;
	}

	private ATermAppl node2term(final Node node)
	{
		if (!_terms.containsKey(node))
			cache(node);
		return (ATermAppl) _terms.get(node);
	}

	private void cache(final Node node)
	{
		if (_terms.containsKey(node) || BuiltinTerm.isBuiltin(node))
			return;

		ATerm aTerm = null;

		if (node.isLiteral())
			aTerm = JenaUtils.makeLiteral(node.getLiteral());
		else
			if (hasObject(node, OWL.onProperty.asNode()))
			{
				aTerm = createRestriction(node);
				_terms.put(node, aTerm);
			}
			else
				if (node.isBlank() || node.isVariable())
				{
					Node o = null;
					if ((o = getObject(node, OWL.intersectionOf.asNode())) != null)
					{
						final ATermList list = createList(o);
						hasObject(node, RDF.type.asNode(), OWL.Class.asNode());

						aTerm = ATermUtils.makeAnd(list);
					}
					else
						if ((o = getObject(node, OWL.unionOf.asNode())) != null)
						{
							final ATermList list = createList(o);
							hasObject(node, RDF.type.asNode(), OWL.Class.asNode());

							aTerm = ATermUtils.makeOr(list);
						}
						else
							if ((o = getObject(node, OWL.oneOf.asNode())) != null)
							{
								final ATermList list = createList(o);
								hasObject(node, RDF.type.asNode(), OWL.Class.asNode());

								ATermList result = ATermUtils.EMPTY_LIST;
								for (ATermList l = list; !l.isEmpty(); l = l.getNext())
								{
									final ATermAppl c = (ATermAppl) l.getFirst();
									if (OpenlletOptions.USE_PSEUDO_NOMINALS)
									{
										final ATermAppl nominal = ATermUtils.makeTermAppl(c.getName() + "_nominal");
										result = result.insert(nominal);
									}
									else
									{
										final ATermAppl nominal = ATermUtils.makeValue(c);
										result = result.insert(nominal);
									}
								}

								aTerm = ATermUtils.makeOr(result);
							}
							else
								if (Var.isBlankNodeVar(node) && (o = getObject(node, OWL.complementOf.asNode())) != null)
								{
									final ATermAppl complement = node2term(o);
									hasObject(node, RDF.type.asNode(), OWL.Class.asNode());

									aTerm = ATermUtils.makeNot(complement);
								}
								else
									if (node.isVariable())
										aTerm = ATermUtils.makeVar(node.getName());
									else
									{
										if ((o = getObject(node, OWL.complementOf.asNode())) != null)
											_logger.info("Blank _nodes in class variable positions are not supported");

										aTerm = ATermUtils.makeBnode(node.getBlankNodeId().toString());
									}
				}
				else
				{
					final String uri = node.getURI();

					aTerm = ATermUtils.makeTermAppl(uri);
				}

		_terms.put(node, aTerm);
	}

	/*
	 * Given a parameterized query, resolve the _node (SPO of a triple pattern)
	 * i.e. if it is a variable and the variable name is contained in the
	 * initial binding (as a parameter) resolve it, i.e. substitute the variable
	 * with the constant.
	 */
	private List<Triple> resolveParameterization(final List<?> triples)
	{
		if (triples == null)
			throw new NullPointerException("The set of _triples cannot be null");

		// Ensure that the initial binding is not a null pointer
		if (_initialBinding == null)
			_initialBinding = new QuerySolutionMap();

		final List<Triple> ret = new ArrayList<>();

		for (final Triple t : triples.toArray(new Triple[triples.size()]))
		{
			if (!triples.contains(t))
				continue;

			final Node s = resolveParameterization(t.getSubject());
			final Node p = resolveParameterization(t.getPredicate());
			final Node o = resolveParameterization(t.getObject());

			ret.add(Triple.create(s, p, o));
		}

		return ret;
	}

	private Node resolveParameterization(final Node node)
	{
		if (node == null)
			throw new NullPointerException("Node is null");
		if (_initialBinding == null)
			throw new NullPointerException("Initial binding is null");

		if (node.isConcrete())
			return node;

		final RDFNode binding = _initialBinding.get(node.getName());

		if (binding == null)
			return node;

		return binding.asNode();
	}
}
