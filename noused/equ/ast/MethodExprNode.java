/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class MethodExprNode extends ExprNode
{
	ExprNode parent;
	NestExprNode parameters;
	
	public MethodExprNode(ExprNode parent, NestExprNode paramters)
	{
		this.parent = parent;
		this.parameters = paramters;
	}
	
	public ExprNode getParent()
	{
		return this.parent;
	}
	
	public NestExprNode getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public TypeNode type()
	{
		return null;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of(this.parameters, this.parent);
	}
}
