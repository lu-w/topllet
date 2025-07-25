package openllet.test.mtcq;

import openllet.mtcq.engine.automaton.MLTL2DFA;
import openllet.mtcq.model.automaton.DFA;
import openllet.mtcq.parser.ParseException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class TestMLTL2DFA
{
    protected DFA convert(String mltlFormula)
    {
        return MLTL2DFA.convert(mltlFormula);
    }

    @Test
    public void testSimpleMLTL()
    {
        DFA dfa = convert("F_<=1 a");
        assertEquals(4, dfa.getStates().size());
        assertTrue(dfa.accepts(List.of("1")));
        assertTrue(dfa.accepts(List.of("0", "1")));
        assertFalse(dfa.accepts(List.of("0", "0", "1")));
        assertFalse(dfa.accepts(List.of("0", "0", "1", "X")));
    }

    @Test
    public void testComplexMLTL()
    {
        DFA dfa = convert("F_<=1 a & (b U_[5,10] c) -> (G b | F_[0,20] d)");
        assertEquals(56, dfa.getStates().size());
    }

    @Test
    public void testWrongMLTLFormula()
    {
        assertThrows(ParseException.class, () -> MLTL2DFA.convert("G[10,20] a"));
    }

    @Test
    public void testWrongLTLFormula()
    {
        assertThrows(ParseException.class, () -> MLTL2DFA.convert("GX -> a"));
    }
}
