/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class ASTNodeExpr extends ASTNode
{
	ASTNode[] elements;
	
	ASTNodeExpr(ASTNode parent, ASTNode[] elements)
	{
		this.parent = parent;
		this.elements = elements;
	}
	ASTNodeExpr(ASTNode parent)
	{
		this.parent = parent;
	}
	
	@Override
	public int type()
	{
		return EXPR;
	}
}
