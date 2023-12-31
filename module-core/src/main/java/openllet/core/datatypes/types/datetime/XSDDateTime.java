package openllet.core.datatypes.types.datetime;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import openllet.core.datatypes.RestrictedDatatype;
import openllet.core.utils.ATermUtils;
import openllet.core.utils.Namespaces;

/**
 * <p>
 * Title: <code>xsd:dateTime</code>
 * </p>
 * <p>
 * Description: Singleton implementation of <code>xsd:dateTime</code> datatype
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 */
public class XSDDateTime extends AbstractTimelineDatatype
{

	private static final XSDDateTime instance;

	static
	{
		instance = new XSDDateTime();
	}

	public static XSDDateTime getInstance()
	{
		return instance;
	}

	private final RestrictedTimelineDatatype dataRange;

	private XSDDateTime()
	{
		super(ATermUtils.makeTermAppl(Namespaces.XSD + "dateTime"), DatatypeConstants.DATETIME);

		dataRange = new RestrictedTimelineDatatype(this, DatatypeConstants.DATETIME, false);
	}

	@Override
	public RestrictedDatatype<XMLGregorianCalendar> asDataRange()
	{
		return dataRange;
	}
}
