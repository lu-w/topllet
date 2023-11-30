package openllet.test.mtcq;

import openllet.mtcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.*;

public class TKBLoaderTest extends AbstractMTCQTest
{
    @Test
    public void testLoadingSimpleTB() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/loading/abox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/loading/catalog-v001.xml");
        assertEquals(2, fkbs.size());
        assertEquals(2, fkbs.get(0).getIndividualsCount());
        assertEquals(2, fkbs.get(1).getIndividualsCount());
        assertThrows(IndexOutOfBoundsException.class, () -> fkbs.get(2));
    }

    @Test
    public void testLoadingComplexTB1() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/right_of_way/good/aboxes.kbs");
        assertEquals(3, kbs.size());
    }

    @Test
    public void testLoadingComplexTB2() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/auto/simple_abox/aboxes.kbs");
        assertEquals(4, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/auto/tbox/catalog-v001.xml");
        assertEquals(7, fkbs.get(0).getIndividualsCount());
    }

    @Test
    public void testEmptyLinesInKBSFile() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/loading/abox_newline.kbs");
        assertEquals(3, kbs.size());
    }

    @Test
    public void testCatalogFile() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/loading/abox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/loading/catalog-v001.xml");
        assertEquals(2, fkbs.get(0).getIndividualsCount());
    }

    @Test
    public void testAllIndividualsPresentInFirstKB() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/loading/abox_faulty_inds.kbs");
        assertEquals(4, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/loading/catalog-v001.xml");
        assertEquals(fkbs.get(0).getIndividualsCount(), 2);
        assertEquals(fkbs.get(1).getIndividualsCount(), 2);
        assertThrows(RuntimeException.class, () -> fkbs.get(2));
    }

    @Test
    public void testABoxNotExisting() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/loading/abox_not_exist.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/loading/catalog-v001.xml");
        assertEquals(2, fkbs.size());
        assertEquals(2, fkbs.get(0).getIndividualsCount());
        assertThrows(RuntimeException.class, () -> fkbs.get(1));
    }

    @Test
    public void testImportWrongTBox() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/loading/abox_wrong_tbox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/loading/catalog-v001.xml");
        fkbs.get(0);
        assertThrows(RuntimeException.class, () -> fkbs.get(1));
    }

    @Test
    public void testWrongExpressivity() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile(
                "test/data/mtcq/loading/abox_wrong_express.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/loading/catalog-v001.xml");
        assertThrows(RuntimeException.class, () -> fkbs.get(0));
    }
}
