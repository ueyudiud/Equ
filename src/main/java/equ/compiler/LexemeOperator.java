/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class LexemeOperator extends LexemeUTF8
{
	public LexemeOperator(char[] source, SourceTrace trace)
	{
		super(source, trace);
	}
	
	@Override
	public String display()
	{
		return new String(source);
	}
	
	@Override
	public String stringValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.OP;
	}
}
