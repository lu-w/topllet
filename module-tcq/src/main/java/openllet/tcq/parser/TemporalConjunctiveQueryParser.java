package openllet.tcq.parser;

import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.shared.tools.Log;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.query.Proposition;
import openllet.tcq.model.query.PropositionFactory;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.model.query.TemporalConjunctiveQueryImpl;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemporalConjunctiveQueryParser
{
    public static final Logger _logger = Log.getLogger(TemporalConjunctiveQueryParser.class);

    protected static final String[] validMLTLToken = {"F", "G", "U", "X", "_", "<", "=", "[", "]", "(", ")", "W",
            "X[!]", "last", "end", "R", "V", "M", "->", "<->", "^", "&", "!", "|", "0", "1", "2", "3", "4", "5", "6",
            "7", "8", "9", ","};

    static public TemporalConjunctiveQuery parse(String input, TemporalKnowledgeBase kb) throws ParseException
    {
        // Removes comments
        Pattern commentLine = Pattern.compile("(?m)(^#.*$)");
        Matcher commentLineMatcher = commentLine.matcher(input);
        String tcq = commentLineMatcher.replaceAll("");
        Pattern inlineComment = Pattern.compile("(?m)^(.*)(# .*)$");
        Matcher inlineCommentMatcher = inlineComment.matcher(tcq);
        tcq = inlineCommentMatcher.replaceAll("$1");

        // Stores prefixes and removes them from string
        Map<String, String> prefixes = new HashMap<>();
        Scanner scanner = new Scanner(tcq);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("PREFIX"))
            {
                Pattern pattern = Pattern.compile("^PREFIX *([^ ]*) *<([^ ]*)>$");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find())
                    prefixes.put(matcher.group(1), matcher.group(2));
            }
        }
        scanner.close();
        tcq = tcq.replaceAll("PREFIX.*(\r\n|\r|\n)", "");

        // Resolves prefixes in remaining string
        for (String prefix : prefixes.keySet())
            tcq = tcq.replaceAll(prefix, prefixes.get(prefix));

        // Removes irrelevant line breaks and tabs / whitespaces
        tcq = tcq.replaceAll("(\r\n|\r|\n)[\t ]*", "").trim();

        TemporalConjunctiveQuery parsedTcq = new TemporalConjunctiveQueryImpl(tcq, kb, true);
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
                throw new ParseException("Can not find opening bracket for conjunctive query in " + tcq);

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
            ConjunctiveQuery q = ConjunctiveQueryParser.parse(cqString, kb.get(0));
            if (!parsedTcq.getConjunctiveQueries().contains(q))
            {
                Proposition qProp = propositionFactory.create(q);
                parsedTcq.addConjunctiveQuery(qProp, q, cqString);
            }
        }

        _logger.fine("Propositional abstraction is " + parsedTcq.getPropositionalAbstraction());

        return parsedTcq;
    }
}
