/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.preprocess;

import equ.compiler.EnumLexType;
import equ.compiler.ILexeme;
import equ.compiler.SourceTrace;

/**
 * @author ueyudiud
 */
class LexemeMacroVariable implements ILexeme
{
	private final String name;
	private final SourceTrace trace;
	
	final int index;
	
	public LexemeMacroVariable(SourceTrace target, ILexeme declearation, int id)
	{
		trace = new SourceTrace(target, declearation.trace());
		name = declearation.display();
		index = id;
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.ID;
	}
	
	@Override
	public String display()
	{
		return "<variable>";
	}
	
	@Override
	public String stringValue()
	{
		return name;
	}
	
	@Override
	public int charsLength()
	{
		return name.length();
	}
	
	@Override
	public char[] charsValue()
	{
		return stringValue().toCharArray();
	}
	
	@Override
	public void copyValue(char[] desc, int descPos)
	{
		name.getChars(0, name.length(), desc, descPos);
	}
	
	@Override
	public boolean boolValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public char charValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int intValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public long longValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public float floatValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double doubleValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public SourceTrace trace()
	{
		return trace;
	}
}
