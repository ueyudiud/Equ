/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

/**
 * @author ueyudiud
 */
public class FullExprNode extends ExprNode
{
	public FullExprNode(List<ExprNode> children)
	{
		this.children = children;
	}
	
	@Override
	public TypeNode type()
	{
		return null;
	}
	
	@Override
	public List<? extends ExprNode> children()
	{
		return (List<? extends ExprNode>) this.children;
	}
}
