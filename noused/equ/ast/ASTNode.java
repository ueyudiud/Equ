/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

/**
 * @author ueyudiud
 */
public abstract class ASTNode
{
	protected List<? extends ASTNode> children;
	
	public abstract List<? extends ASTNode> children();
}
