package openllet.mtcq.parser;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.Query;
import openllet.query.sparqldl.model.cq.*;
import openllet.shared.tools.Log;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

/**
 * A parser for simple conjunctive queries.
 * Allows for classes and roles, as well as individuals, undistinguished and answer variables.
 */
public class ConjunctiveQueryParser
{
    public static final Logger _logger = Log.getLogger(ConjunctiveQueryParser.class);

    /**
     * Helper method to convert a string to either an individual, an answer variable, or a undistinguished variable.
     * Adds the appropriate result to the given CQ.
     * @param indString String representing the name of the given variable / individual.
     * @param cq The CQ to add the variable / individual to.
     * @return The ATermAppl representing the variable / individual.
     * @throws ParseException If conflicting information on variables / individuals is present in the CQ.
     */
    static private ATermAppl toIndividual(String indString, ConjunctiveQuery cq) throws ParseException
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

    /**
     * Parses the given CQ string to an Openllet ConjunctiveQuery a la "C(a) & r(a,?b)"
     * @param input The sting to parse.
     * @param kb The knowledge base containing roles and concepts used in the CQ.
     * @param isDistinct whether the query has only answers who are distinct (the same individual can not be mapped to
     *                   two different answer variables).
     * @return The parsed CQ.
     * @throws ParseException If conflicting information on variables / individuals is present in the CQ, or the string
     * does not adhere to a valid CQ syntax.
     */
    static public ConjunctiveQuery parse(String input, KnowledgeBase kb, boolean isDistinct) throws ParseException
    {
        ConjunctiveQuery cq = new ConjunctiveQueryImpl(kb, isDistinct);
        for (String atom : input.split("&"))
        {
            if (atom.contains("(") && atom.contains(")"))
            {
                String newAtom = atom.replace(")", "");
                String[] splitAtom = newAtom.split("\\(");
                if (splitAtom.length == 2)
                {
                    QueryAtom qAtom;
                    long numberOfCommas = splitAtom[1].chars().filter(ch -> ch == ',').count();
                    if (numberOfCommas == 1)
                    {
                        // Role
                        String role = splitAtom[0].trim();
                        String[] indsString = splitAtom[1].split(",");
                        indsString[0] = indsString[0].trim();
                        indsString[1] = indsString[1].trim();
                        ensureValidURI(role, indsString[0], indsString[1]);
                        ATermAppl roleATerm = ATermUtils.makeTermAppl(role);
                        ensureValidRole(roleATerm, cq);
                        ATermAppl ind1 = toIndividual(indsString[0], cq);
                        ATermAppl ind2 = toIndividual(indsString[1], cq);
                        qAtom = QueryAtomFactory.PropertyValueAtom(ind1, roleATerm, ind2);
                    }
                    else if (numberOfCommas == 0)
                    {
                        // Class
                        String cls = splitAtom[0].trim();
                        String indString = splitAtom[1].trim();
                        ensureValidURI(cls, indString);
                        ATermAppl clsATerm = ATermUtils.makeTermAppl(cls);
                        ensureValidClass(clsATerm, cq);
                        ATermAppl ind = toIndividual(indString, cq);
                        qAtom = QueryAtomFactory.TypeAtom(ind, clsATerm);
                    }
                    else
                        throw new ParseException("Variable list " + splitAtom[1] + " contains too many commas");
                    cq.add(qAtom);
                }
                else if (splitAtom.length > 2)
                    throw new ParseException("Atom " + atom + " contains too many opening brackets");
                else
                    throw new ParseException("Atom " + atom + " contains no opening bracket");
            }
            else
                throw new ParseException("Atom " + atom + " does not contain closing and opening brackets");
        }
        return cq;
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
