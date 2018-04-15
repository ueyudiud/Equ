/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeReflectionNode extends TypeNode
{
	TypeNode parent;
	String reflect;
	
	public TypeReflectionNode(TypeNode parent, String reflect)
	{
		this.parent = parent;
		this.reflect = reflect;
	}
	
	public TypeNode getParent()
	{
		return this.parent;
	}
	
	public String getReflect()
	{
		return this.reflect;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of(this.parent);
	}
}
