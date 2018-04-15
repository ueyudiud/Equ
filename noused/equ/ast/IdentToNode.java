/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class IdentToNode extends ExprNode
{
	ExprNode parent;
	IdentNode child;
	
	public IdentToNode(ExprNode parent, IdentNode child)
	{
		this.parent = parent;
		this.child = child;
	}
	
	public ExprNode getParent()
	{
		return this.parent;
	}
	
	public IdentNode getChild()
	{
		return this.child;
	}
	
	@Override
	public TypeNode type()
	{
		return this.child.type();
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of(this.parent, this.child);
	}
}
