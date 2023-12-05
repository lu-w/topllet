package openllet.mtcq.model.kb.loader;

import openllet.core.KnowledgeBase;
import openllet.core.utils.Timer;
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

/**
 * Standard loader to load knowledge bases from files. Allows to set Oasis XML catalogs.
 */
public class KnowledgeBaseLoader
{
    public static final Logger _logger = Log.getLogger(KnowledgeBaseLoader.class);

    protected static final String _defaultCatalogFileName = "catalog-v001.xml";
    protected Set<OWLOntologyIRIMapper> _catalogs = new HashSet<>();
    protected Timer _timer = new Timer("ontology loading");
    private Set<OWLOntology> _prevImports = null;

    public KnowledgeBaseLoader()
    {
    }

    public KnowledgeBaseLoader(Timer timer)
    {
        if (timer != null)
            _timer = timer;
    }

    /**
     * Functionality for fetching differences between loaded knowledge bases. Note: works only with some loaders.
     * @return A pointer to the difference of the currently loaded knowledge base to the previously loaded one.
     */
    @Nullable
    public OntologyDiff getDiffToLastKB()
    {
        _logger.finest("Difference between KBs only supported by incremental loader.");
        return null;
    }

    /**
     * Loads a knowledge base from a given file path.
     * @param fileName The path to the file to load the knowledge base from.
     * @return The loaded knowledge base, newly created.
     * @throws OWLOntologyCreationException If there are inconsistencies in the imports of the loaded knowledge base
     *      to the imports of the previously loaded knowledge base.
     * @throws FileNotFoundException If the given file does not exist.
     */
    @Nullable
    public KnowledgeBase load(String fileName) throws OWLOntologyCreationException, FileNotFoundException
    {
        _timer.start();
        OWLOntology ont = loadOntology(fileName);
        OpenlletReasoner reasoner = OpenlletReasonerFactory.getInstance().createReasoner(ont);
        _timer.stop();
        _logger.fine("Loaded " + fileName);
        if (_prevImports != null && !_prevImports.equals(ont.getImports()))
            throw new OWLOntologyCreationException("Temporal knowledge base loaded an ABox that does not has the " +
                    "same imports as the previously loaded ABox.");
        else
            _prevImports = ont.getImports();
        return reasoner.getKB();
    }

    /**
     * Adds an OASIS XML catalog to the loader. After adding, any loading procedure will consider this catalog. Does
     * not overwrite old catalogs, just adds new ones. Logs a warning if the catalog could not be loaded.
     * @param catalogFileName The path to the OASIS XML catalog.
     */
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

    /**
     * Implements the loading procedure for OWL ontologies.
     * @param fileName The path to the file to load the ontology from.
     * @return The ontology that was loaded.
     * @throws OWLOntologyCreationException If there was a problem in creating and loading the ontology.
     * @throws FileNotFoundException If the given file does not exist.
     */
    protected OWLOntology loadOntology(String fileName) throws OWLOntologyCreationException, FileNotFoundException
    {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntologyLoaderConfiguration config = man.getOntologyLoaderConfiguration();
        //config = config.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
        //man.setOntologyLoaderConfiguration(config);
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
