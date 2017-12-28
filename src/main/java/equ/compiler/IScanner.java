/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public interface IScanner
{
	IScannerFactory getFactory();
	
	void scan();
	
	void mark();
	
	void reset();
	
	LexType type();
	
	void setIdent(Identifier identifier);
	
	Identifier ident();
	
	void setLit(Literal literal);
	
	Literal lit();
	
	void setCom(Comment comment);
	
	Comment com();
	
	void warn(String cause, Object...formats);
	
	void error(String cause, Object...formats);
	
	SourcePosition current();
	
	SourcePosition previous();
}
