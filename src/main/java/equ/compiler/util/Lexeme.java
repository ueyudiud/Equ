/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.util;

/**
 * @author ueyudiud
 */
public class Lexeme<E> extends AbstractSourceInfo
{
	public static <E> Lexeme<E> lexeme(int type, E lexeme)
	{
		return new Lexeme<>(type, lexeme);
	}
	
	public static <E> Lexeme<E> lexeme(int type, E lexeme, String source, String line, int ln, int col)
	{
		return new Lexeme<>(type, lexeme, source, line, ln, col);
	}
	
	public static <E> Lexeme<E> lexeme(int type, E lexeme, ISourceInfoProvider provider)
	{
		return new Lexeme<>(type, lexeme, provider);
	}
	
	public static final int
	END = 0,
	LITERAL_INT = 1,
	LITERAL_FLOAT = 2,
	LITERAL_CHAR = 3,
	LITERAL_STRING = 4,
	LITERAL_CUSTOM = 5,
	LITERAL_CUSTOM_NUM = 6,
	SEPERATOR = 7,
	SYMBOL = 8,
	MACRO = 10;
	
	public int type;
	public E lexeme;
	
	Lexeme(int type, E lexeme)
	{
		this.type = type;
		this.lexeme = lexeme;
	}
	
	Lexeme(int type, E lexeme, String source, String line, int ln, int col)
	{
		this.type = type;
		this.lexeme = lexeme;
		this.source = source;
		this.line = line;
		this.ln = ln;
		this.col = col;
	}
	Lexeme(int type, E lexeme, ISourceInfoProvider provider)
	{
		this.type = type;
		this.lexeme = lexeme;
		setSource(provider);
	}
	
	@Override
	public String toString()
	{
		return this.lexeme + "\r" + toSourceInfo();
	}
}
