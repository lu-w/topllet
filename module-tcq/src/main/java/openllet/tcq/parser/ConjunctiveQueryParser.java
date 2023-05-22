package openllet.tcq.parser;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.boxes.abox.Individual;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.cq.*;
import openllet.shared.tools.Log;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class ConjunctiveQueryParser
{
    public static final Logger _logger = Log.getLogger(ConjunctiveQueryParser.class);

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
                cq.addResultVar(ind);
        }
        return ind;
    }

    static public ConjunctiveQuery parse(String input, KnowledgeBase kb)
    {
        // Parsing of CNCQ a la "C(a) ^ r(a,b)"
        ConjunctiveQuery cq = new ConjunctiveQueryImpl(kb, false);
        for (String atom : input.split("\\^"))
        {
            if (atom.contains("(") && atom.contains(")"))
            {
                atom = atom.replace(")", "");
                String[] splitAtom = atom.split("\\(");
                if (splitAtom.length == 2)
                {
                    QueryAtom qAtom;
                    if (splitAtom[1].contains(","))
                    {
                        // Role
                        String role = splitAtom[0].trim();
                        String[] indsString = splitAtom[1].split(",");
                        ATermAppl ind1 = toIndividual(indsString[0], cq);
                        ATermAppl ind2 = toIndividual(indsString[1], cq);
                        qAtom = QueryAtomFactory.PropertyValueAtom(ind1, ATermUtils.makeTermAppl(role), ind2);
                    }
                    else
                    {
                        // Class
                        String cls = splitAtom[0].trim();
                        ATermAppl ind = toIndividual(splitAtom[1], cq);
                        qAtom = QueryAtomFactory.TypeAtom(ind, ATermUtils.makeTermAppl(cls));
                    }
                    cq.add(qAtom);
                }
            }
        }
        return cq;
    }
}
