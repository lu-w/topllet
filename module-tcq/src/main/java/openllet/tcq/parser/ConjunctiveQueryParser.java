package openllet.tcq.parser;

import openllet.core.KnowledgeBase;
import openllet.core.utils.ATermUtils;
import openllet.query.sparqldl.model.cq.*;

public class ConjunctiveQueryParser
{
    static public ConjunctiveQuery parse(String input, KnowledgeBase kb)
    {
        // Parsing of CNCQ a la "C(a) ^ r(a,b)"
        // TODO existentially quantified variables
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
                        String role = splitAtom[0];
                        String[] inds = splitAtom[1].split(",");
                        String ind1 = inds[0];
                        String ind2 = inds[1];
                        qAtom = QueryAtomFactory.PropertyValueAtom(ATermUtils.makeTermAppl(ind1), ATermUtils.makeTermAppl(role),
                                ATermUtils.makeTermAppl(ind2));
                    }
                    else
                    {
                        // Class
                        String cls = splitAtom[0];
                        String ind = splitAtom[1];
                        qAtom = QueryAtomFactory.TypeAtom(ATermUtils.makeTermAppl(ind), ATermUtils.makeTermAppl(cls));
                    }
                    cq.add(qAtom);
                }
            }
        }
        return cq;
    }
}
