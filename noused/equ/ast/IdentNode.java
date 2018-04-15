/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class IdentNode extends ExprNode
{
	String name;
	TypeNode[] parameters;
	
	public IdentNode(String name, TypeNode[] parameters)
	{
		this.name = name;
		this.parameters = parameters;
	}
	
	@Override
	public TypeNode type()
	{
		return null;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public TypeNode[] getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.copyOf(this.parameters);
	}
}
