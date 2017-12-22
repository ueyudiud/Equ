/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.util;

/**
 * @author ueyudiud
 */
public class Literal
{
	public LitType type;
	public Object value;
	
	public Literal(LitType type, Object value)
	{
		this.type = type;
		this.value = value;
	}
	
	@Override
	public String toString()
	{
		return this.type.name() + " " + this.value;
	}
	
	public static enum LitType
	{
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		CHAR,
		STRING,
		CNUMBER,
		CNAMING;
	}
}
