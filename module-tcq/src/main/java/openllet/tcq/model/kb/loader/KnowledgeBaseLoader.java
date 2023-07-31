package openllet.tcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
import openllet.core.utils.Timers;
import openllet.modularity.OntologyDiff;
import openllet.owlapi.OpenlletReasoner;
import openllet.owlapi.OpenlletReasonerFactory;
import openllet.shared.tools.Log;
import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(KnowledgeBaseLoader.class);

    protected static final String _defaultCatalogFileName = "catalog-v001.xml";
    protected Set<OWLOntologyIRIMapper> _catalogs = new HashSet<>();
    protected Timer _timer = new Timer("ontology loading");

    public KnowledgeBaseLoader()
    {
    }

    public KnowledgeBaseLoader(Timer timer)
    {
        if (timer != null)
            _timer = timer;
    }

    @Nullable
    public OntologyDiff getDiffToLastKB()
    {
        _logger.finest("Difference between KBs only supported by incremental loader.");
        return null;
    }

    @Nullable
    public KnowledgeBase load(String fileName) throws OWLOntologyCreationException, FileNotFoundException
    {
        _timer.start();
        OWLOntology ont = loadOntology(fileName);
        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
        _timer.stop();
        System.out.println("Loaded " + fileName);
        return reasoner.getKB();
    }

    public void addCatalog(String catalogFileName)
    {
        if (catalogFileName != null)
        {
            try
            {
                File catalog = new File(catalogFileName);
                XMLCatalogIRIMapper mapper = new XMLCatalogIRIMapper(catalog);
                _catalogs.add(mapper);
            }
            catch (IOException e)
            {
                _logger.warning("Can not load catalog " + catalogFileName);
            }
        }
    }

    protected OWLOntology loadOntology(String fileName) throws OWLOntologyCreationException, FileNotFoundException
    {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        man.setIRIMappers(_catalogs);
        String catalogFileString = new File(fileName).getParent() + "/" + _defaultCatalogFileName;
        File catalogFile = new File(catalogFileString);
        if (catalogFile.exists())
            addCatalog(catalogFileString);
        File file = new File(fileName);
        if (file.exists())
            return man.loadOntologyFromOntologyDocument(file);
        else
            throw new FileNotFoundException(file.toString());
    }
}
