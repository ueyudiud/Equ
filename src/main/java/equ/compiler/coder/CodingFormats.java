/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.coder;

import static equ.compiler.coder.ICodingFormat.ARRAY_RANGE_MATCHER;
import static equ.compiler.coder.ICodingFormat.IDENT;
import static equ.compiler.coder.ICodingFormat.TO;
import static equ.compiler.coder.ICodingFormat.TYPE_ARRAY;
import static equ.compiler.coder.ICodingFormat.TYPE_ARRAY_CHECK;
import static equ.compiler.coder.ICodingFormat.TYPE_CALLER;
import static equ.compiler.coder.ICodingFormat.TYPE_FUNC;
import static equ.compiler.coder.ICodingFormat.TYPE_PARAMETERIZED;
import static equ.compiler.coder.ICodingFormat.TYPE_POINTER;
import static equ.compiler.coder.ICodingFormat.TYPE_REFLECT;
import static equ.compiler.coder.ICodingFormat.TYPE_SIMPLE;
import static equ.compiler.coder.ICodingFormat.TYPE_TUPLE;
import static equ.compiler.coder.ICodingFormat.TYPE_UNIT;

import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class CodingFormats
{
	public static String toString(Object codes)
	{
		return toString(codes, new StringBuilder()).toString();
	}
	
	private static StringBuilder toString(Object codes, StringBuilder builder)
	{
		return toString((Object[]) codes, builder);
	}
	
	private static StringBuilder toString(Object[] codes, StringBuilder builder)
	{
		switch ((int) codes[0])
		{
		case IDENT :
			return builder.append(codes[1]);
		case TO :
			return toString(codes[1], builder).append('.').append(codes[2]);
		case TYPE_SIMPLE :
			return toString(codes[1], builder);
		case TYPE_REFLECT :
			return toString(codes[1], builder).append('#').append(codes[2]);
		case TYPE_ARRAY :
			return toString(codes[1], builder).append('[').append(']');
		case TYPE_ARRAY_CHECK :
			return toString(codes[2], toString(codes[1], builder).append('[')).append(']');
		case TYPE_POINTER :
			return toString(codes[1], builder).append('*');
		case TYPE_PARAMETERIZED :
			return toString(codes[1], builder).append("<|").append(
					Strings.toString((Object[]) codes[2], CodingFormats::toString)).append("|>");
		case TYPE_TUPLE :
			return builder.append('(').append(
					Strings.toString((Object[]) codes[1], CodingFormats::toString)).append(')');
		case TYPE_FUNC :
			return toString(codes[2], toString(codes[1], builder).append("=>"));
		case TYPE_UNIT :
			return builder.append('(').append(')');
		case TYPE_CALLER :
			return toString(codes[1], builder.append("=>"));
		case ARRAY_RANGE_MATCHER :
		{
			return builder.append(Strings.toString((Object[]) codes[1], '|', rawRange ->
			{
				int[] range = (int[]) rawRange;
				if (range[0] == range[1])
					return Integer.toString(range[0]);
				else if (range[1] == Integer.MAX_VALUE)
					return range[0] + "~";
				else
					return range[0] + "~" + range[1];
			}));
		}
		default:
			throw new InternalError();
		}
	}
}
