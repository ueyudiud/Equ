/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.util;

/**
 * @author ueyudiud
 */
public interface ILiteralFactory
{
	Literal parseHexLiteral(String number, String suffix);
	
	Literal parseOctLiteral(String number, String suffix);
	
	Literal parseDecLiteral(String number, String suffix);
	
	Literal parseCustomLiteral(String value);
}
