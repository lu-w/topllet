package openllet.mtcq.parser;

import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.MTCQFormula;
import openllet.mtcq.model.query.NextFormula;
import org.antlr.v4.runtime.tree.*;

public class MTCQBuilder extends AbstractParseTreeVisitor<MTCQFormula> implements MTCQVisitor<MTCQFormula>
{
    private final TemporalKnowledgeBase _tkb;
    private final boolean _isDistinct;

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
        return visit(ctx.getChild(0));
    }

    @Override
    public MTCQFormula visitWeak_next(MTCQParser.Weak_nextContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitNext(MTCQParser.NextContext ctx)
    {
        return new NextFormula(_tkb, _isDistinct, visit(ctx.children.get(0)));
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
    public MTCQFormula visitMtcq_formula(MTCQParser.Mtcq_formulaContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitPrefix(MTCQParser.PrefixContext ctx)
    {
        return null;
    }

    @Override
    public MTCQFormula visitStart(MTCQParser.StartContext ctx)
    {
        return visit(ctx.children.get(0));
    }
}
