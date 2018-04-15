/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler2;

/**
 * @author ueyudiud
 */
public interface IScanner
{
	TokenType peek();
	
	boolean peek(TokenType type);
	
	TokenType next();
	
	void next(TokenType type);
	
	boolean markSupported();
	
	default void mark()
	{
		throw new UnsupportedOperationException();
	}
	
	default void reset()
	{
		throw new UnsupportedOperationException();
	}
	
	String ident();
	
	void setIdent(String ident);
	
	Literal literal();
	
	void setLiteral(Literal literal);
}
