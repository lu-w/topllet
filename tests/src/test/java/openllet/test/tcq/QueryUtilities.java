package openllet.test.tcq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.tcq.engine.BooleanTCQEngine;
import openllet.tcq.model.query.PropositionFactory;
import openllet.tcq.model.query.TemporalConjunctiveQuery;

import java.util.Set;

import static openllet.core.utils.TermFactory.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QueryUtilities
{
    protected static void testAtom(QueryAtom atom, ATermAppl... parts)
    {
        assertEquals(atom.getArguments().size(), parts.length);
        for (int i = 0; i < parts.length; i++)
            assertEquals(parts[i], atom.getArguments().get(i));
    }

    protected static void testCQ(ConjunctiveQuery cq, ATermAppl[]... atoms)
    {
        assertEquals(atoms.length, cq.getAtoms().size());
        for (int i = 0; i < atoms.length; i++)
            testAtom(cq.getAtoms().get(i), atoms[i]);
    }

    protected static void testTCQ(TemporalConjunctiveQuery tcq, int numberOfCQs, String negatedPropositionalAbstraction)
    {
        assertEquals(numberOfCQs, tcq.getQueries().size());
        assertEquals(numberOfCQs, tcq.getPropositionalAbstraction().size());
        assertEquals(negatedPropositionalAbstraction, tcq.toNegatedPropositionalAbstractionString());
        PropositionFactory propFac = new PropositionFactory();
        for (ConjunctiveQuery cq : tcq.getQueries())
            assertEquals(cq, tcq.getPropositionalAbstraction().get(propFac.create(cq)));
    }

    protected static void testVars(TemporalConjunctiveQuery tcq, Set<ATermAppl> undistVars, Set<ATermAppl> resultVars)
    {
        assertEquals(undistVars.size(), tcq.getUndistVars().size());
        assertEquals(resultVars.size(), tcq.getDistVars().size());
        assertEquals(resultVars.size(), tcq.getResultVars().size());
        for (ATermAppl undistVar : undistVars)
            assertTrue(tcq.getUndistVars().contains(undistVar));
        for (ATermAppl resultVar : resultVars)
        {
            assertTrue(tcq.getResultVars().contains(resultVar));
            assertTrue(tcq.getDistVars().contains(resultVar));
        }
    }

    protected static ATermAppl[] atoms(ATermAppl... atoms)
    {
        return atoms;
    }
}
