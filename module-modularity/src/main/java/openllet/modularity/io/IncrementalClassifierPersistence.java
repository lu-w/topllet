// Copyright (c) 2006 - 2008, Clark & Parsia, LLC. <http://www.clarkparsia.com>
// This source code is available under the terms of the Affero General Public License v3.
//
// Please see LICENSE.txt for full license terms, including the availability of proprietary exceptions.
// Questions, comments, or requests for clarification: licensing@clarkparsia.com

package openllet.modularity.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import openllet.core.taxonomy.TaxonomyImpl;
import openllet.modularity.IncrementalClassifier;
import openllet.modularity.ModuleExtractor;
import openllet.modularity.ModuleExtractorFactory;

/**
 * Provides persistence for IncrementalClassifier objects.
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Blazej Bulka
 */
public class IncrementalClassifierPersistence
{
	/**
	 * The name for the zip entry that stores the taxonomy.
	 */
	private static final String TAXONOMY_FILE_NAME = "Taxonomy";
	private static final String PROPERTIES_FILE_NAME = "Properties";
	private static final String PROPERTIES_FILE_COMMENT = "Properties of the IncrementalClassifier";
	private static final String REALIZED_PROPERTY = "realized";

	/**
	 * Saves the internal state of an incremental classifier to an output stream.
	 *
	 * @param classifier the classifier to be saved
	 * @param outputStream the output stream where the classifier should be saved
	 * @throws IOException if an error should occur during the save operation
	 */
	@SuppressWarnings("resource") // for the UncloseableOutputStream
	public static void save(final IncrementalClassifier classifier, final OutputStream outputStream) throws IOException
	{
		// extract the components that should be saved from the classifier
		final IncrementalClassifier.PersistedState persistedState = new IncrementalClassifier.PersistedState(classifier);

		final ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

		// save the module extractor
		persistedState.getModuleExtractor().save(zipOutputStream);

		// save the taxonomy
		final ZipEntry taxonomyEntry = new ZipEntry(TAXONOMY_FILE_NAME);
		zipOutputStream.putNextEntry(taxonomyEntry);

		TaxonomyPersistence.save(persistedState.getTaxonomy(), new UncloseableOutputStream(zipOutputStream));

		final ZipEntry propertiesEntry = new ZipEntry(PROPERTIES_FILE_NAME);
		zipOutputStream.putNextEntry(propertiesEntry);

		final Properties properties = new Properties();
		properties.setProperty(REALIZED_PROPERTY, String.valueOf(persistedState.isRealized()));
		properties.store(zipOutputStream, PROPERTIES_FILE_COMMENT);

		zipOutputStream.finish();
	}

	/**
	 * Loads the previously saved internal state of an incremental classifier from an output stream.
	 *
	 * @param inputStream the input stream containing the previously saved internal state of an incremental classifier
	 * @return the newly created incremental classifier
	 * @throws IOException if an error should occur during the reading
	 */
	public static IncrementalClassifier load(final InputStream inputStream) throws IOException
	{
		return load(inputStream, null);
	}

	/**
	 * Loads the previously saved internal state of an incremental classifier from an output stream.
	 *
	 * @param inputStream the input stream containing the previously saved internal state of an incremental classifier
	 * @param loadedOntology previous state.
	 * @return the newly created incremental classifier
	 * @throws IOException if an error should occur during the reading
	 */
	public static IncrementalClassifier load(final InputStream inputStream, final OWLOntology loadedOntology) throws IOException
	{
		ModuleExtractor extractor = null;
		TaxonomyImpl<OWLClass> taxonomy = null;

		final ZipInputStream zipInputStream = new ZipInputStream(inputStream);

		extractor = ModuleExtractorFactory.createModuleExtractor();
		extractor.load(zipInputStream);

		ZipEntry currentEntry = zipInputStream.getNextEntry();

		if (!TAXONOMY_FILE_NAME.equals(currentEntry.getName()))
			throw new IOException(String.format("Unexpected entry (%s) in ZipInputStream. Expected %s", currentEntry.getName(), TAXONOMY_FILE_NAME));

		taxonomy = TaxonomyPersistence.load(zipInputStream);

		final Properties properties = new Properties();
		currentEntry = zipInputStream.getNextEntry();
		if (currentEntry != null && PROPERTIES_FILE_NAME.equals(currentEntry.getName()))
			properties.load(zipInputStream);

		final boolean realized = Boolean.valueOf(properties.getProperty(REALIZED_PROPERTY, "false"));

		final IncrementalClassifier.PersistedState persistedState = new IncrementalClassifier.PersistedState(extractor, taxonomy, realized);

		if (loadedOntology != null)
			return new IncrementalClassifier(persistedState, loadedOntology);
		else
			return new IncrementalClassifier(persistedState);
	}
}
