/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import java.util.function.BiFunction;

import equ.compiler.util.AbstractSourceInfo;

/**
 * @author ueyudiud
 */
class StringReplacement extends AbstractSourceInfo
implements BiFunction<StringBuilder, String[], StringBuilder>
{
	int parameterCount;
	int[] codes;
	String[] values;
	
	StringReplacement(int count, int[] codes, String[] values)
	{
		this.parameterCount = count;
		this.codes = codes;
		this.values = values;
	}
	
	String transform(String...arguments) throws PreoperationException
	{
		if (arguments.length != this.parameterCount)
			throw new PreoperationException("Illegal macro parameter count.", this);
		return apply(new StringBuilder(), arguments).toString();
	}
	
	@Override
	public StringBuilder apply(StringBuilder builder, String[] arguments)
	{
		for (int i : this.codes)
		{
			builder.append((i & 0x80000000) != 0 ? this.values[(i & ~0x80000000)] : arguments[i]);
		}
		return builder;
	}
}
