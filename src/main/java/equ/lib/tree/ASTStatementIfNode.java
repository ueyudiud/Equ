/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

/**
 * @author ueyudiud
 */
public class ASTStatementIfNode extends ASTStatementNode
{
	public String label;
	
	public ASTStatementNode predicate;
	public ASTCodeNode code1;
	public ASTCodeNode code2;
}
