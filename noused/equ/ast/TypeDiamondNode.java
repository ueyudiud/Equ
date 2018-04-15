/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

/**
 * @author ueyudiud
 */
public class TypeDiamondNode extends TypeNode
{
	TypeNode parent;
	
	public TypeDiamondNode(TypeNode node)
	{
		this.parent = node;
	}
	
	public TypeNode getParent()
	{
		return this.parent;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of(this.parent);
	}
}
