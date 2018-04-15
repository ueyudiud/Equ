/*
 * copyright© 2017 ueyudiud
 */
package equ.ast;

/**
 * @author ueyudiud
 */
public class TypeTupleNode extends TypeParameterizedNode
{
	public TypeTupleNode(TypeNode[] parameters)
	{
		super(new TypeFullNode("equ", "Tuple" + parameters.length), parameters);
	}
}
