/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.coder;

import static equ.compiler.coder.ICodingFormat.ARRAY_RANGE_MATCHER;
import static equ.compiler.coder.ICodingFormat.BLOCK;
import static equ.compiler.coder.ICodingFormat.EXPR;
import static equ.compiler.coder.ICodingFormat.EXPR_BLOCK;
import static equ.compiler.coder.ICodingFormat.FUNC;
import static equ.compiler.coder.ICodingFormat.IDENT;
import static equ.compiler.coder.ICodingFormat.LITERAL;
import static equ.compiler.coder.ICodingFormat.OPERATOR;
import static equ.compiler.coder.ICodingFormat.OPERATOR_UNARY;
import static equ.compiler.coder.ICodingFormat.PARAMETERIZED;
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

import java.util.function.BiFunction;

import equ.compiler.util.Literal;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class CodingFormats
{
	public static String toString(Object codes)
	{
		return toString((Object[]) codes, new StringBuilder(), 0).toString();
	}
	
	private static StringBuilder toString(Object codes, StringBuilder builder)
	{
		return toString((Object[]) codes, builder);
	}
	
	private static StringBuilder toString(Object[] codes, StringBuilder builder, int tab)
	{
		switch ((int) codes[0])
		{
		case BLOCK :
		{
			builder.append('{').append('\n');
			Object[] exprs = (Object[]) codes[1];
			tab ++;
			for (Object expr : exprs)
			{
				toString((Object[]) expr, builder.append(Strings.repeat('\t', tab)), tab).append(';').append('\n');
			}
			builder.append(Strings.repeat('\t', tab - 1)).append('}');
			return builder;
		}
		default:
			return toString(codes, builder);
		}
	}
	
	private static StringBuilder toString(Object[] codes, StringBuilder builder)
	{
		switch ((int) codes[0])
		{
		case LITERAL :
			return builder.append(((Literal) codes[1]).value);
		case EXPR :
			return append(builder, codes, 1, CodingFormats::toString);
		case EXPR_BLOCK :
			return append(builder.append((boolean) codes[2] ? '[' : '('), codes, 1, CodingFormats::toString).append((boolean) codes[3] ? ']' : ')');
		case OPERATOR :
			return (int) ((Object[]) codes[3])[0] != EXPR_BLOCK ?
					toString(codes[3], toString(codes[1], builder).append('.').append(codes[2]).append('(')).append(')') :
						toString(codes[3], toString(codes[1], builder).append('.').append(codes[2]));
		case OPERATOR_UNARY :
			return toString(codes[1], builder).append('.').append(codes[2]).append('(').append(')');
		case PARAMETERIZED :
			return append(toString(codes[1], builder).append("<|"), codes, 2, CodingFormats::toString).append("|>");
		case IDENT :
			return builder.append(codes[1]);
		case FUNC :
			return toString(codes[2], toString(codes[1], builder));
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
	
	private static StringBuilder append(StringBuilder builder, Object[] codes, int idx, BiFunction<Object[], StringBuilder, StringBuilder> toString)
	{
		Object[] elements = (Object[]) codes[idx];
		switch (elements.length)
		{
		case 0 : return builder;
		case 1 : return toString.apply((Object[]) elements[0], builder);
		default:
			toString.apply((Object[]) elements[0], builder);
			for (int i = 1; i < elements.length;
					toString.apply((Object[]) elements[i++], builder.append(", ")));
			return builder;
		}
	}
}
