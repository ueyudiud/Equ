/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTStatementBinaryOperatorNode extends ASTStatementNode
{
	public String operator;
	public ASTTemplateElement[] templates = AST.NO_TEMPLATE_ELEMENT;
	public ASTTypeNode[] typeParameters = AST.NO_TYPE;
	
	public ASTStatementNode left;
	public ASTStatementNode right;
}
