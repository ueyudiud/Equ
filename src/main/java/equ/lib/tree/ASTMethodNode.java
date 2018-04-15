/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTMethodNode extends ASTNode
{
	public String name;
	public ASTTypeVariableNode[] typeVariables = AST.NO_TYPE_VARIABLE;
	public ASTTypeNode descriptor;
	
	public ASTLocalVariableNode[] variables = AST.NO_VARIABLE;
	
	public ASTAttributeNode[]	attributes		= AST.NO_ATTRIBUTE;
	public ASTFieldNode[]		fields			= AST.NO_FIELD;
	public ASTMethodNode[]		internalMethods = AST.NO_METHOD;
	public ASTClassNode[]		internalClasses = AST.NO_CLASS;
}
