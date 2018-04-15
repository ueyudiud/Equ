/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTFieldNode extends ASTNode
{
	public String name;
	public ASTTypeNode descriptor;
	
	public ASTAttributeNode[]	attributes		= AST.NO_ATTRIBUTE;
	
	public ASTStatementNode initialization;
}
