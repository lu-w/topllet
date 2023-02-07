package openllet.owlapi;

import static org.semanticweb.owlapi.util.OWLAPIStreamUtils.asList;

import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitor;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;

import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.owlapi.facet.FacetReasonerOWL;
import openllet.query.sparqldl.engine.cq.QueryEngine;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtomFactory;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.results.QueryResult;

public class EntailmentQueryVisitor implements OWLAxiomVisitor, FacetReasonerOWL
{
	private final IndividualTermConverter _indConv;

	private final OpenlletReasoner _reasoner;

	private ConjunctiveQuery _query;

	@Override
	public OpenlletReasoner getReasoner()
	{
		return _reasoner;
	}

	private class IndividualTermConverter implements OWLIndividualVisitor
	{

		private ATermAppl _term;

		public ATermAppl getTerm(final OWLIndividual individual)
		{
			_term = null;
			individual.accept(this);
			return _term;
		}

		//@Override
		@Override
		public void visit(final OWLNamedIndividual individual)
		{
			_term = _reasoner.term(individual);
		}

		//@Override
		@Override
		public void visit(final OWLAnonymousIndividual individual)
		{
			_term = ATermUtils.makeVar(individual.toStringID());
		}

	}

	public EntailmentQueryVisitor(final OpenlletReasoner reasoner)
	{
		_reasoner = reasoner;
		_indConv = new IndividualTermConverter();
		reset();
	}

	public boolean isEntailed()
	{
		final QueryResult results = QueryEngine.exec(_query);
		return !results.isEmpty();

	}

	public void reset()
	{
		_query = new ConjunctiveQueryImpl(_reasoner.getKB(), false);
	}

	@Override
	public void visit(final OWLClassAssertionAxiom axiom)
	{
		final ATermAppl ind = _indConv.getTerm(axiom.getIndividual());
		final ATermAppl cls = _reasoner.term(axiom.getClassExpression());
		_query.add(QueryAtomFactory.TypeAtom(ind, cls));
	}

	@Override
	public void visit(final OWLDataPropertyAssertionAxiom axiom)
	{
		final ATermAppl subj = _indConv.getTerm(axiom.getSubject());
		final ATermAppl pred = _reasoner.term(axiom.getProperty());
		final ATermAppl obj = _reasoner.term(axiom.getObject());
		_query.add(QueryAtomFactory.PropertyValueAtom(subj, pred, obj));
	}

	@Override
	public void visit(final OWLDifferentIndividualsAxiom axiom)
	{
		final List<ATermAppl> differents = new ArrayList<>();
		axiom.individuals().forEach(ind ->
		{
			final ATermAppl term = _indConv.getTerm(ind);
			for (final ATermAppl dterm : differents)
				_query.add(QueryAtomFactory.DifferentFromAtom(term, dterm));
		});
	}

	@Override
	public void visit(final OWLNegativeDataPropertyAssertionAxiom axiom)
	{
		final ATermAppl subj = _indConv.getTerm(axiom.getSubject());
		final ATermAppl pred = _reasoner.term(axiom.getProperty());
		final ATermAppl obj = _reasoner.term(axiom.getObject());
		_query.add(QueryAtomFactory.NegativePropertyValueAtom(subj, pred, obj));
	}

	@Override
	public void visit(final OWLNegativeObjectPropertyAssertionAxiom axiom)
	{
		final ATermAppl subj = _indConv.getTerm(axiom.getSubject());
		final ATermAppl pred = _reasoner.term(axiom.getProperty());
		final ATermAppl obj = _indConv.getTerm(axiom.getObject());
		_query.add(QueryAtomFactory.NegativePropertyValueAtom(subj, pred, obj));
	}

	@Override
	public void visit(final OWLObjectPropertyAssertionAxiom axiom)
	{
		final ATermAppl subj = _indConv.getTerm(axiom.getSubject());
		final ATermAppl pred = _reasoner.term(axiom.getProperty());
		final ATermAppl obj = _indConv.getTerm(axiom.getObject());
		_query.add(QueryAtomFactory.PropertyValueAtom(subj, pred, obj));
	}

	@Override
	public void visit(final OWLSameIndividualAxiom axiom)
	{
		ATermAppl head = null;
		for (final OWLIndividual ind : asList(axiom.individuals()))
		{
			final ATermAppl term = _indConv.getTerm(ind);
			if (head == null)
				head = term;
			else
				_query.add(QueryAtomFactory.SameAsAtom(head, term));
		}
	}

}
