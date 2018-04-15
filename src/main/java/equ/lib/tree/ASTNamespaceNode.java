/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTNamespaceNode extends ASTNode
{
	public String name;
	
	public ASTAttributeNode[] attriutes = AST.NO_ATTRIBUTE;
	
	public ASTImportNode[]		imports = AST.NO_IMPORT;
	
	public ASTFieldNode[]		fields	= AST.NO_FIELD;
	public ASTMethodNode[]		methods	= AST.NO_METHOD;
	public ASTClassNode[]		classes	= AST.NO_CLASS;
}
