package openllet.test.tcq;

import openllet.aterm.ATermAppl;
import openllet.query.sparqldl.model.cq.QueryAtom;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static openllet.core.utils.TermFactory.term;
import static openllet.test.tcq.QueryUtilities.*;
import static org.junit.Assert.*;

public class CQParserTest extends AbstractTCQTest
{
    @Before
    public void simpleTKB()
    {
        fillTKB(3);
    }

    protected void fillTKB(int size)
    {
        timeSteps(size);
        classes(_A, _B, _C, _D);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);
    }

    @Test
    public void testSimpleQuery()
    {
        TemporalConjunctiveQuery q = temporalQuery("(C(a) ^ D(b))");
        assertEquals("!((a))", q.toNegatedPropositionalAbstractionString());
        assertEquals(1, q.getQueries().size());
        assertEquals(2, q.getQueries().get(0).getAtoms().size());
        List<QueryAtom> atoms = q.getQueries().get(0).getAtoms();
        testAtom(atoms.get(0), _a, _C);
        testAtom(atoms.get(1), _b, _D);
    }

    @Test
    public void testConjunctiveQuery1()
    {
        TemporalConjunctiveQuery q = temporalQuery("(C(a) ^ r(?x, y) ^ A(?x) ^ C(b) ^ p(y,a))");
        assertEquals("!((a))", q.toNegatedPropositionalAbstractionString());
        assertEquals(1, q.getQueries().size());
        assertEquals(5, q.getQueries().get(0).getAtoms().size());
        List<QueryAtom> atoms = q.getQueries().get(0).getAtoms();
        testAtom(atoms.get(0), _a, _C);
        testAtom(atoms.get(1), x, _r, y);
        testAtom(atoms.get(2), x, _A);
        testAtom(atoms.get(3), _b, _C);
        testAtom(atoms.get(4), y, _p, _a);
        assertFalse(q.getDistVars().contains(y));
        assertFalse(q.getResultVars().contains(y));
        assertTrue(q.getUndistVars().contains(y));
        assertFalse(q.getUndistVars().contains(x));
        assertTrue(q.getDistVars().contains(x));
        assertTrue(q.getResultVars().contains(x));
    }

    @Test
    public void testConjunctiveQuery2()
    {
        String indStr = "http://_longIndividualNameWith.de/dom/#Weird$Characters-";
        ATermAppl ind = term(indStr);
        individuals(ind);
        TemporalConjunctiveQuery q = temporalQuery("(r(a, b) ^ A(" + indStr + ") ^ r(?x, y))");
        assertEquals(1, q.getQueries().size());
        testCQ(q.getQueries().get(0), atoms(_a, _r, _b), atoms(ind, _A), atoms(x, _r, y));
    }

    @Test
    public void testInvalidURI()
    {
        String indStr = "_longIndividualNameWith#Bad$Char acters";
        ATermAppl ind = term(indStr);
        individuals(ind);
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a, b) ^ A(" + indStr + ") ^ r(?x, y))"));
    }

    @Test
    public void testWrongTokenInCQ()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(C(a) & C(b))"));
    }

    @Test
    public void testIndividualAsResultVariable()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,b) ^ C(?b))"));
    }

    @Test
    public void testResultAndUndistVariableEquallyNamed()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,?x) ^ C(x))"));
    }

    @Test
    public void testRoleNotInKB()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(m(a,b) ^ C(b))"));
    }

    @Test
    public void testClassNotInKB()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,b) ^ Z(b))"));
    }

    @Test
    public void testTooManyCommasInCQ()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,b,c) ^ C(b))"));
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(C(a) ^ r(b,,))"));
    }

    @Test
    public void testTooManyBracketsInCQ()
    {
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r((a,b) ^ C(b))"));
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,b ^ C(b))"));
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,b) ^ C(b)"));
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(r(a,b ^ Cb)))"));
        assertThrows(ParseException.class, () -> uncheckedTemporalQuery("(ra,b ^ Cb)"));
    }
}
