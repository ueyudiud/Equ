/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeFullNode extends TypeNode
{
	String[] fullName;
	
	public TypeFullNode(String...name)
	{
		this.fullName = name;
	}
	
	public String[] getName()
	{
		return this.fullName;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of();
	}
}
