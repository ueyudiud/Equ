/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

/**
 * @author ueyudiud
 */
public abstract class AbstractASTVisitor implements IASTVisitor
{
	protected void visitChild(ASTNode node)
	{
		if (node instanceof TypeNode)
		{
			visitTypeNode((TypeNode) node);
		}
	}
}
