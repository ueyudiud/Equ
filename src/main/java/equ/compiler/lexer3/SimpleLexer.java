/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.lexer3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

import com.google.common.primitives.Chars;

/**
 * @author ueyudiud
 */
class SimpleLexer<T> implements Lexer<T>
{
	Object[][] transforms;
	
	@Override
	public Iterator<T> parse(char[] array, int start, int end)
	{
		return null;
	}
	
	Result<?> transform(char[] array, int start, int end)
	{
		for (Object[] transform : this.transforms)
		{
			Result<?> result = transform(array, start, end, transform);
			if (result != Result.DENIED)
				return result;
		}
		return Result.DENIED;
	}
	
	Result<?> transform(char[] array, int start, int end, Object[] format)
	{
		switch ((ParserType) format[0])
		{
		case ARRAY : return start < array.length && Chars.contains((char[]) format[1], array[start]) ?
				new Result(start + 1, null) : Result.DENIED;
		case RANGING : return start < array.length &&
				array[start] >= ((char[]) format[1])[0] && array[start] <= ((char[]) format[1])[1] ?
						new Result(start + 1, null) : Result.DENIED;
		case CHARTEST : return start < array.length &&
				((CharPredicate) format[1]).test(array[start]) ?
						new Result(start + 1, null) : Result.DENIED;
		case LIST :
		{
			int i = start;
			Object[] r = new Object[((Object[][]) format[1]).length];
			int c = 0;
			for (Object[] t : (Object[][]) format[1])
			{
				Result<?> result = transform(array, i, end, t);
				if (result == Result.DENIED)
					return Result.DENIED;
				i = result.pos;
				r[c++] = result.transformed;
			}
			return new Result(i, format.length == 3 ? ((Function) format[2]).apply(r) : r);
		}
		case OR :
		{
			for (Object[] t : (Object[][]) format[1])
			{
				Result<?> result = transform(array, start, end, t);
				if (result != Result.DENIED)
					return result;
			}
			return Result.DENIED;
		}
		case ORELSE :
		{
			Result<?> result = transform(array, start, end, (Object[]) format[1]);
			if (result == Result.DENIED)
			{
				return new Result(start, null);
			}
			return new Result(start, null);
		}
		case SOME :
		{
			List<Object> list = new ArrayList<>();
			Result<?> result;
			do
			{
				result = transform(array, start, end, (Object[]) format[1]);
				list.add(result.transformed);
			}
			while (result != Result.DENIED);
			return new Result(result.pos, list.toArray());
		}
		default:
			throw new InternalError();
		}
	}
}

enum ParserType
{
	RANGING,
	ARRAY,
	CHARTEST,
	LIST,
	OR,
	ORELSE,
	SOME;
}

class Result<T>
{
	static final Result DENIED = new Result(-1, null);
	
	int pos;
	T transformed;
	
	Result(int pos, T transformed)
	{
		this.pos = pos;
		this.transformed = transformed;
	}
}