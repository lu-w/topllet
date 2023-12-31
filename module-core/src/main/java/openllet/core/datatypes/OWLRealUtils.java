package openllet.core.datatypes;

import static java.lang.String.format;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.DatatypeConverter;

import openllet.core.datatypes.types.real.Rational;
import openllet.shared.tools.Log;

/**
 * <p>
 * Title: <code>owl:real</code> Utilities
 * </p>
 * <p>
 * Description: Implementation bits to support <code>owl:real<code> and derived datatypes.
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
public class OWLRealUtils
{

	private enum Type
	{
		BIG_DECIMAL(5, BigDecimal.class, false), //
		BIG_INTEGER(4, BigInteger.class, true), //
		BYTE(0, Byte.class, true), //
		INTEGER(2, Integer.class, true), //
		LONG(3, Long.class, true), //
		RATIONAL(6, Rational.class, false), //
		SHORT(1, Short.class, true);

		private static Map<Class<?>, Type> _map = new HashMap<>();
		static
		{
			for (final Type t : values())
				_map.put(t._cls, t);
		}

		public static Type compareType(final Type a, final Type b)
		{
			return a._index > b._index ? a : b;
		}

		public static Type get(final Class<? extends Number> cls)
		{
			return _map.get(cls);
		}

		private final Class<? extends Number> _cls;
		private final int _index;

		private final boolean _integerOnly;

		private Type(final int index, final Class<? extends Number> cls, final boolean integerOnly)
		{
			_index = index;
			_cls = cls;
			_integerOnly = integerOnly;
		}

		public boolean isIntegerOnly()
		{
			return _integerOnly;
		}
	}

	private static Logger _logger = Log.getLogger(OWLRealUtils.class);

	public static boolean acceptable(final Class<? extends Number> c)
	{
		return Type.get(c) != null;
	}

	public static BigDecimal bigDecimal(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to bigDecimal method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}
		final BigDecimal d = (BigDecimal) convertFromTo(n, t, Type.BIG_DECIMAL);
		return d;
	}

	public static BigInteger bigInteger(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (null == t)
		{
			final String msg = format("Unexpected number type %s passed to bigInteger method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (Type.BIG_INTEGER.equals(t))
			return (BigInteger) n;

		return (BigInteger) convertFromTo(n, t, Type.BIG_INTEGER);
	}

	@SuppressWarnings("unchecked")
	public static int compare(final Number a, final Number b)
	{
		final Type ta = Type.get(a.getClass());
		if (ta == null)
			throw new IllegalArgumentException();

		final Type tb = Type.get(b.getClass());
		if (tb == null)
			throw new IllegalArgumentException();

		if (ta == tb)
			return ((Comparable<Number>) a).compareTo(b);

		final int sa = signum(a);
		final int sb = signum(b);
		if (sa == sb)
		{

			Comparable<Number> compA;
			Number compB;
			final Type target = Type.compareType(ta, tb);

			if (ta == target)
				compA = (Comparable<Number>) a;
			else
				compA = (Comparable<Number>) convertFromTo(a, ta, target);

			if (tb == target)
				compB = b;
			else
				compB = convertFromTo(b, tb, target);

			return compA.compareTo(compB);
		}
		else
			return sa > sb ? 1 : -1;
	}

	private static Number convertFromTo(final Number nParam, final Type inParam, final Type out)
	{
		Number n = nParam;
		Type in = inParam;

		if (Type.BIG_DECIMAL.equals(in) && out.isIntegerOnly())
		{
			in = Type.BIG_INTEGER;
			n = ((BigDecimal) n).toBigIntegerExact();
		}

		switch (out)
		{
			case BYTE:
				if (!in.equals(Type.BYTE))
					throw new IllegalArgumentException();
				return n;
			case SHORT:
				switch (in)
				{
					case BYTE:
						return n.shortValue();
					case SHORT:
						return n;
					default:
						throw new IllegalArgumentException();
				}
			case INTEGER:
				switch (in)
				{
					case BYTE:
					case SHORT:
						return n.intValue();
					case INTEGER:
						return n;
					default:
						throw new IllegalArgumentException();
				}
			case LONG:
				switch (in)
				{
					case BYTE:
					case SHORT:
					case INTEGER:
						return n.longValue();
					case LONG:
						return n;
					default:
						throw new IllegalArgumentException();
				}
			case BIG_INTEGER:
				switch (in)
				{
					case BYTE:
					case SHORT:
					case INTEGER:
					case LONG:
						return BigInteger.valueOf(n.longValue());
					case BIG_INTEGER:
						return n;
					case BIG_DECIMAL:
						try
						{
							return ((BigDecimal) n).toBigIntegerExact();
						}
						catch (final ArithmeticException e)
						{
							throw new IllegalArgumentException(e);
						}
					case RATIONAL:
						Rational r = (Rational) n;
						if (compare(1, r.getDenominator()) != 0)
						{
							r = Rational.simplify(r);
							if (compare(1, r.getDenominator()) != 0)
								throw new IllegalArgumentException();
						}
						return bigInteger(r.getNumerator());
					default:
						throw new IllegalArgumentException();
				}
			case BIG_DECIMAL:
				switch (in)
				{
					case BYTE:
					case SHORT:
					case INTEGER:
					case LONG:
						return BigDecimal.valueOf(n.longValue());
					case BIG_INTEGER:
						return new BigDecimal((BigInteger) n);
					case BIG_DECIMAL:
						return n;
					default:
				}
				//$FALL-THROUGH$
			case RATIONAL:
				switch (in)
				{
					case BYTE:
					case SHORT:
					case INTEGER:
					case LONG:
					case BIG_INTEGER:
						return new Rational(n, 1);
					case BIG_DECIMAL:
						final BigDecimal d = (BigDecimal) n;
						final Number num = d.unscaledValue();
						final int scale = d.scale();
						final Number denom = BigInteger.TEN.pow(scale);
						return new Rational(num, denom);
					case RATIONAL:
						return n;
					default:
				}
				//$FALL-THROUGH$
			default:
				throw new IllegalArgumentException();
		}
	}

	public static Number getCanonicalObject(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to integerIncrement method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		// TODO: re-implement shrink to avoid always going to BigInteger
		if (isInteger(n))
			return shrinkBigInteger(bigInteger(n));
		else
			if (Type.RATIONAL.equals(t))
			{
				final Rational r = (Rational) n;
				if (r.isQuotientExact())
					return getCanonicalObject(r.getQuotient());
				else
					return Rational.simplify(r);
			}
			else
				if (Type.BIG_DECIMAL.equals(t))
				{
					final BigDecimal d = (BigDecimal) n;
					int shift = 0;
					BigInteger unscaled = d.unscaledValue();
					BigInteger[] dandr = unscaled.divideAndRemainder(BigInteger.TEN);
					while (BigInteger.ZERO.equals(dandr[1]))
					{
						unscaled = dandr[0];
						shift--;
						dandr = unscaled.divideAndRemainder(BigInteger.TEN);
					}
					return shift == 0 ? d : d.setScale(d.scale() + shift);
				}
				else
					throw new IllegalStateException();
	}

	public static Number integerDecrement(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to integerIncrement method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (!t.isIntegerOnly())
			return shrinkBigInteger(bigInteger(n).subtract(BigInteger.ONE));
		else
			/**
			 * For Java rules on type promotions during addition, see
			 * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#183628" >the spec</a>
			 */
			switch (t)
			{
				case BYTE:
					final byte b = n.byteValue();
					return b > Byte.MIN_VALUE ? Byte.valueOf((byte) (b - 1)) : Short.valueOf((short) (b - 1));
				case SHORT:
					final short s = n.shortValue();
					return s > Short.MIN_VALUE ? Short.valueOf((short) (s - 1)) : Integer.valueOf(s - 1);
				case INTEGER:
					final int i = n.intValue();
					return i > Integer.MIN_VALUE ? Integer.valueOf(i - 1) : Long.valueOf(i - 1L);
				case LONG:
					final long l = n.longValue();
					return l > Long.MIN_VALUE ? Long.valueOf(l - 1) : BigInteger.valueOf(l).subtract(BigInteger.ONE);
				case BIG_INTEGER:
					final BigInteger bi = (BigInteger) n;
					return bi.subtract(BigInteger.ONE);
				default:
					throw new IllegalStateException();
			}
	}

	public static Number integerDifference(final Number a, final Number b)
	{
		final Type ta = Type.get(a.getClass());
		final Type tb = Type.get(b.getClass());
		if (ta == null || tb == null)
		{
			final String msg = format("Unexpected number type %s,%s passed to integerDifference method.", a.getClass().getCanonicalName(), b.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		final BigInteger ia = bigInteger(a);
		final BigInteger ib = bigInteger(b);

		return shrinkBigInteger(ia.subtract(ib));
	}

	public static Number integerIncrement(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to integerIncrement method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (!t.isIntegerOnly())
			return shrinkBigInteger(bigInteger(n).add(BigInteger.ONE));
		else
			/**
			 * For Java rules on type promotions during addition, see
			 * <a href="http://java.sun.com/docs/books/jls/second_edition/html/conversions.doc.html#183628" >the spec</a>
			 */
			switch (t)
			{
				case BYTE:
					final byte b = n.byteValue();
					return b < Byte.MAX_VALUE ? Byte.valueOf((byte) (b + 1)) : Short.valueOf((short) (b + 1));
				case SHORT:
					final short s = n.shortValue();
					return s < Short.MAX_VALUE ? Short.valueOf((short) (s + 1)) : Integer.valueOf(s + 1);
				case INTEGER:
					final int i = n.intValue();
					return i < Integer.MAX_VALUE ? Integer.valueOf(i + 1) : Long.valueOf(i + 1L);
				case LONG:
					final long l = n.longValue();
					return l < Long.MAX_VALUE ? Long.valueOf(l + 1) : BigInteger.valueOf(l).add(BigInteger.ONE);
				case BIG_INTEGER:
					final BigInteger bi = (BigInteger) n;
					return bi.add(BigInteger.ONE);
				default:
					throw new IllegalStateException();
			}
	}

	public static Number integerSum(final Number a, final Number b)
	{
		final Type ta = Type.get(a.getClass());
		final Type tb = Type.get(b.getClass());
		if (ta == null || tb == null)
		{
			final String msg = format("Unexpected number type %s,%s passed to integerSum method.", a.getClass().getCanonicalName(), b.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		final BigInteger ia = bigInteger(a);
		final BigInteger ib = bigInteger(b);

		return shrinkBigInteger(ia.add(ib));
	}

	public static boolean isDecimal(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to isInteger method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (t.isIntegerOnly())
			return true;

		if (t.equals(Type.BIG_DECIMAL))
			return true;

		if (t.equals(Type.RATIONAL))
		{
			final Rational ratVal = (Rational) n;
			return ratVal.isQuotientExact();
		}

		throw new IllegalStateException();
	}

	public static boolean isInteger(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to isInteger method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (t.isIntegerOnly())
			return true;

		if (t.equals(Type.BIG_DECIMAL))
		{
			final BigDecimal decVal = (BigDecimal) n;
			return BigInteger.ZERO.equals(decVal.unscaledValue()) || decVal.stripTrailingZeros().scale() <= 0;
		}
		else
			if (t.equals(Type.RATIONAL))
			{
				final Rational ratVal = (Rational) n;
				if (compare(1, ratVal.getDenominator()) == 0)
					return true;
				else
					return compare(1, Rational.simplify(ratVal).getDenominator()) == 0;
			}

		return false;
	}

	public static boolean isRational(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to isInteger method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (t.isIntegerOnly())
			return true;

		if (t.equals(Type.BIG_DECIMAL))
			return true;

		if (t.equals(Type.RATIONAL))
			return true;

		throw new IllegalStateException();
	}

	public static String print(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to print method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		switch (t)
		{
			case BYTE:
				return DatatypeConverter.printByte(n.byteValue());
			case SHORT:
				return DatatypeConverter.printShort(n.shortValue());
			case INTEGER:
				return DatatypeConverter.printInt(n.intValue());
			case LONG:
				return DatatypeConverter.printLong(n.longValue());
			case BIG_INTEGER:
				return DatatypeConverter.printInteger((BigInteger) n);
			case BIG_DECIMAL:
				return DatatypeConverter.printDecimal((BigDecimal) n);
			case RATIONAL:
				return ((Rational) n).toString();
			default:
				throw new IllegalStateException();
		}
	}

	public static Number roundCeiling(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to roundDown method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (t.isIntegerOnly())
			return n;

		if (Type.BIG_DECIMAL.equals(t))
		{
			final BigDecimal d = (BigDecimal) n;
			final BigDecimal[] dandr = d.divideAndRemainder(BigDecimal.ONE);
			if (dandr[1].equals(BigDecimal.ZERO) || d.signum() == -1)
				return shrinkBigInteger(dandr[0].toBigIntegerExact());
			else
				return shrinkBigInteger(dandr[0].toBigIntegerExact().add(BigInteger.ONE));
		}
		else
			if (Type.RATIONAL.equals(t))
			{
				final Rational r = (Rational) n;
				return roundCeiling(r.getQuotient());
			}
			else
				throw new IllegalStateException();
	}

	public static Number roundDown(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to roundDown method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (t.isIntegerOnly())
			return n;

		if (Type.BIG_DECIMAL.equals(t))
		{
			final BigDecimal d = (BigDecimal) n;
			return shrinkBigInteger(d.divideToIntegralValue(BigDecimal.ONE).toBigIntegerExact());
		}
		else
			if (Type.RATIONAL.equals(t))
			{
				final Rational r = (Rational) n;
				return roundDown(r.getQuotient());
			}
			else
				throw new IllegalStateException();
	}

	public static Number roundFloor(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to roundDown method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (t.isIntegerOnly())
			return n;

		if (Type.BIG_DECIMAL.equals(t))
		{
			final BigDecimal d = (BigDecimal) n;
			final BigDecimal[] dandr = d.divideAndRemainder(BigDecimal.ONE);
			if (dandr[1].equals(BigDecimal.ZERO) || d.signum() == 1)
				return shrinkBigInteger(dandr[0].toBigIntegerExact());
			else
				return shrinkBigInteger(dandr[0].toBigIntegerExact().subtract(BigInteger.ONE));
		}
		else
			if (Type.RATIONAL.equals(t))
			{
				final Rational r = (Rational) n;
				return roundFloor(r.getQuotient());
			}
			else
				throw new IllegalStateException();
	}

	/**
	 * Shrink an integer to the smallest supporting typef
	 *
	 * @param i the number to shrink
	 * @return the number is the smallest supporting type
	 */
	private static Number shrinkBigInteger(final BigInteger i)
	{
		final int sign = i.signum();
		if (sign == 0)
			return Byte.valueOf((byte) 0);
		else
			if (sign < 0)
			{
				if (i.compareTo(BigInteger.valueOf(Byte.MIN_VALUE)) > 0)
					return i.byteValue();
				else
					if (i.compareTo(BigInteger.valueOf(Short.MIN_VALUE)) > 0)
						return i.shortValue();
					else
						if (i.compareTo(BigInteger.valueOf(Integer.MIN_VALUE)) > 0)
							return i.intValue();
						else
							if (i.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) > 0)
								return i.longValue();
							else
								return i;
			}
			else
				if (i.compareTo(BigInteger.valueOf(Byte.MAX_VALUE)) < 0)
					return i.byteValue();
				else
					if (i.compareTo(BigInteger.valueOf(Short.MAX_VALUE)) < 0)
						return i.shortValue();
					else
						if (i.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) < 0)
							return i.intValue();
						else
							if (i.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) < 0)
								return i.longValue();
							else
								return i;
	}

	public static int signum(final Number n)
	{
		final Type t = Type.get(n.getClass());
		if (t == null)
		{
			final String msg = format("Unexpected number type %s passed to signum method.", n.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		switch (t)
		{
			case BIG_DECIMAL:
				return ((BigDecimal) n).signum();
			case BIG_INTEGER:
				return ((BigInteger) n).signum();
			case RATIONAL:
				return ((Rational) n).signum();
			case LONG:
				return Long.signum(n.longValue());
			case INTEGER:
			case SHORT:
			case BYTE:
				return Integer.signum(n.intValue());
			default:
				throw new IllegalArgumentException();
		}
	}

	public static Number sum(final Number a, final Number b)
	{
		if (isInteger(a) && isInteger(b))
			return integerSum(a, b);

		final Type ta = Type.get(a.getClass());
		final Type tb = Type.get(b.getClass());

		if (ta == null || tb == null)
		{
			final String msg = format("Unexpected number type %s,%s passed to integerSum method.", a.getClass().getCanonicalName(), b.getClass().getCanonicalName());
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		if (EnumSet.of(ta, tb).contains(Type.RATIONAL))
		{
			final String msg = format("Addition for rational numbers is not supported");
			_logger.warning(msg);
			throw new IllegalArgumentException(msg);
		}

		final BigDecimal da = (BigDecimal) convertFromTo(a, ta, Type.BIG_DECIMAL);
		final BigDecimal db = (BigDecimal) convertFromTo(b, tb, Type.BIG_DECIMAL);
		return getCanonicalObject(da.add(db));
	}

}
