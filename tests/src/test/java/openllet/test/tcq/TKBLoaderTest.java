package openllet.test.tcq;

import openllet.tcq.model.kb.FileBasedTemporalKnowledgeBaseImpl;
import org.junit.Test;
import org.semanticweb.owlapi.model.UnloadableImportException;

import java.io.FileNotFoundException;
import java.util.List;

import static org.junit.Assert.*;

public class TKBLoaderTest extends AbstractTCQTest
{
    @Test
    public void testLoadingSimpleTB() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/tcq/abox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/tcq/catalog-v001.xml");
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
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/tcq/abox_newline.kbs");
        assertEquals(3, kbs.size());
    }

    @Test
    public void testCatalogFile() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/tcq/abox.kbs");
        assertEquals(2, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbsNoImport = new FileBasedTemporalKnowledgeBaseImpl(kbs);
        assertThrows(UnloadableImportException.class, () -> fkbsNoImport.get(0));
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/tcq/catalog-v001.xml");
        assertEquals(2, fkbs.get(0).getIndividualsCount());
    }

    @Test
    public void testAllIndividualsPresentInFirstKB() throws FileNotFoundException
    {
        List<String> kbs = FileBasedTemporalKnowledgeBaseImpl.parseKBSFile("test/data/tcq/abox_faulty_inds.kbs");
        assertEquals(4, kbs.size());
        FileBasedTemporalKnowledgeBaseImpl fkbs = new FileBasedTemporalKnowledgeBaseImpl(kbs,
                "test/data/tcq/catalog-v001.xml");
        assertEquals(fkbs.get(0).getIndividualsCount(), 2);
        assertEquals(fkbs.get(1).getIndividualsCount(), 2);
        assertThrows(RuntimeException.class, () -> fkbs.get(2));
    }
}
