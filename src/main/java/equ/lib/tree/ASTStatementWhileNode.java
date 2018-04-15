/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTStatementWhileNode extends ASTStatementNode
{
	public String label;
	
	public ASTStatementNode predicate;
	public ASTCodeNode code;
	public ASTStatementNode result;
}
