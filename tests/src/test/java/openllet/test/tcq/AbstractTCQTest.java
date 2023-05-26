package openllet.test.tcq;

import openllet.aterm.ATermAppl;
import openllet.core.KnowledgeBaseImpl;
import openllet.tcq.model.kb.InMemoryTemporalKnowledgeBaseImpl;
import openllet.tcq.model.kb.TemporalKnowledgeBase;
import openllet.tcq.model.query.TemporalConjunctiveQuery;
import openllet.tcq.parser.ParseException;
import openllet.tcq.parser.TemporalConjunctiveQueryParser;
import openllet.test.query.AbstractQueryTest;
import org.junit.After;
import org.junit.Before;

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
        _kb = _tkb.first();
        if (_kb != null)
            do
            {
                super.classes(classes);
                _kb = _tkb.next();
            }
            while (_kb != null);
        _tkb.reset();
    }

    @Override
    protected void objectProperties(final ATermAppl... props)
    {
        _kb = _tkb.first();
        if (_kb != null)
            do
            {
                super.objectProperties(props);
                _kb = _tkb.next();
            }
            while (_kb != null);
        _tkb.reset();
    }

    @Override
    protected void dataProperties(final ATermAppl... props)
    {
        _kb = _tkb.first();
        if (_kb != null)
            do
            {
                super.dataProperties(props);
                _kb = _tkb.next();
            }
            while (_kb != null);
        _tkb.reset();
    }

    @Override
    protected void annotationProperties(final ATermAppl... props)
    {
        _kb = _tkb.first();
        if (_kb != null)
            do
            {
                super.annotationProperties(props);
                _kb = _tkb.next();
            }
            while (_kb != null);
        _tkb.reset();
    }

    @Override
    protected void individuals(final ATermAppl... inds)
    {
        _kb = _tkb.first();
        if (_kb != null)
            do
            {
                super.individuals(inds);
                _kb = _tkb.next();
            }
            while (_kb != null);
        _tkb.reset();
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
}
