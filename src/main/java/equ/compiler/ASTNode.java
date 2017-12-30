/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public abstract class ASTNode
{
	public static final int
	STAT = 1,
	EXPR = 2;
	
	ASTNode parent;
	
	public abstract int type();
	
	void insertNode(ASTNode node)
	{
		throw new UnsupportedOperationException();
	}
}
