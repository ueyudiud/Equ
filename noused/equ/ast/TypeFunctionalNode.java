/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeFunctionalNode extends TypeNode
{
	TypeNode[] parameters;
	TypeNode result;
	
	public TypeFunctionalNode(TypeNode result, TypeNode...parameters)
	{
		this.parameters = parameters;
		this.result = result;
	}
	
	public TypeNode[] getParameters()
	{
		return this.parameters;
	}
	
	public TypeNode getResult()
	{
		return this.result;
	}
	
	@Override
	public List<TypeNode> children()
	{
		if (this.children == null)
		{
			ImmutableList.Builder<TypeNode> builder = ImmutableList.builder();
			builder.add(this.parameters);
			builder.add(this.result);
			this.children = builder.build();
		}
		return (List<TypeNode>) this.children;
	}
}
