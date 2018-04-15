/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.tree;

import equ.lib.type.EnumVariableBoundType;

/**
 * @author ueyudiud
 */
public class ASTTypeVariableNode extends ASTNode
{
	public String name;
	public EnumVariableBoundType bound_t;
	public ASTTypeNode[] bounds;
}
