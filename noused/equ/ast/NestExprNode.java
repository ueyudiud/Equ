/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class NestExprNode extends ExprNode
{
	boolean right;
	boolean left;
	FullExprNode child;
	
	public NestExprNode(FullExprNode child, boolean left, boolean right)
	{
		this.child = child;
		this.left = left;
		this.right = right;
	}
	
	public boolean isLeft()
	{
		return this.left;
	}
	
	public boolean isRight()
	{
		return this.right;
	}
	
	public FullExprNode getChild()
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
		return ImmutableList.of(this.child);
	}
}
