/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class OpBinaryNode extends ExprNode
{
	ExprNode left;
	ExprNode right;
	IdentNode operator;
	
	public OpBinaryNode(ExprNode left, ExprNode right, IdentNode operator)
	{
		this.left = left;
		this.right = right;
		this.operator = operator;
	}
	
	public ExprNode getLeft()
	{
		return this.left;
	}
	
	public ExprNode getRight()
	{
		return this.right;
	}
	
	public IdentNode getOperator()
	{
		return this.operator;
	}
	
	@Override
	public TypeNode type()
	{
		return null;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of(this.left, this.right, this.operator);
	}
}
