/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
abstract class LexemeAbstract implements ILexeme
{
	final SourceTrace trace;
	
	public LexemeAbstract(SourceTrace trace)
	{
		this.trace = trace;
	}
	
	@Override
	public String display()
	{
		return stringValue();
	}
	
	@Override
	public String stringValue()
	{
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int charsLength()
	{
		return stringValue().length();
	}
	
	@Override
	public char[] charsValue()
	{
		return stringValue().toCharArray();
	}
	
	@Override
	public void copyValue(char[] desc, int descPos)
	{
		String value = stringValue();
		value.getChars(0, value.length(), desc, descPos);
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
	
	@Override
	public String toString()
	{
		return type() + ": " + display();
	}
}
