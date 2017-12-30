/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public abstract class AbstractASTVisitor
{
	protected void visitChild(ASTNode node)
	{
		switch (node.type())
		{
		case ASTNode.EXPR :
			visitExpression((ASTNodeExpr) node);
			break;
		case ASTNode.STAT :
			visitStatement((ASTNodeStat) node);
			break;
		}
	}
	
	public abstract void visitExpression(ASTNodeExpr node);
	
	public abstract void visitStatement(ASTNodeStat node);
}