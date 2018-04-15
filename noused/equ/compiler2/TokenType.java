/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

/**
 * @author ueyudiud
 */
public enum TokenType
{
	IDENTIFIER,
	OPERATOR,
	LITERAL,
	ATTRIBUTE,
	KEYWORD,
	DOC,
	//Separator
	LPANC,
	RPANC,
	LBRACE,
	RBRACE,
	LBRACKET,
	RBRACKET,
	LNAMING,
	RNAMING,
	SHARP,
	DOT,
	COMMA,
	SEMI,
	//Special
	NL,
	END;
}
