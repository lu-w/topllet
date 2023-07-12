package openllet.test.tcq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBase;
import openllet.core.KnowledgeBaseImpl;
import openllet.tcq.model.kb.InMemoryTemporalKnowledgeBaseImpl;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;
import openllet.tcq.parser.TemporalConjunctiveQueryParser;
import openllet.test.query.AbstractQueryTest;
import org.junit.After;
import org.junit.Before;

import java.util.List;

import static openllet.core.utils.TermFactory.*;
import static org.junit.Assert.*;

public class AbstractTCQTest extends AbstractQueryTest
{
    TemporalKnowledgeBase _tkb = null;

    @Override
    @Before
    public void initializeKB()
    {
        _tkb = new InMemoryTemporalKnowledgeBaseImpl();
    }

    @Override
    @After
    public void disposeKB()
    {
        _tkb = null;
    }

    @Override
    protected void classes(final ATermAppl... classes)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.classes(classes);
        }
    }

    protected void subClass(final ATermAppl sub, final ATermAppl sup)
    {
        for (KnowledgeBase kb : _tkb)
            kb.addSubClass(sub, sup);
    }

    @Override
    protected void objectProperties(final ATermAppl... props)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.objectProperties(props);
        }
    }

    @Override
    protected void dataProperties(final ATermAppl... props)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.dataProperties(props);
        }
    }

    @Override
    protected void annotationProperties(final ATermAppl... props)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.annotationProperties(props);
        }
    }

    @Override
    protected void individuals(final ATermAppl... inds)
    {
        for (KnowledgeBase kb : _tkb)
        {
            _kb = kb;
            super.individuals(inds);
        }
    }

    protected void timeSteps(int size)
    {
        for (int i = 0; i < size; i++)
            appendABox();
    }

    protected void appendABox()
    {
        _tkb.add(new KnowledgeBaseImpl());
    }

    protected TemporalConjunctiveQuery temporalQuery(String formula)
    {
        TemporalConjunctiveQuery tcq = null;
        try
        {
            tcq = TemporalConjunctiveQueryParser.parse(formula, _tkb);
        }
        catch (ParseException e)
        {
            fail("Parsing of TCQ failed: " + e);
        }
        return tcq;
    }

    protected TemporalConjunctiveQuery uncheckedTemporalQuery(String formula) throws ParseException
    {
        return TemporalConjunctiveQueryParser.parse(formula, _tkb);
    }

    public void simpleTKB()
    {
        fillSimpleTKB(5);
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            kb.addType(_b, _B);
            kb.addPropertyValue(_r, _a, _b);
        }
    }

    public void simpleTKB2()
    {
        fillSimpleTKB(5);
        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            kb.addType(_a, _A);
            if (i > 2)
                kb.addType(_b, _B);
            kb.addPropertyValue(_r, _a, _b);
            i++;
        }
    }

    protected void fillSimpleTKB(int size)
    {
        timeSteps(size);

        classes(_A, _B, _C, _D);
        individuals(_a, _b, _c);
        objectProperties(_r, _p);
    }

    public void complexTKB()
    {
        fillComplexTKB(10);
        int i = 0;
        for (KnowledgeBase kb : _tkb)
        {
            if (i % 2 == 0)
            {
                kb.addType(_a, _A);
                kb.addPropertyValue(_r, _a, _b);
            }
            else
            {
                kb.addType(_b, _E);
            }
            kb.addType(_b, _D);
            kb.addType(_c, _B);
            if (i == 8)
                kb.addPropertyValue(_q, _b, _a);
            if (i == 9)
                kb.addPropertyValue(_q, _c, _b);
            i++;
        }
    }

    protected void fillComplexTKB(int size)
    {
        timeSteps(size);

        classes(_A, _B, _C, _D, _E);
        individuals(_a, _b, _c);
        objectProperties(_r, _p, _q);

        subClass(_A, or(_B, _C));
        subClass(_A, not(_D));
        subClass(_B, some(_p, TOP));
        subClass(_C, not(some(_r, TOP)));
    }

    protected void assertQueryEntailed(String query)
    {
        testQuery(query, true);
    }

    protected void assertQueryNotEntailed(String query)
    {
        testQuery(query, false);
    }

    protected void testQuery(String query, final boolean expected)
    {
        TemporalConjunctiveQuery tcq = temporalQuery(query);
        testQuery(tcq, expected);
    }

    protected void testQuery(String query, final ATermAppl[]... values)
    {
        TemporalConjunctiveQuery tcq = temporalQuery(query);
        testQuery(tcq, values);
    }

    protected void testQuery(String query, final List<List<ATermAppl>> values)
    {
        TemporalConjunctiveQuery tcq = temporalQuery(query);
        testQuery(tcq, values);
    }
}
