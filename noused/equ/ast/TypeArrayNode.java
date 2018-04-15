/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeArrayNode extends TypeNode
{
	TypeNode parent;
	int[][] requirements;
	
	public TypeArrayNode(TypeNode node, int[][] requirements)
	{
		this.parent = node;
		this.requirements = requirements;
	}
	
	public TypeNode getParent()
	{
		return this.parent;
	}
	
	public int[][] getRequirements()
	{
		return this.requirements;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of(this.parent);
	}
}
