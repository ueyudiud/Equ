/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

/**
 * @author ueyudiud
 */
public enum Keyword
{
	TYPE,
	CLASS,
	ENUM,
	ATTRIBUTE,
	VAR,
	VAL,
	EXTENDS,
	SUPER,
	THIS,
	LABEL;
	
	public final String name = name().toLowerCase();
}
