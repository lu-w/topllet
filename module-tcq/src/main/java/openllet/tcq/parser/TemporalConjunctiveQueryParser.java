package openllet.tcq.parser;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.query.Proposition;
import openllet.tcq.model.query.PropositionFactory;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.model.query.TemporalConjunctiveQueryImpl;

import java.util.logging.Logger;

public class TemporalConjunctiveQueryParser
{
    public static final Logger _logger = Log.getLogger(TemporalConjunctiveQueryParser.class);

    protected static final String[] validMLTLToken = {"F", "G", "U", "X", "_", "<", "=", "[", "]", "(", ")", "W",
            "X[!]", "last", "end", "R", "V", "M", "->", "<->", "^", "&", "!", "|", "0", "1", "2", "3", "4", "5", "6",
            "7", "8", "9", ","};

    static public TemporalConjunctiveQuery parse(String input, TemporalKnowledgeBase kb)
    {
        _logger.info("Building propositional abstraction...");

        String tcq = input.replaceAll("(\r\n|\r|\n)[\t ]*", "");
        TemporalConjunctiveQuery parsedTcq = new TemporalConjunctiveQueryImpl(tcq, kb, false);
        final PropositionFactory propositionFactory = new PropositionFactory();

        while (tcq.replace(")", "").length() > 0)
        {
            String remainder = tcq;
            int curIndex = 0;
            boolean tokenFound = true;
            // iterate until we first observe a non-whitespace non-valid MLTL token
            while (tokenFound)
            {
                tokenFound = false;
                if (remainder.length() > 0 && Character.isWhitespace(remainder.charAt(0)))
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
                for (int i = curIndex - 1; i >= 0 && Character.isWhitespace(tcq.charAt(i)); i--)
                    if (tcq.charAt(i) == '(')
                    {
                        openingBracketFound = true;
                        break;
                    }

            if (!openingBracketFound)
                throw new IllegalArgumentException("Can not find opening bracket for conjunctive query in " + tcq);

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
            ConjunctiveQuery q = ConjunctiveQueryParser.parse(cqString, kb.first());
            Proposition qProp = propositionFactory.create(q);
            parsedTcq.addConjunctiveQuery(qProp, q, cqString);
        }
        _logger.info("Propositional abstraction is " + parsedTcq.getPropositionalAbstraction());

        return parsedTcq;
    }
}
