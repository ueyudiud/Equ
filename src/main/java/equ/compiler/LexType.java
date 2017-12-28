/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public enum LexType
{
	IDENTIFIER,
	OPERATOR,
	LITERAL,
	COMMENT_LINE,
	COMMENT_BLOCK,
	COMMENT_DOC,
	//separator
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
	//keywords
	VAL,
	VAR,
	IF,
	ELSE,
	WHILE,
	DO,
	SWITCH,
	MATCH,
	CASE,
	PACKAGE,
	IMPORT,
	CLASS,
	ENUM,
	OBJECT,
	INTERFACE,
	EXTENDS,
	SUPER,
	THIS,
	THAT,
	//special
	NL,
	END;
}
