/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class OpUnaryNode extends ExprNode
{
	ExprNode right;
	IdentNode operator;
	
	public OpUnaryNode(ExprNode right, IdentNode operator)
	{
		this.right = right;
		this.operator = operator;
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
		return ImmutableList.of(this.right, this.operator);
	}
}
