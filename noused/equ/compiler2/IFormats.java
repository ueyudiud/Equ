/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

/**
 * @author ueyudiud
 */
public interface IFormats
{
	TokenType[] typesRequired();
	
	void buildPattern(IScanner[] scanner);
}
