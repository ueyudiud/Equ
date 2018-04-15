/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import equ.util.Numbers;

/**
 * @author ueyudiud
 */
public class LexemeInt extends LexemeAbstract
{
	private final byte[] arg;
	private final String name;
	
	public LexemeInt(byte[] arg, SourceTrace trace)
	{
		super(trace);
		this.arg = arg;
		name = Numbers.toDecString(arg);
	}
	
	@Override
	public EnumLexType type()
	{
		return EnumLexType.LII;
	}
	
	@Override
	public String stringValue()
	{
		return name;
	}
	
	@Override
	public double doubleValue()
	{
		return Numbers.parseDouble(arg);
	}
	
	@Override
	public float floatValue()
	{
		return (float) doubleValue();
	}
	
	@Override
	public long longValue()
	{
		return Numbers.parseLong(arg);
	}
	
	@Override
	public int intValue()
	{
		return (int) longValue();
	}
	
	@Override
	public boolean boolValue()
	{
		return longValue() != 0;
	}
}
