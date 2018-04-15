/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.preprocess;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import equ.compiler.ILexeme;
import equ.compiler.IMacro;

/**
 * @author ueyudiud
 */
class MacroSimple implements IMacro
{
	private final String name;
	private final LexemeMacroVariable[] variables;
	private final ILexeme[] transform;
	
	MacroSimple(String name, LexemeMacroVariable[] variables, ILexeme[] transform)
	{
		this.name = name;
		this.variables = variables;
		this.transform = transform;
	}
	
	@Override
	public String name()
	{
		return name;
	}
	
	@Override
	public int parameterCount()
	{
		return variables != null ? variables.length : -1;
	}
	
	@Override
	public List<ILexeme> transform(ILexeme header, List<ILexeme>... parameters)
	{
		if (variables == null)
		{
			return Arrays.asList(transform);
		}
		else
		{
			List<ILexeme> lexemes = new ArrayList<>();
			for (ILexeme lexeme : transform)
			{
				if (lexeme instanceof LexemeMacroVariable)
				{
					lexemes.addAll(parameters[((LexemeMacroVariable) lexeme).index]);
				}
				else
				{
					lexemes.add(lexeme);
				}
			}
			return lexemes;
		}
	}
}
