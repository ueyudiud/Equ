/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTStatementFunctionNode extends ASTStatementNode
{
	public ASTStatementNode left;
	public ASTTemplateElement[] templates = AST.NO_TEMPLATE_ELEMENT;
	public ASTTypeNode[] typeParameters = AST.NO_TYPE;
	public ASTStatementNode[] right;
}
