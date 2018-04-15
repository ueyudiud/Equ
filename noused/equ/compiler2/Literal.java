/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler2;

import java.io.Serializable;

/**
 * @author ueyudiud
 */
public class Literal
{
	public final LiteralType type;
	public final Serializable value;
	
	public Literal(LiteralType type, Serializable value)
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
