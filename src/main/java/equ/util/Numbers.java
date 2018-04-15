/*
 * copyright© 2018 ueyudiud
 */
package equ.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * @author ueyudiud
 */
public final class Numbers
{
	private static final char DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
	
	private Numbers() { }
	
	public static final byte
	MODE_DEC = 0,
	MODE_HEX = 1,
	MODE_OCT = 2,
	MODE_BIN = 3;
	
	public static final byte
	DOT = '\20',
	EXP = '\21',
	POS = '\22',
	NEG = '\23';
	
	public static int log2(long l)
	{
		return (Float.floatToIntBits(l) >> 23 & 0xFF) - 127;
	}
	
	public static byte[] fromInteger(long l, int mode)
	{
		switch (mode)
		{
		case MODE_HEX: return fromInteger(l);
		case MODE_DEC: return fromString(Long.toString(l), MODE_DEC);
		default:
			throw new IllegalArgumentException();
		}
	}
	
	public static byte[] fromInteger(long l)
	{
		if (l == 0)
		{
			return new byte[] {MODE_HEX, 0};
		}
		else
		{
			int e = l < 0 ? 16 : log2(l) / 4 + 1;
			byte[] values = new byte[1 + e];
			values[0] = MODE_HEX;
			for (int e1 = 0; e1 < e; ++e1)
			{
				values[e - e1] = (byte) (l >>> (e1 << 2) & 0xF);
			}
			return values;
		}
	}
	
	public static byte[] fromString(String s, byte mode)
	{
		ByteBuffer buffer = ByteBuffer.allocate(s.length() + 1);
		buffer.put(mode);
		s.codePoints().forEachOrdered(i -> {
			switch (i)
			{
			case '0': case '1':
			case '2': case '3':
			case '4': case '5':
			case '6': case '7':
			case '8': case '9':
				buffer.put((byte) (i - '0'));
				break;
			case 'e':
				if (mode != MODE_HEX)
				{
					buffer.put(EXP);
					break;
				}
			case 'a': case 'b': case 'c':
			case 'd': case 'f':
				buffer.put((byte) (i - 'a' + 10));
				break;
			case 'E':
				if (mode != MODE_HEX)
				{
					buffer.put(EXP);
					break;
				}
			case 'A': case 'B': case 'C':
			case 'D': case 'F':
				buffer.put((byte) (i - 'A' + 10));
				break;
			case '.':
				buffer.put(DOT);
				break;
			case '+':
				buffer.put(POS);
				break;
			case '-':
				buffer.put(NEG);
				break;
			case 'p':
				buffer.put(EXP);
				break;
			default:
				int digit;
				if ((digit = Character.digit(i, 16)) != -1)
				{
					buffer.put((byte) digit);
					break;
				}
				throw new NumberFormatException();
			}
		});
		return Arrays.copyOf(buffer.array(), buffer.position());
	}
	
	public static String toDecString(byte[] source)
	{
		switch (source[0])
		{
		case MODE_DEC: return toString(source);
		case MODE_HEX: return toString0(source, 16);
		case MODE_OCT: return toString0(source, 8);
		case MODE_BIN: return toString0(source, 2);
		default: throw new IllegalArgumentException();
		}
	}
	
	private static String toString0(byte[] source, int radix)
	{
		final int len = source.length - 1;
		StringBuilder builder = new StringBuilder(len);
		for (int i = 0; i < len; builder.append(DIGITS[source[++ i]]));
		BigInteger i = new BigInteger(builder.toString(), radix);
		return i.toString();
	}
	
	public static String toString(byte[] source)
	{
		int o;
		boolean flag;
		final int len;
		final char[] target;
		switch (source[0])
		{
		case MODE_HEX:
			flag = true;
			o = 1;
			len = source.length + 1;
			target = new char[len];
			target[0] = '0';
			target[1] = 'x';
			break;
		case MODE_BIN:
			flag = false;
			o = 1;
			len = source.length + 1;
			target = new char[len];
			target[0] = '0';
			target[1] = 'b';
			break;
		case MODE_OCT:
			flag = false;
			o = 1;
			len = source.length + 1;
			target = new char[len];
			target[0] = '0';
			target[1] = 'o';
			break;
		default:
			flag = false;
			o = -1;
			len = source.length - 1;
			target = new char[len];
			break;
		}
		for (int i = 1; i < source.length; ++i)
		{
			switch (source[i])
			{
			case DOT: target[i + o] = '.'; break;
			case EXP: target[i + o] = flag ? 'p' : 'e'; break;
			case POS: target[i + o] = '+'; break;
			case NEG: target[i + o] = '-'; break;
			default :
				if (source[i] < 16)
				{
					target[i + o] = DIGITS[source[i]];
					break;
				}
				throw new InternalError();
			}
		}
		return new String(target);
	}
	
	private static class cache { long l; }
	
	private static cache $()
	{
		return new cache();
	}
	
	public static long parseLong(String s)
	{
		return parseLong(fromString(s, MODE_DEC));
	}
	
	public static long parseLong(byte[] source)
	{
		switch (source[0])
		{
		case MODE_DEC:
			cache c = $();
			parseDecLong(source, 0, c);
			return c.l;
		case MODE_HEX:
		{
			long r = 0;
			for (int i = 1; i < source.length; ++i)
			{
				r = r << 4 | source[i];
			}
			return r;
		}
		default:
			throw new InternalError();
		}
	}
	
	private static int parseDecLong(byte[] source, int from, cache c)
	{
		boolean neg = false;
		int p = from;
		switch (source[p])
		{
		case NEG:
			neg = true;
		case POS:
			++p;
			break;
		}
		long r = 0;
		digit: while (p < source.length)
		{
			switch (source[p])
			{
			case 0 : case 1 :
			case 2 : case 3 :
			case 4 : case 5 :
			case 6 : case 7 :
			case 8 : case 9 :
				r = 10 * r - source[p ++];
				if (r > 0)
					throw new NumberFormatException("Number out of range.");
				break;
			default:
				break digit;
			}
		}
		c.l = neg ? r : -r;
		return p;
	}
	
	private static final int MAX_DEC_EXP = 308, MIN_DEC_EXP = -324;
	
	private static final double EXPS_DEC[], EXPS_HEX[];
	
	static
	{
		EXPS_DEC = new double[1 + MAX_DEC_EXP - MIN_DEC_EXP];
		double
		x = 1.0;
		for (int i = 0; i <= MAX_DEC_EXP; ++i)
		{
			EXPS_DEC[i - MIN_DEC_EXP] = x;
			x *= 10.0;
		}
		x = 0.1;
		for (int i = -MIN_DEC_EXP - 1; i >= 0; --i)
		{
			EXPS_DEC[i] = x;
			x /= 10.0;
		}
		EXPS_HEX = new double[1 + Double.MAX_EXPONENT - Double.MIN_EXPONENT];
		x = 0x1.0p+0;
		for (int i = 0; i <= Double.MAX_EXPONENT; ++i)
		{
			EXPS_HEX[i - Double.MIN_EXPONENT] = x;
			x *= 16.0;
		}
		x = 0x1.0p-4;
		for (int i = -Double.MIN_EXPONENT - 1; i >= 0; --i)
		{
			EXPS_HEX[i] = x;
			x /= 16.0;
		}
	}
	
	public static double parseDouble(String s)
	{
		return parseDouble(fromString(s, MODE_DEC));
	}
	
	public static double parseDouble(byte[] source)
	{
		long b;
		int e;
		switch (source[0])
		{
		case MODE_DEC:
		{
			boolean neg = false;
			int p = 1;
			switch (source[p])
			{
			case NEG:
				neg = true;
			case POS:
				p ++;
				break;
			}
			b = 0;
			e = Integer.MAX_VALUE;
			int o = 0;
			digit:
			{
				while (p < source.length)
				{
					switch (source[p])
					{
					default:
						if (o <= 56)
						{
							b = b * 10 + source[p ++];
							e --;
							o ++;
						}
						break;
					case DOT:
						e = 0;
						p ++;
						break;
					case EXP:
						p ++;
						cache c = $();
						parseDecLong(source, p, c);
						e += c.l;
						break digit;
					}
				}
				if (e > 0)
				{
					e = 0;
				}
			}
			if (e > MAX_DEC_EXP)
				throw new NumberFormatException("Out of range.");
			else if (e < MIN_DEC_EXP)
				return neg ? -0 : 0;
			else
				return (neg ? -b : b) * EXPS_DEC[e - MIN_DEC_EXP];
		}
		case MODE_HEX:
		{
			boolean neg = false;
			int p = 0;
			switch (source[p])
			{
			case NEG:
				neg = true;
			case POS:
				p ++;
				break;
			}
			b = 0;
			e = Integer.MAX_VALUE;
			int o = 0;
			digit:
			{
				while (p < source.length)
				{
					switch (source[p])
					{
					default:
						if (o <= 56)
						{
							b = b << 4 | source[p ++];
							e --;
							o ++;
						}
						break;
					case DOT:
						e = 0;
						p ++;
						break;
					case EXP:
						p ++;
						cache c = $();
						parseDecLong(source, p, c);
						e += c.l;
						break digit;
					}
				}
				if (e > 0)
				{
					e = 0;
				}
			}
			if (e > Double.MAX_EXPONENT || e < Double.MIN_EXPONENT)
				throw new NumberFormatException("Out of range.");
			return (neg ? -b : b) * EXPS_HEX[e - Double.MIN_EXPONENT];
		}
		default:
			throw new InternalError();
		}
	}
}
