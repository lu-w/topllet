package openllet.test.mtcq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.query.sparqldl.engine.QueryExec;
import openllet.query.sparqldl.model.cq.ConjunctiveQuery;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.mtcq.engine.BooleanMTCQEngine;
import openllet.mtcq.model.query.PropositionFactory;
import openllet.mtcq.model.query.MetricTemporalConjunctiveQuery;

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

    protected static void testMTCQ(MetricTemporalConjunctiveQuery mtcq, int numberOfCQs, String negatedPropositionalAbstraction)
    {
        assertEquals(numberOfCQs, mtcq.getQueries().size());
        assertEquals(numberOfCQs, mtcq.getPropositionalAbstraction().size());
        assertEquals(negatedPropositionalAbstraction, mtcq.toNegatedPropositionalAbstractionString());
        PropositionFactory propFac = new PropositionFactory();
        for (ConjunctiveQuery cq : mtcq.getQueries())
            assertEquals(cq, mtcq.getPropositionalAbstraction().get(propFac.create(cq)));
    }

    protected static void testVars(MetricTemporalConjunctiveQuery mtcq, Set<ATermAppl> undistVars, Set<ATermAppl> resultVars)
    {
        assertEquals(undistVars.size(), mtcq.getUndistVars().size());
        assertEquals(resultVars.size(), mtcq.getDistVars().size());
        assertEquals(resultVars.size(), mtcq.getResultVars().size());
        for (ATermAppl undistVar : undistVars)
            assertTrue(mtcq.getUndistVars().contains(undistVar));
        for (ATermAppl resultVar : resultVars)
        {
            assertTrue(mtcq.getResultVars().contains(resultVar));
            assertTrue(mtcq.getDistVars().contains(resultVar));
        }
    }

    protected static ATermAppl[] atoms(ATermAppl... atoms)
    {
        return atoms;
    }
}
