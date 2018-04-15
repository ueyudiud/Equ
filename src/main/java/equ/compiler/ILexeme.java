/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public interface ILexeme
{
	EnumLexType type();
	
	String display();
	
	String stringValue();
	
	int charsLength();
	
	char[] charsValue();
	
	void copyValue(char[] desc, int descPos);
	
	boolean boolValue();
	
	char charValue();
	
	int intValue();
	
	long longValue();
	
	float floatValue();
	
	double doubleValue();
	
	SourceTrace trace();
}
