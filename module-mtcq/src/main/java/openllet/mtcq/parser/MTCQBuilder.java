package openllet.mtcq.parser;

import openllet.aterm.ATermAppl;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Pair;
import openllet.mtcq.model.kb.TemporalKnowledgeBase;
import openllet.mtcq.model.query.*;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.ConjunctiveQueryImpl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.query.sparqldl.model.cq.QueryAtomFactory;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class MTCQBuilder extends AbstractParseTreeVisitor<MTCQFormula> implements MTCQVisitor<MTCQFormula>
{
    private final TemporalKnowledgeBase _tkb;
    private final boolean _isDistinct;
    private final Map<String, String> _prefixes = new HashMap<>();
    private final char _PREFIX_CHARACTER = ':';

    public MTCQBuilder(TemporalKnowledgeBase tkb, boolean isDistinct)
    {
        _tkb = tkb;
        _isDistinct = isDistinct;
    }

    public ConjunctiveQuery toConjunctiveQuery(MTCQParser.Conjunctive_queryContext ctx)
    {
        final ConjunctiveQuery cq = new ConjunctiveQueryImpl(_tkb.get(0), _isDistinct);
        for (MTCQParser.AtomContext parsedAtom : ctx.atom())
        {
            QueryAtom qAtom;
            if (parsedAtom.concept_atom() != null)
            {
                // Concept atom
                String cls = replacePrefix(parsedAtom.concept_atom().term().getText());
                String indString = replacePrefix(parsedAtom.concept_atom().subject().getText());
                ensureValidURI(cls, indString);
                ATermAppl clsATerm = ATermUtils.makeTermAppl(cls);
                ensureValidClass(clsATerm, cq);
                ATermAppl ind = toIndividual(indString, cq);
                qAtom = QueryAtomFactory.TypeAtom(ind, clsATerm);
            }
            else
            {
                // Role atom
                String role = replacePrefix(parsedAtom.role_atom().term().getText());
                String indString0 = replacePrefix(parsedAtom.role_atom().subject(0).getText());
                String indString1 = replacePrefix(parsedAtom.role_atom().subject(1).getText());
                ensureValidURI(role, indString0, indString1);
                ATermAppl roleATerm = ATermUtils.makeTermAppl(role);
                ensureValidRole(roleATerm, cq);
                ATermAppl ind1 = toIndividual(indString0, cq);
                ATermAppl ind2 = toIndividual(indString1, cq);
                qAtom = QueryAtomFactory.PropertyValueAtom(ind1, roleATerm, ind2);
            }
            cq.add(qAtom);
        }
        return cq;
    }

    @Override
    public MTCQFormula visitTrace_position(MTCQParser.Trace_positionContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitProp_booleans(MTCQParser.Prop_booleansContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitLogic_booleans(MTCQParser.Logic_booleansContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitNot(MTCQParser.NotContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitAnd(MTCQParser.AndContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitOr(MTCQParser.OrContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitImpl(MTCQParser.ImplContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitEquiv(MTCQParser.EquivContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitXor(MTCQParser.XorContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitFull_interval(MTCQParser.Full_intervalContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitUpper_including_bound_interval(MTCQParser.Upper_including_bound_intervalContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitUpper_excluding_bound_interval(MTCQParser.Upper_excluding_bound_intervalContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitInterval(MTCQParser.IntervalContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitWeak_next(MTCQParser.Weak_nextContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitNext(MTCQParser.NextContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitUntil(MTCQParser.UntilContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitEventually(MTCQParser.EventuallyContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitGlobally(MTCQParser.GloballyContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitTerm(MTCQParser.TermContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitSubject(MTCQParser.SubjectContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitRole_atom(MTCQParser.Role_atomContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitConcept_atom(MTCQParser.Concept_atomContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitAtom(MTCQParser.AtomContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitConjunctive_query(MTCQParser.Conjunctive_queryContext ctx)
    {
        // Intentionally left empty.
        return null;
    }

    @Override
    public MTCQFormula visitTracePositionFormula(MTCQParser.TracePositionFormulaContext ctx)
    {
        if (ctx.trace_position().END_TERMINAL() != null)
            return new EndFormula(_tkb, _isDistinct);
        else
            return new LastFormula(_tkb, _isDistinct);
    }

    @Override
    public MTCQFormula visitLogicBooleanFormula(MTCQParser.LogicBooleanFormulaContext ctx)
    {
        if (ctx.logic_booleans().FF_TERMINAL() != null)
            return new LogicalFalseFormula(_tkb, _isDistinct);
        else
            return new LogicalTrueFormula(_tkb, _isDistinct);
    }

    @Override
    public MTCQFormula visitXorFormula(MTCQParser.XorFormulaContext ctx)
    {
        return new XorFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)), visit(ctx.mtcq_formula(1)));
    }

    @Override
    public MTCQFormula visitImplFormula(MTCQParser.ImplFormulaContext ctx)
    {
        return new ImplFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)), visit(ctx.mtcq_formula(1)));
    }

    @Override
    public MTCQFormula visitPropositionalBooleanFormula(MTCQParser.PropositionalBooleanFormulaContext ctx)
    {
        if (ctx.prop_booleans().FALSE_TERMINAL() != null)
            return new PropositionalFalseFormula(_tkb, _isDistinct);
        else
            return new PropositionalTrueFormula(_tkb, _isDistinct);
    }

    @Override
    public MTCQFormula visitBracketFormula(MTCQParser.BracketFormulaContext ctx)
    {
        return visit(ctx.mtcq_formula());
    }

    @Override
    public MTCQFormula visitGloballyFormula(MTCQParser.GloballyFormulaContext ctx)
    {
        if (isBounded(ctx, true))
        {
            Pair<Integer, Integer> bounds = getBounds(ctx, true);
            return new BoundedGloballyFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()), bounds.first,
                    bounds.second);
        }
        else
            return new GloballyFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()));
    }

    @Override
    public MTCQFormula visitWeakNextFormula(MTCQParser.WeakNextFormulaContext ctx)
    {
        return new WeakNextFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()));
    }

    @Override
    public MTCQFormula visitEventuallyFormula(MTCQParser.EventuallyFormulaContext ctx)
    {
        if (isBounded(ctx, true))
        {
            Pair<Integer, Integer> bounds = getBounds(ctx, true);
            return new BoundedEventuallyFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()), bounds.first,
                    bounds.second);
        }
        else
            return new EventuallyFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()));
    }

    @Override
    public MTCQFormula visitEquivFormula(MTCQParser.EquivFormulaContext ctx)
    {
        return new EquivFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)), visit(ctx.mtcq_formula(1)));
    }

    @Override
    public MTCQFormula visitConjunctiveQueryFormula(MTCQParser.ConjunctiveQueryFormulaContext ctx)
    {
        return new ConjunctiveQueryFormula(_tkb, _isDistinct, toConjunctiveQuery(ctx.conjunctive_query()));
    }

    @Override
    public MTCQFormula visitNextFormula(MTCQParser.NextFormulaContext ctx)
    {
        return new StrongNextFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()));
    }

    @Override
    public MTCQFormula visitAndFormula(MTCQParser.AndFormulaContext ctx)
    {
        return new AndFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)), visit(ctx.mtcq_formula(1)));
    }

    @Override
    public MTCQFormula visitUntilFormula(MTCQParser.UntilFormulaContext ctx)
    {
        if (isBounded(ctx, false))
        {
            Pair<Integer, Integer> bounds = getBounds(ctx, false);
            return new BoundedUntilFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)),
                    visit(ctx.mtcq_formula(1)), bounds.first, bounds.second);
        }
        else
            return new UntilFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)), visit(ctx.mtcq_formula(1)));
    }

    @Override
    public MTCQFormula visitNotFormula(MTCQParser.NotFormulaContext ctx)
    {
        return new NotFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula()));
    }

    @Override
    public MTCQFormula visitOrFormula(MTCQParser.OrFormulaContext ctx)
    {
        return new OrFormula(_tkb, _isDistinct, visit(ctx.mtcq_formula(0)), visit(ctx.mtcq_formula(1)));
    }

    @Override
    public MTCQFormula visitPrefix(MTCQParser.PrefixContext ctx)
    {
        if (ctx.NAME() != null)
            _prefixes.put(ctx.NAME().getText(), ctx.URI().getText());
        else
            _prefixes.put("", ctx.URI().getText());
        return null;
    }

    @Override
    public MTCQFormula visitStart(MTCQParser.StartContext ctx)
    {
        if (ctx.getChildCount() > 0)
        {
            for (MTCQParser.PrefixContext prefix : ctx.prefix())
                visit(prefix);
            return visit(ctx.mtcq_formula());
        }
        else
        {
            return new EmptyFormula(_tkb, _isDistinct);
        }
    }

    private boolean isBounded(ParserRuleContext ctx, boolean isUnary)
    {
        int operatorIndex = isUnary ? 0 : 1;
        return ctx.getChildCount() > 0 && ctx.getChild(operatorIndex).getChildCount() > 1 &&
                ctx.getChild(operatorIndex).getChild(1) instanceof MTCQParser.IntervalContext;
    }

    // Assumes isBounded() has been verified before.
    private Pair<Integer, Integer> getBounds(ParserRuleContext ctx, boolean isUnary)
    {
        Pair<Integer, Integer> bounds = new Pair<>(0,0);
        int operatorIndex = isUnary ? 0 : 1;
        MTCQParser.IntervalContext interval = (MTCQParser.IntervalContext) ctx.getChild(operatorIndex).getChild(1);
        if (interval.getChild(0) instanceof MTCQParser.Full_intervalContext vals)
        {
            bounds = new Pair<>(Integer.parseInt(vals.TIME_POINT(0).getText()),
                    Integer.parseInt(vals.TIME_POINT(1).getText())
            );
        }
        else if (interval.getChild(0) instanceof MTCQParser.Upper_including_bound_intervalContext vals)
        {
            bounds = new Pair<>(0, Integer.parseInt(vals.TIME_POINT().getText()));
        }
        else if (interval.getChild(0) instanceof MTCQParser.Upper_excluding_bound_intervalContext vals)
        {
            bounds = new Pair<>(0, Integer.parseInt(vals.TIME_POINT().getText()) - 1);
        }
        return bounds;
    }

    private String replacePrefix(String orig)
    {
        String replaced = orig;
        for (String prefix : _prefixes.keySet())
        {
            if (replaced.startsWith(prefix + _PREFIX_CHARACTER))
            {
                replaced = replaced.replace(prefix + _PREFIX_CHARACTER, _prefixes.get(prefix));
                break;
            }
        }
        return replaced;
    }

    /**
     * Helper method to convert a string to either an individual, an answer variable, or an undistinguished variable.
     * Adds the appropriate result to the given CQ.
     * @param indString String representing the name of the given variable / individual.
     * @param cq The CQ to add the variable / individual to.
     * @return The ATermAppl representing the variable / individual.
     * @throws ParseException If conflicting information on variables / individuals is present in the CQ.
     */
    static private ATermAppl toIndividual(String indString, ConjunctiveQuery cq)
    {
        indString = indString.trim();
        boolean isResultVar = indString.startsWith("?");
        if (isResultVar)
            indString = indString.substring(1);
        ATermAppl ind = ATermUtils.makeTermAppl(indString);
        if (!cq.getKB().isIndividual(ATermUtils.makeTermAppl(indString)))
        {
            ind = ATermUtils.makeVar(indString);
            if (isResultVar)
            {
                cq.addResultVar(ind);
                cq.addDistVar(ind, Query.VarType.INDIVIDUAL);
            }
            else if (cq.getResultVars().contains(ind))
                throw new ParseException("Undistinguished variable " + indString + " is also present as a result " +
                        "variable in " + cq);
        }
        else if (isResultVar)
            throw new ParseException("Individual " + indString + " can not be used as a result variable in " + cq);
        return ind;
    }

    static private void ensureValidClass(ATermAppl cls, ConjunctiveQuery cq) throws ParseException
    {
        if (!cq.getKB().getClasses().contains(cls))
            throw new ParseException("Class " + cls + " not in knowledge base");
    }

    static private void ensureValidRole(ATermAppl role, ConjunctiveQuery cq) throws ParseException
    {
        if (!cq.getKB().getProperties().contains(role))
            throw new ParseException("Role " + role + " not in knowledge base");
    }

    static private void ensureValidURI(String... uris) throws ParseException
    {
        for (String uri : uris)
            try
            {
                new URI(uri);
            }
            catch (URISyntaxException e)
            {
                if (!uri.isEmpty() && !uri.matches("[a-zA-Z0-9_-]"))
                    throw new ParseException("Invalid URI: " + uri);
            }
    }
}
