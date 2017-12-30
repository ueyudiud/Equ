/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class AST
{
	private final ASTNode root;
	private ASTNode node;
	
	private AST(ASTNode node)
	{
		this.root = this.node = node;
	}
	
	public void endStat()
	{
		assert this.node instanceof ASTNodeStat;
		visitParent();
	}
	
	public void endExpression()
	{
		assert this.node instanceof ASTNodeExpr;
		visitParent();
	}
	
	public void startStat()
	{
		assert this.node != null;
		ASTNodeStat node = new ASTNodeStat(this.node);
		this.node.insertNode(node);
		this.node = node;
	}
	
	public void startExpression(ASTNode...elements)
	{
		assert this.node != null;
		ASTNodeExpr node = new ASTNodeExpr(this.node, elements);
		this.node.insertNode(node);
		this.node = node;
	}
	
	private void visitParent()
	{
		this.node = this.node.parent;
	}
}
