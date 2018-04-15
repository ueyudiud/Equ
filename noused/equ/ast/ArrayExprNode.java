/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

/**
 * @author ueyudiud
 */
public class ArrayExprNode extends NestExprNode
{
	public ArrayExprNode(FullExprNode child)
	{
		super(child, true, true);
	}
}
