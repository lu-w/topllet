package openllet.test.mtcq;

import openllet.aterm.ATermAppl;
import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static openllet.core.utils.TermFactory.term;

public class TestFileBasedMTCQEngine extends AbstractMTCQTest
{
    @Test
    public void simpleTest1() throws FileNotFoundException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simple_1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simple_1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:A(?x) & :A(?y))""",
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
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simple_1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simple_1/catalog-v001.xml");
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
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simple_1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simple_1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:A(?y) & :A(?x) & :r(?y,?x))""",
                new ATermAppl[][] {
                        {
                                term("http://mtcq/example2/data#r1"),
                                term("http://mtcq/example2/data#r0")
                        }});
    }

    //@Test
    public void simpleTest4() throws FileNotFoundException
    {
        // TODO
        // Known to fail: Openllet has a bug in the CQ engine. For this MTCQ, the ordering of the CQ is:
        // A(?x), r(?y,?x), A(?y)
        // which leads to wrong answers (none found).
        // The CQ engine assigns r0 to x and fetches all r(?y,r0), but doesn't find any.
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/simple_1/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/simple_1/catalog-v001.xml");
        testQuery("""
                        PREFIX : <http://mtcq/example2#>

                        (:A(?x) & :A(?y) & :r(?y,?x))""",
                new ATermAppl[][] {
                        {
                                term("http://mtcq/example2/data#r1"),
                                term("http://mtcq/example2/data#r0")
                        }});
    }

    @Test
    public void testRightOfWayGood() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/right_of_way/good/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/right_of_way/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/right_of_way/row.tcq"));
        testQuery(query,
                new ATermAppl[][] {
                        {
                                term("http://mtcq/eval/data#s"),
                                term("http://mtcq/eval/data#t"),
                                term("http://mtcq/eval/data#r1"),
                                term("http://mtcq/eval/data#r0")
                        }});
    }

    @Test
    public void testRightOfWayBad() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/right_of_way/bad/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/right_of_way/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/right_of_way/row.tcq"));
        testQuery(query);
    }

    @Test
    public void testAUTOSimpleQuery1() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/auto/simple_abox/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/auto/tbox/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/auto/queries/simple.tcq"));
        testQuery(query, new ATermAppl[][] { { term("http://mtcq/auto/data#r0") } });
    }

    @Test
    public void testAUTOSimpleQuery2() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/auto/simple_abox/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/auto/tbox/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/auto/queries/simple2.tcq"));
        testQuery(query, new ATermAppl[][] { { term("http://mtcq/auto/data#r0") } });
    }

    @Test
    public void testAUTOSimpleQuery3() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/auto/simple_abox/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/auto/tbox/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/auto/queries/simple3.tcq"));
        testQuery(query);
    }

    @Test
    public void testAUTOQuery1() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/auto/simple_abox/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/auto/tbox/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/auto/queries/query1.tcq"));
        testQuery(query, new ATermAppl[][] { {
                term("http://mtcq/auto/data#r0"),
                term("http://mtcq/auto/data#r1")
        } });
    }

    @Test
    public void testAUTOQuery2() throws IOException
    {
        List<String> kb = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/auto/simple_abox/aboxes.kbs");
        _tkb = new FileBasedTemporalKnowledgeBaseImpl(kb, "test/data/mtcq/auto/tbox/catalog-v001.xml");
        String query = Files.readString(Paths.get("test/data/mtcq/auto/queries/query2.tcq"));
        testQuery(query, new ATermAppl[][] { {
                term("http://mtcq/auto/data#r0"),
                term("http://mtcq/auto/data#r1")
        } });
    }
}
