/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeWildcardNode extends TypeNode
{
	TypeNode[] upper;
	TypeNode lower;
	
	public TypeWildcardNode(TypeNode lower, TypeNode...upper)
	{
		this.upper = upper;
		this.lower = lower;
	}
	
	public TypeNode[] getUpper()
	{
		return this.upper;
	}
	
	public TypeNode getLower()
	{
		return this.lower;
	}
	
	@Override
	public List<TypeNode> children()
	{
		if (this.children == null)
		{
			ImmutableList.Builder<TypeNode> builder = ImmutableList.builder();
			builder.add(this.upper);
			builder.add(this.lower);
			this.children = builder.build();
		}
		return (List<TypeNode>) this.children;
	}
}
