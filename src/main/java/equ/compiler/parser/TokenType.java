/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

/**
 * @author ueyudiud
 */
public enum TokenType
{
	END,
	IDENTIFIER,
	LITERAL,
	WHITE(" ", false),
	LPANC("(", false),
	LBRACE("[", false),
	LBRACKET("{", false),
	LNAMING("<|", false),
	RPANC(")", false),
	RBRACE("]", false),
	RBRACKET("}", false),
	RNAMING("|>", false),
	SEMI(";", false),
	COMMA(",", false),
	DOT(".", false),
	SHARP("#", false),
	
	TYPE_LOWER("<:", false),
	TYPE_GREATER(">:", false),
	
	RETURN("return"),
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
	TRUE("true"),
	FALSE("false"),
	NULL("null"),
	
	MACRO("#", false),
	MACRO_ENDING;
	
	final String name;
	final boolean keyword;
	
	TokenType()
	{
		this(null, false);
	}
	TokenType(String name)
	{
		this(name, true);
	}
	TokenType(String name, boolean keyword)
	{
		this.name = name;
		this.keyword = keyword;
	}
}
