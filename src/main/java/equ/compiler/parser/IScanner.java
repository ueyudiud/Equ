/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

import equ.compiler.util.Literal;
import equ.compiler.util.Name;

/**
 * @author ueyudiud
 */
public interface IScanner
{
	LexemeTokenType scan();
	
	Literal lit();
	
	void setLiteral(Literal literal);
	
	Name ident();
	
	void setIdentifier(Name identifier);
}
