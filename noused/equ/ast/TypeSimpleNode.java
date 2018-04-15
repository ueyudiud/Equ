/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeSimpleNode extends TypeNode
{
	String type;
	
	public TypeSimpleNode(String type)
	{
		this.type = type;
	}
	
	public String getName()
	{
		return this.type;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of();
	}
}
