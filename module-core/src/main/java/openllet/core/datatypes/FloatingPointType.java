package openllet.core.datatypes;

/**
 * <p>
 * Title: Floating Point Type
 * </p>
 * <p>
 * Description: Interface to provide a common set of methods for Float and Double types
 * </p>
 * <p>
 * Copyright: Copyright (c) 2009
 * </p>
 * <p>
 * Company: Clark & Parsia, LLC. <http://www.clarkparsia.com>
 * </p>
 *
 * @author Mike Smith
 * @param <T> kind of numbers
 */
public interface FloatingPointType<T extends Number & Comparable<T>>
{

	/**
	 * Casts an object to the class described by this <code>FloatingPointType</code> object.
	 *
	 * @param o the object to cast
	 * @return <code>o</code> in the appropriate type
	 * @throws ClassCastException if the object is not null and is no assignable to the type <code>T</code>.
	 */
	T cast(Object o);

	/**
	 * @param n The value to decrement Return the next floating point value in the direction of <code>-Inf<code>.
	 * &#64;return If <code>n > -Inf</code>, the next floating point value in the direction of <code>-Inf<code>.  If <code>n == -Inf</code>, <code>-Inf</code>.
	 * @return The kind of elements
	 * @throws IllegalArgumentException if <code>n.isNaN()</code>
	 */
	T decrement(T n);

	/**
	 * Gets the Not-a-Number constant for the type.
	 *
	 * @return <code>T.NaN</code>
	 */
	T getNaN();

	/**
	 * Gets the negative infinity constant for the type.
	 *
	 * @return <code>T.NEGATIVE_INFINITY</code>
	 */
	T getNegativeInfinity();

	/**
	 * Gets the positive infinity constant for the type.
	 *
	 * @return <code>T.POSITIVE_INFINITY</code>
	 */
	T getPositiveInfinity();

	/**
	 * @param n The value to increment Return the next floating point value in the direction of <code>+Inf<code>.
	 *
	 * &#64;return If <code>n < +Inf</code>, the next floating point value in the direction of <code>+Inf<code>.  If <code>n == +Inf</code>, <code>+Inf</code>.
	 * @return The kind of elements
	 * @throws IllegalArgumentException if <code>n.isNaN()</code>
	 */
	T increment(T n);

	/**
	 * Count the number of floating point values in an inclusive interval
	 *
	 * @param lower The lower bound of the interval
	 * @param upper The upper bound of the interval
	 * @return If <code>lower.equals(upper)</code>, <code>1</code>. Else, the number of floating point values between <code>lower</code> and <code>upper</code>
	 *         plus <code>2</code>
	 */
	Number intervalSize(T lower, T upper);

	/**
	 * Determine if the specified <code>Object</code> is assignment compatible with the object represented by this <code>FloatingPointType</code>. Typically
	 * implemented as a wrapper for <code>T.class.isInstance(Object)</code>.
	 *
	 * @param o the object to check
	 * @return <code>true</code> if <code>o</code> is an instance of <code>T</code>, <code>false</code> else.
	 */
	boolean isInstance(Object o);

	/**
	 * Returns <code>true</code> if this floating point value is a Not-a-Number (NaN) value, <code>false</code> otherwise.
	 *
	 * @param f the value to be tested
	 * @return <code>true</code> if the argument is NaN, else <code>false</code>
	 */
	boolean isNaN(T f);
}
