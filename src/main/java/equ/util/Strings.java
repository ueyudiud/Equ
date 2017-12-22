/*
 * copyright© 2017 ueyudiud
 */
package equ.util;

import java.util.Arrays;
import java.util.function.Function;

/**
 * @author ueyudiud
 */
public class Strings
{
	public static final String EMPTY = "";
	
	public static <E> String toString(E[] elements, Function<E, String> function)
	{
		switch (elements.length)
		{
		case 0 : return EMPTY;
		case 1 : return function.apply(elements[0]);
		default:
			StringBuilder builder = new StringBuilder();
			builder.append(function.apply(elements[0]));
			for (int i = 1; i < elements.length; ++i)
			{
				builder.append(',').append(' ').append(function.apply(elements[i]));
			}
			return builder.toString();
		}
	}
	
	public static <E> String toString(E[] elements, char joiner, Function<E, String> function)
	{
		switch (elements.length)
		{
		case 0 : return EMPTY;
		case 1 : return function.apply(elements[0]);
		default:
			StringBuilder builder = new StringBuilder();
			builder.append(function.apply(elements[0]));
			for (int i = 1; i < elements.length; ++i)
			{
				builder.append(joiner).append(function.apply(elements[i]));
			}
			return builder.toString();
		}
	}
	
	public static String repeat(char chr, int size)
	{
		switch (size)
		{
		case 0 : return EMPTY;
		case 1 : return Character.toString(chr);
		default:
			char[] chrs = new char[size];
			Arrays.fill(chrs, chr);
			return new String(chrs);
		}
	}
	
	public static String[] split(String str, char split)
	{
		if (str.length() == 0)
			return new String[0];
		
		String[] buf = new String[str.length() >> 1 | 1];
		int count = 0;
		char[] array = str.toCharArray();
		int l = 0;
		for (int i = 0; i < array.length; ++i)
		{
			if (array[i] == split)
			{
				buf[count++] = l == i ? EMPTY : new String(array, l, i - l);
				l = i + 1;
			}
		}
		
		if (l == 0)
			return new String[] { str };
		
		buf[count++] = new String(array, l, array.length - l);
		String[] result = new String[count];
		System.arraycopy(buf, 0, result, 0, count);
		return result;
	}
	
	public static String[] split(String str, char split, int maxSplit)
	{
		if (str.length() == 0)
			return new String[0];
		
		String[] buf = new String[maxSplit];
		maxSplit --;
		int count = 0;
		char[] array = str.toCharArray();
		int l = 0;
		for (int i = 0; i < array.length && count < maxSplit; ++i)
		{
			if (array[i] == split)
			{
				buf[count++] = l == i ? EMPTY : new String(array, l, i - l);
				l = i + 1;
			}
		}
		
		if (l == 0)
			return new String[] { str };
		
		buf[count++] = new String(array, l, array.length - l);
		if (count == buf.length)
			return buf;
		
		String[] result = new String[count];
		System.arraycopy(buf, 0, result, 0, count);
		return result;
	}
	
	private static final int[][] LIMIT1 = {
			{},
			{32, 32},
			{16, 16, 16, 16},
			{11, 11, 11, 11, 10, 10, 10, 10},
			{8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8 , 8}
	};
	
	private static final int[][] LIMIT2 = {
			{},
			{64, 64},
			{32, 32, 32, 32},
			{22, 22, 21, 21, 21, 21, 21, 21},
			{16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16}
	};
	
	public static int parseInt2(String s, int lpradix) throws NumberFormatException
	{
		final int radix = 1 << lpradix;
		
		if (s == null)
			throw new NumberFormatException("null");
		
		if (radix < Character.MIN_RADIX)
			throw new NumberFormatException("radix " + radix +
					" less than Character.MIN_RADIX");
		
		if (radix > Character.MAX_RADIX)
			throw new NumberFormatException("radix " + radix +
					" greater than Character.MAX_RADIX");
		
		int result;
		int digit;
		int i = 1, len = s.length();
		
		if (len > 0)
		{
			result = Character.digit(s.charAt(0), radix);
			if (result < 0)
			{
				throw new NumberFormatException("For input string: \"" + s + "\"");
			}
			if (len > LIMIT1[lpradix][result])
			{
				throw new NumberFormatException("For input string: \"" + s + "\"");
			}
			
			while (i < len)
			{
				digit = Character.digit(s.charAt(i++), radix);
				if (digit < 0)
				{
					throw new NumberFormatException("For input string: \"" + s + "\"");
				}
				result = result << lpradix | digit;
			}
		}
		else
		{
			throw new NumberFormatException("For input string: \"" + s + "\"");
		}
		return result;
	}
	
	public static long parseLong2(String s, int lpradix) throws NumberFormatException
	{
		final int radix = 1 << lpradix;
		
		if (s == null)
			throw new NumberFormatException("null");
		
		if (radix < Character.MIN_RADIX)
			throw new NumberFormatException("radix " + radix +
					" less than Character.MIN_RADIX");
		
		if (radix > Character.MAX_RADIX)
			throw new NumberFormatException("radix " + radix +
					" greater than Character.MAX_RADIX");
		
		long result;
		int digit;
		int i = 1, len = s.length();
		
		if (len > 0)
		{
			digit = Character.digit(s.charAt(0), radix);
			if (digit < 0)
			{
				throw new NumberFormatException("For input string: \"" + s + "\"");
			}
			if (len > LIMIT2[lpradix][digit])
			{
				throw new NumberFormatException("For input string: \"" + s + "\"");
			}
			result = digit;
			
			while (i < len)
			{
				digit = Character.digit(s.charAt(i++), radix);
				if (digit < 0)
				{
					throw new NumberFormatException("For input string: \"" + s + "\"");
				}
				result = result << lpradix | digit;
			}
		}
		else
		{
			throw new NumberFormatException("For input string: \"" + s + "\"");
		}
		return result;
	}
}
