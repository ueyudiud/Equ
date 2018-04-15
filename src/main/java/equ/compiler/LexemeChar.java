/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class LexemeChar extends LexemeAbstract
{
	private final char value;
	
	public LexemeChar(char value, SourceTrace trace)
	{
		super(trace);
		this.value = value;
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.LIC;
	}
	
	@Override
	public String stringValue()
	{
		return Character.toString(value);
	}
	
	@Override
	public double doubleValue()
	{
		return value;
	}
	
	@Override
	public float floatValue()
	{
		return value;
	}
	
	@Override
	public long longValue()
	{
		return value;
	}
	
	@Override
	public int intValue()
	{
		return value;
	}
	
	@Override
	public boolean boolValue()
	{
		return value != 0;
	}
}
