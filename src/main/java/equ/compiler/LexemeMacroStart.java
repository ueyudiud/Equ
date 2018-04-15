/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class LexemeMacroStart extends LexemeUTF8
{
	public LexemeMacroStart(char[] source, SourceTrace trace)
	{
		super(source, trace);
	}
	
	@Override
	public String display()
	{
		return new StringBuilder().append('#').append(source).toString();
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.MAS;
	}
}
