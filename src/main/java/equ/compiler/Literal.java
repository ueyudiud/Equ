/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class Literal
{
	public final LiteralType type;
	public final Object value;
	
	public Literal(LiteralType type, Object value)
	{
		this.type = type;
		this.value = value;
	}
	
	public enum LiteralType
	{
		INT,
		LONG,
		FLOAT,
		DOUBLE,
		STRING,
		CHAR,
		BOOL,
		NULL,
		CINT,
		CFLOAT,
		CNAMING;
	}
}
