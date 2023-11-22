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
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/abox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/catalog-v001.xml");
        assertEquals(2, fkbs.size());
        assertEquals(2, fkbs.get(0).getIndividualsCount());
        assertEquals(2, fkbs.get(1).getIndividualsCount());
        assertThrows(IndexOutOfBoundsException.class, () -> fkbs.get(2));
    }

    @Test
    public void testLoadingComplexTB()
    {
        // TODO based on example of NFM paper
    }

    @Test
    public void testEmptyLinesInKBSFile() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/abox_newline.kbs");
        assertEquals(3, kbs.size());
    }

    @Test
    public void testCatalogFile() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/abox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/catalog-v001.xml");
        assertEquals(2, fkbs.get(0).getIndividualsCount());
    }

    @Test
    public void testAllIndividualsPresentInFirstKB() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/abox_faulty_inds.kbs");
        assertEquals(4, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/catalog-v001.xml");
        assertEquals(fkbs.get(0).getIndividualsCount(), 2);
        assertEquals(fkbs.get(1).getIndividualsCount(), 2);
        assertThrows(RuntimeException.class, () -> fkbs.get(2));
    }

    @Test
    public void testImportWrongTBox() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/abox_wrong_tbox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/catalog-v001.xml");
        fkbs.get(0);
        assertThrows(RuntimeException.class, () -> fkbs.get(1));
    }

    @Test
    public void testWrongExpressivity() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/mtcq/abox_wrong_express.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/mtcq/catalog-v001.xml");
        assertThrows(RuntimeException.class, () -> fkbs.get(0));
    }
}
