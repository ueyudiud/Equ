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
}
