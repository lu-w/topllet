package openllet.mtcq.model.query;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.QueryPredicate;

public class ConjunctiveQueryFormula extends MTCQFormula
{
    private final ConjunctiveQuery _cq;

    public ConjunctiveQueryFormula(TemporalKnowledgeBase temporalKb, boolean isDistinct,
                                   ConjunctiveQuery conjunctiveQuery)
    {
        super(temporalKb, isDistinct);
        _cq = conjunctiveQuery;
        addQuery(_cq);
    }

    public ConjunctiveQueryFormula(MTCQFormula parentFormula, ConjunctiveQuery conjunctiveQuery)
    {
        this(parentFormula.getTemporalKB(), parentFormula.isDistinct(), conjunctiveQuery);
    }

    @Override
    public void setKB(KnowledgeBase kb)
    {
        super.setKB(kb);
        _cq.setKB(kb);
    }

    @Override
    public boolean isTemporal()
    {
        return false;
    }

    public ConjunctiveQuery getConjunctiveQuery()
    {
        return _cq;
    }

    @Override
    public String toString(PropositionFactory propositions)
    {
        StringBuilder cqString = new StringBuilder();
        if (propositions == null)
        {
            for (int i = 0; i < _cq.getAtoms().size(); i++)
            {
                QueryAtom atom = _cq.getAtoms().get(i);
                QueryPredicate pred = atom.getPredicate();
                if (pred == QueryPredicate.Type)
                {
                    cqString.append(atom.getArguments().get(1)).
                            append("(").
                            append(prefix(atom.getArguments().get(0))).
                            append(")");
                }
                else if (pred == QueryPredicate.PropertyValue || pred == QueryPredicate.DatatypeProperty)
                {
                    cqString.append(atom.getArguments().get(1)).
                            append("(").
                            append(prefix(atom.getArguments().get(0))).
                            append(",").
                            append(prefix(atom.getArguments().get(2))).
                            append(")");
                }
                else
                    _logger.fine("Encountered unsupported query atom for textual representation: " + atom);
                if (i < _cq.getAtoms().size() - 1)
                    cqString.append(" & ");
            }
        }
        else
            cqString.append(propositions.create(_cq).toString());
        return cqString.toString();
    }

    private String prefix(ATermAppl var)
    {
        String result = "";
        if (_cq.getResultVars().contains(var))
            result = "?";
        if (var.getArguments().isEmpty())
            result += var.toString();
        else
            result += var.getArgument(0).toString(); // case: funVar()
        return result;
    }

    public void accept(MTCQVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public MTCQFormula copy()
    {
        return new ConjunctiveQueryFormula(getTemporalKB(), isDistinct(), _cq.copy());
    }

    @Override
    public boolean equals(Object other)
    {
        if (other instanceof ConjunctiveQueryFormula oCq)
        {
            return getConjunctiveQuery().equals(oCq.getConjunctiveQuery());
        }
        else
            return false;
    }

    @Override
    public int hashCode()
    {
        return _cq.hashCode();
    }
}
