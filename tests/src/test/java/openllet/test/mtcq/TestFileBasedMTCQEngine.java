package openllet.test.mtcq;

import openllet.aterm.ATermAppl;
import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static openllet.core.utils.TermFactory.term;

public class TestFileBasedMTCQEngine extends AbstractMTCQTest
{
    @Test
    public void simpleTest1() throws FileNotFoundException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simpleTest1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simpleTest1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:A(?x) ^ :A(?y))""",
                new ATermAppl[][] {
                        {
                            term("http://mtcq/example2/data#r1"),
                            term("http://mtcq/example2/data#r0")
                        },
                        {
                            term("http://mtcq/example2/data#r0"),
                                term("http://mtcq/example2/data#r1")
                        }});
    }

    @Test
    public void simpleTest2() throws FileNotFoundException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simpleTest1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simpleTest1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:r(?y,?x))""",
                new ATermAppl[][] {
                        {
                                term("http://mtcq/example2/data#r1"),
                                term("http://mtcq/example2/data#r0")
                        }});
    }

    @Test
    public void simpleTest3() throws FileNotFoundException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simpleTest1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simpleTest1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:A(?y) ^ :A(?x) ^ :r(?y,?x))""",
                new ATermAppl[][] {
                        {
                                term("http://mtcq/example2/data#r1"),
                                term("http://mtcq/example2/data#r0")
                        }});
    }

    @Test
    public void simpleTest4() throws FileNotFoundException
    {
        // TODO
        // Known to fail: Openllet has a bug in the CQ engine. For this MTCQ, the ordering of the CQ is:
        // A(?x), r(?y,?x), A(?y)
        // which leads to wrong answers (none found).
        // The CQ engine assigns r0 to x and fetches all r(?y,r0), but doesn't find any.
        // Investigate why this happens.
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simpleTest1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simpleTest1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:A(?x) ^ :A(?y) ^ :r(?y,?x))""",
                new ATermAppl[][] {
                        {
                                term("http://mtcq/example2/data#r1"),
                                term("http://mtcq/example2/data#r0")
                        }});
    }
}
