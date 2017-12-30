/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ueyudiud
 */
public class ASTNodeStat extends ASTNode
{
	List<ASTNode> expressions = new LinkedList<>();
	
	ASTNodeStat(ASTNode parent)
	{
		this.parent = parent;
	}
	ASTNodeStat()
	{
		
	}
	
	@Override
	public int type()
	{
		return STAT;
	}
	
	@Override
	void insertNode(ASTNode node)
	{
		this.expressions.add(node);
	}
}
