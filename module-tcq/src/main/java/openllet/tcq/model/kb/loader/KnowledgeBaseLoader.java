package openllet.tcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.shared.tools.Log;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(ReloadKnowledgeBaseLoader.class);

    @Nullable
    public abstract KnowledgeBase load(String fileName);

    protected static OWLOntology loadOWLOntology(String fileName)
    {
        OWLOntology ont = null;
        final String catalogFileName = "/catalog-v001.xml";
        File catalog = new File(new File(fileName).getParent() + catalogFileName);
        if (!catalog.exists())
            catalog = new File(System.getProperty("user.dir") + catalogFileName);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        man.getClass().getPackage().getImplementationVersion();
        try
        {
            man.addIRIMapper(new XMLCatalogIRIMapper(catalog));
        }
        catch (IOException e)
        {
            _logger.info("Can not load catalog " + catalogFileName);
        }
        try
        {
             ont = man.loadOntology(IRI.create("file://" + fileName));
        }
        catch (OWLOntologyCreationException e)
        {
            _logger.warning("Can not load knowledge base from " + fileName);
        }
        return ont;
    }
}
