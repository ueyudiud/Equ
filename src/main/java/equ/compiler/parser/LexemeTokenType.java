/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

/**
 * @author ueyudiud
 */
public enum LexemeTokenType
{
	END,
	IDENTIFIER(true),
	OPERATOR(true),
	LITERAL,
	WHITE(' '),
	LPANC('('),
	LBRACE('['),
	LBRACKET('{'),
	LNAMING("<|", false, false),
	RPANC(')'),
	RBRACE(']'),
	RBRACKET('}'),
	RNAMING("|>", false, false),
	SEMI(';'),
	COMMA(','),
	DOT('.'),
	SHARP("#", false, false),
	
	TYPE_LOWER("<:", false, false),
	TYPE_GREATER(">:", false, false),
	
	RETURN("return"),
	IF("if"),
	ELSE("else"),
	WHILE("while"),
	DO("do"),
	VAR("var"),
	VAL("val"),
	NEW("new"),
	PUBLIC("public"),
	PROTECTED("protected"),
	PRIVATE("private"),
	SYNCHRONIZED("synchronized"),
	NATIVE("native"),
	STATIC("static"),
	TEMPLATE("template"),
	TYPE("type"),
	TYPEDEF("typedef"),
	CLASS("class"),
	ENUM("enum"),
	OBJECT("object"),
	INTERFACE("interface"),
	EXTENDS("extends"),
	SUPER("super"),
	THIS("this"),
	THAT("that"),
	ASM("asm"),
	
	MACRO("#", false, false),
	MACRO_ENDING;
	
	final String name;
	final boolean identifier;
	final boolean keyword;
	
	LexemeTokenType()
	{
		this(null, false, false);
	}
	LexemeTokenType(String name)
	{
		this(name, true, true);
	}
	LexemeTokenType(boolean unused)
	{
		this(null, true, false);
	}
	LexemeTokenType(char name)
	{
		this(Character.toString(name), false, false);
	}
	LexemeTokenType(String name, boolean identifier, boolean keyword)
	{
		this.name = name;
		this.identifier = identifier;
		this.keyword = keyword;
	}
}
