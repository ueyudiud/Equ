/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
interface AST
{
	ASTAttributeNode[] NO_ATTRIBUTE = new ASTAttributeNode[0];
	ASTClassNode[] NO_CLASS = new ASTClassNode[0];
	ASTMethodNode[] NO_METHOD = new ASTMethodNode[0];
	ASTFieldNode[] NO_FIELD = new ASTFieldNode[0];
	ASTLocalVariableNode[] NO_VARIABLE = new ASTLocalVariableNode[0];
	ASTTypeNode[] NO_TYPE = new ASTTypeNode[0];
	ASTTypeVariableNode[] NO_TYPE_VARIABLE = new ASTTypeVariableNode[0];
	ASTImportNode[] NO_IMPORT = new ASTImportNode[0];
	ASTTemplateElement[] NO_TEMPLATE_ELEMENT = new ASTTemplateElement[0];
}
