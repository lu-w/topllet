package openllet.tcq.model.query;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.parser.ConjunctiveQueryParser;

import java.util.*;
import java.util.logging.Logger;

public class TemporalConjunctiveQueryImpl implements TemporalConjunctiveQuery
{
    public static final Logger _logger = Log.getLogger(TemporalConjunctiveQueryImpl.class);

    private final boolean _distinct;
    private TemporalKnowledgeBase _kb;
    private final Map<Proposition, ConjunctiveQuery> _propAbs = new TreeMap<>();
    private String _propAbsTcq;
    private final String _tcq;

    public TemporalConjunctiveQueryImpl(String tcq, TemporalKnowledgeBase kb, boolean distinct)
    {
        _distinct = distinct;
        _kb = kb;
        _tcq = tcq;
        buildPropositionalAbstraction(tcq);
    }

    private void buildPropositionalAbstraction(String tcq)
    {
        _logger.info("Building propositional abstraction...");

        _propAbsTcq = tcq;

        final String[] validMLTLToken = {"F", "G", "U", "X", "_", "<", "=", "[", "]", "(", ")", "W", "X[!]", "last",
                "end", "R", "V", "M", "->", "<->", "^", "&", "!", "|", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                ","};
        final PropositionFactory propositionFactory = new PropositionFactory();

        while(tcq.replace(")", "").length() > 0)
        {
            String remainder = tcq;
            int curIndex = 0;
            boolean tokenFound = true;
            // iterate until we first observe a non-whitespace non-valid MLTL token
            while (tokenFound)
            {
                tokenFound = false;
                if (remainder.startsWith(" "))
                {
                    remainder = remainder.substring(1);
                    curIndex += 1;
                    tokenFound = true;
                }
                for (String token : validMLTLToken)
                {
                    if (remainder.startsWith(token))
                    {
                        remainder = remainder.substring(token.length());
                        curIndex += token.length();
                        tokenFound = true;
                        break;
                    }
                }
            }
            int cqBeg = curIndex;
            curIndex--;  // we consumed the opening bracket - undo

            // check if this was preceded by an opening bracket (ignoring whitespaces)
            boolean openingBracketFound = false;
            if (tcq.charAt(curIndex) == '(')
                openingBracketFound = true;
            else
                for (int i = curIndex - 1; i >= 0 && tcq.charAt(i) == ' '; i--)
                    if (tcq.charAt(i) == '(')
                    {
                        openingBracketFound = true;
                        break;
                    }

            if (!openingBracketFound)
            {
                throw new IllegalArgumentException("Can not find opening bracket for conjunctive query in " + tcq);
            }

            // iterate until we find the closing bracket
            curIndex++;  // consume opening bracket
            int numOpenBrackets = 1;
            while (numOpenBrackets > 0 && curIndex < tcq.length())
            {
                if (tcq.charAt(curIndex) == '(')
                    numOpenBrackets++;
                else if (tcq.charAt(curIndex) == ')')
                    numOpenBrackets--;
                curIndex++;
            }
            int cqEnd = curIndex - 1;  // we do not want the closing bracket included in the CQ itself
            String cqString = tcq.substring(cqBeg, cqEnd);
            tcq = tcq.substring(curIndex);
            ConjunctiveQuery q = ConjunctiveQueryParser.parse(cqString, getKB().first());
            Proposition qProp = propositionFactory.create(q);
            _propAbs.put(qProp, q);
            // TODO replace only in cqString region, else this happens: "F(C(a)) & (C(a) ^ D(b))" -> F(a) & (a ^ D(b))
            _propAbsTcq = _propAbsTcq.replace(cqString, qProp.toString());
        }
        _logger.info("Propositional abstraction is " + _propAbs);
    }

    @Override
    public boolean isDistinct()
    {
        return _distinct;
    }

    @Override
    public String getPropositionalAbstractionTCQ()
    {
        return _propAbsTcq;
    }

    @Override
    public String getNegatedPropositionalAbstractionTCQ()
    {
        return "!(" + getPropositionalAbstractionTCQ() + ")";
    }

    @Override
    public Set<Proposition> getPropositionsInAbstraction()
    {
        return _propAbs.keySet();
    }

    @Override
    public Collection<ConjunctiveQuery> getConjunctiveQueries() {
        return _propAbs.values();
    }

    @Override
    public Map<Proposition, ConjunctiveQuery> getPropositionalAbstraction()
    {
        return _propAbs;
    }

    @Override
    public TemporalKnowledgeBase getKB()
    {
        return _kb;
    }

    @Override
    public void setKB(TemporalKnowledgeBase kb)
    {
        _kb = kb;
    }

    @Override
    public String toString()
    {
        return _tcq;
    }
}
