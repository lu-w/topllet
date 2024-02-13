package openllet.mtcq.parser;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.LogicalTrueFormula;
import openllet.mtcq.model.query.MTCQFormula;
import openllet.mtcq.model.query.StrongNextFormula;
import openllet.mtcq.model.query.PropositionFactory;
import org.antlr.v4.runtime.tree.*;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO tmrw:
 * - add all relevant data classes to query model
 * - implement toString method in each of these classes
 * - implement the recursive visit methods (easy, just see how the parse tree looks)
 * - implement the base cases:
 *   - for CQs: take the code from CQParser and add it here
 *   - also handle prop. abstraction while building CQ (should be easy, just use the _propositionFactory
 *   - store propAbstraction somewhere and add it to the query
 * - implement getPropAbstraction() method in MTCQFormula
 */

public class MTCQBuilder extends AbstractParseTreeVisitor<MTCQFormula> implements MTCQVisitor<MTCQFormula>
{
    private final TemporalKnowledgeBase _tkb;
    private final boolean _isDistinct;
    private final PropositionFactory _propositionFactory = new PropositionFactory();
    private final Map<String, String> _prefixes = new HashMap<>();

    public MTCQBuilder(TemporalKnowledgeBase tkb, boolean isDistinct)
    {
        _tkb = tkb;
        _isDistinct = isDistinct;
    }

    @Override
    public MTCQFormula visitTrace_position(MTCQParser.Trace_positionContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitProp_booleans(MTCQParser.Prop_booleansContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitLogic_booleans(MTCQParser.Logic_booleansContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitNot(MTCQParser.NotContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitAnd(MTCQParser.AndContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitOr(MTCQParser.OrContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitImpl(MTCQParser.ImplContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitEquiv(MTCQParser.EquivContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitXor(MTCQParser.XorContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitFull_interval(MTCQParser.Full_intervalContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitUpper_including_bound_interval(MTCQParser.Upper_including_bound_intervalContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitUpper_excluding_bound_interval(MTCQParser.Upper_excluding_bound_intervalContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitInterval(MTCQParser.IntervalContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitWeak_next(MTCQParser.Weak_nextContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitNext(MTCQParser.NextContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitUntil(MTCQParser.UntilContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitEventually(MTCQParser.EventuallyContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitAlways(MTCQParser.AlwaysContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitTerm(MTCQParser.TermContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitSubject(MTCQParser.SubjectContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitRole_atom(MTCQParser.Role_atomContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitConcept_atom(MTCQParser.Concept_atomContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitAtom(MTCQParser.AtomContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitConjunctive_query(MTCQParser.Conjunctive_queryContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitTracePositionFormula(MTCQParser.TracePositionFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitLogicBooleanFormula(MTCQParser.LogicBooleanFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitXorFormula(MTCQParser.XorFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitImplFormula(MTCQParser.ImplFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitPropositionalBooleanFormula(MTCQParser.PropositionalBooleanFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitBracketFormula(MTCQParser.BracketFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitAlwaysFormula(MTCQParser.AlwaysFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitWeakNextFormula(MTCQParser.WeakNextFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitEventuallyFormula(MTCQParser.EventuallyFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitEquivFormula(MTCQParser.EquivFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitConjunctiveQueryFormula(MTCQParser.ConjunctiveQueryFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitNextFormula(MTCQParser.NextFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitAndFormula(MTCQParser.AndFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitUntilFormula(MTCQParser.UntilFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitNotFormula(MTCQParser.NotFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitOrFormula(MTCQParser.OrFormulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitPrefix(MTCQParser.PrefixContext ctx)
    {
        _prefixes.put(ctx.getChild(1).getText(), ctx.getChild(4).getText());
        return null;
    }

    @Override
    public MTCQFormula visitStart(MTCQParser.StartContext ctx)
    {
        if (ctx.getChildCount() > 0)
        {
            for (int i = 0; i < ctx.getChildCount() - 1; i++)
                visit(ctx.getChild(i));
            return visit(ctx.getChild(ctx.getChildCount() - 1));
        }
        else
        {
            return new LogicalTrueFormula(_tkb, _isDistinct);  // TODO check (empty query always true?)
        }
    }
}
