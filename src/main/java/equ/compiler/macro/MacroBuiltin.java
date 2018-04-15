/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.macro;

import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import equ.compiler.ILexeme;
import equ.compiler.IMacro;
import equ.compiler.LexemeInt;
import equ.compiler.LexemeString;
import equ.util.Numbers;

/**
 * @author ueyudiud
 */
public enum MacroBuiltin implements IMacro
{
	__TIME__
	{
		final DateFormat format = DateFormat.getTimeInstance();
		
		@Override
		public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
		{
			return ImmutableList.of(new LexemeString(format.format(new Date()).toCharArray(), header.trace()));
		}
	},
	__DATE__
	{
		final DateFormat format = DateFormat.getDateInstance();
		
		@Override
		public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
		{
			return ImmutableList.of(new LexemeString(format.format(new Date()).toCharArray(), header.trace()));
		}
	},
	__PATH__
	{
		@Override
		public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
		{
			return ImmutableList.of(new LexemeString(header.trace().path.toCharArray(), header.trace()));
		}
	},
	__LINE__
	{
		@Override
		public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
		{
			return ImmutableList.of(new LexemeInt(Numbers.fromInteger(header.trace().ln), header.trace()));
		}
	},
	__INFO__
	{
		@Override
		public int parameterCount()
		{
			return 1;
		}
		
		@Override
		public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
		{
			final List<ILexeme> list = parameters[0];
			char[] value;
			switch (list.size())
			{
			case 0 : value = new char[0]; break;
			case 1 : value = list.get(0).charsValue().clone(); break;
			default:
				value = new char[list.size() + list.stream().mapToInt(ILexeme::charsLength).sum() - 1];
				Iterator<ILexeme> itr = list.iterator();
				ILexeme lexeme = itr.next();
				lexeme.copyValue(value, 0);
				int i = lexeme.charsLength();
				while (itr.hasNext())
				{
					lexeme = itr.next();
					value[i ++] = ' ';
					lexeme.copyValue(value, i);
					i += lexeme.charsLength();
				}
				break;
			}
			return ImmutableList.of(new LexemeString(value, header.trace()));
		}
	};
	
	public int parameterCount() { return -1; }
	
	/** Unused. */
	public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters) { return ImmutableList.of(); }
}
