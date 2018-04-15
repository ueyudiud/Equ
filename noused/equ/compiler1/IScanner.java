/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

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
	
	SourcePosition current();
	
	SourcePosition previous();
	
	void setErrPos(SourcePosition pos);
	
	SourcePosition err();
}
