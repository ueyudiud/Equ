/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeParameterizedNode extends TypeNode
{
	TypeNode[] parameters;
	TypeNode base;
	
	public TypeParameterizedNode(TypeNode node, TypeNode...parameters)
	{
		this.base = node;
		this.parameters = parameters;
	}
	
	public TypeNode getBase()
	{
		return this.base;
	}
	
	public TypeNode[] getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public List<TypeNode> children()
	{
		if (this.children == null)
		{
			ImmutableList.Builder<TypeNode> builder = ImmutableList.builder();
			builder.add(this.parameters);
			builder.add(this.base);
			this.children = builder.build();
		}
		return (List<TypeNode>) this.children;
	}
}
