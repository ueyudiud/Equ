/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.macro;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import equ.compiler.ILexeme;
import equ.compiler.IMacro;
import equ.compiler.LexemeBool;
import equ.compiler.LexemeChar;
import equ.compiler.LexemeFloat;
import equ.compiler.LexemeInt;
import equ.compiler.LexemeString;
import equ.compiler.MacroTransformException;
import equ.compiler.SourceTrace;
import equ.util.Numbers;

/**
 * @author ueyudiud
 */
public class MacroNative implements IMacro
{
	private static Function<ILexeme, Object> iformat(Class<?> type)
	{
		if (type == int.class || type == Integer.class)
		{
			return ILexeme::intValue;
		}
		else if (type == long.class || type == Long.class)
		{
			return ILexeme::longValue;
		}
		else if (type == float.class || type == Float.class)
		{
			return ILexeme::floatValue;
		}
		else if (type == double.class || type == Double.class)
		{
			return ILexeme::doubleValue;
		}
		else if (type == char.class || type == Character.class)
		{
			return ILexeme::charValue;
		}
		else if (type == boolean.class || type == Boolean.class)
		{
			return ILexeme::boolValue;
		}
		else
		{
			return ILexeme::stringValue;
		}
	}
	
	private static BiFunction<Object, SourceTrace, ILexeme> oformat(Class<?> type)
	{
		if (type == int.class || type == Integer.class)
		{
			return (i, t) -> new LexemeInt(Numbers.fromInteger(((Number) i).intValue()), t);
		}
		else if (type == long.class || type == Long.class)
		{
			return (i, t) -> new LexemeInt(Numbers.fromInteger(((Number) i).longValue()), t);
		}
		else if (type == float.class || type == Float.class)
		{
			return (i, t) -> new LexemeFloat(Numbers.fromString(Float.toHexString(((Number) i).floatValue()), Numbers.MODE_HEX), t);
		}
		else if (type == double.class || type == Double.class)
		{
			return (i, t) -> new LexemeFloat(Numbers.fromString(Double.toHexString(((Number) i).doubleValue()), Numbers.MODE_HEX), t);
		}
		else if (type == char.class || type == Character.class)
		{
			return (i, t) -> new LexemeChar((char) i, t);
		}
		else if (type == boolean.class || type == Boolean.class)
		{
			return (i, t) -> new LexemeBool((boolean) i, t);
		}
		else
		{
			return (i, t) -> new LexemeString(i.toString().toCharArray(), t);
		}
	}
	
	private final IMacro eval;
	
	private final String name;
	private final int count;
	private final MethodHandle handle;
	private final Function<ILexeme, Object>[] parameterFormats;
	private final BiFunction<Object, SourceTrace, ILexeme> resultFormat;
	
	public MacroNative(IMacro eval, String name, MethodHandle handle)
	{
		this.eval = eval;
		this.name = name;
		MethodType mt = handle.type();
		count = mt.parameterCount();
		parameterFormats = new Function[count];
		Class<?>[] inputs = mt.parameterArray();
		for (int i = 0; i < count; ++i)
		{
			parameterFormats[i] = iformat(inputs[i]);
		}
		resultFormat = oformat(mt.returnType());
		this.handle = handle.asSpreader(Object[].class, count);
	}
	
	@Override
	public String name()
	{
		return name;
	}
	
	@Override
	public int parameterCount()
	{
		return count;
	}
	
	@Override
	public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
	{
		Object[] values = new Object[parameters.length];
		for (int i = 0; i < values.length; ++i)
		{
			try
			{
				values[i] = parameterFormats[i].apply(eval.transform(header, parameters[i]).get(0));
			}
			catch (UnsupportedOperationException exception)
			{
				throw new MacroTransformException("Illegal evaluated value.");
			}
		}
		try
		{
			return ImmutableList.of(resultFormat.apply(handle.invoke(values), header.trace()));
		}
		catch (Throwable t)
		{
			throw new MacroTransformException("Caught an exception during invoke native macro.", t);
		}
	}
}
