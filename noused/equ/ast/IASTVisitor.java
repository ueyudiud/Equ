/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

/**
 * @author ueyudiud
 */
public interface IASTVisitor
{
	void visitTypeNode(TypeNode node);
	
	void visitExprNode(ExprNode node);
}
