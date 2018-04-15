/*
 * copyright© 2018 ueyudiud
 */
package equ.ast;

import java.util.List;

import com.google.common.collect.ImmutableList;

import equ.compiler1.Literal.LiteralType;

/**
 * @author ueyudiud
 */
public class ConstNode extends ExprNode
{
	private static final TypeNode INTEGER = new TypeFullNode("equ", "virtural", "AnyInt");
	private static final TypeNode LONG = new TypeFullNode("equ", "virtural", "AnyLong");
	private static final TypeNode FLOAT = new TypeFullNode("equ", "virtural", "AnyFloat");
	private static final TypeNode DOUBLE = new TypeFullNode("equ", "virtural", "AnyDouble");
	private static final TypeNode CHAR = new TypeFullNode("equ", "virtural", "AnyChar");
	private static final TypeNode BOOL = new TypeFullNode("equ", "Bool");
	private static final TypeNode NULL = new TypeFullNode("equ", "Null");
	private static final TypeNode STRING = new TypeFullNode("equ", "String");
	
	Object constant;
	TypeNode node;
	
	public ConstNode(LiteralType type, Object constant)
	{
		this.constant = constant;
		switch (type)
		{
		case BOOL :
			this.node = BOOL;
			break;
		case NULL :
			this.node = NULL;
			break;
		case CHAR :
			this.node = CHAR;
			break;
		case INT :
			this.node = INTEGER;
			break;
		case LONG :
			this.node = LONG;
			break;
		case FLOAT :
			this.node = FLOAT;
			break;
		case DOUBLE :
			this.node = DOUBLE;
			break;
		case STRING :
			this.node = STRING;
			break;
		default :
			throw new InternalError();
		}
	}
	
	public Object getConstant()
	{
		return this.constant;
	}
	
	@Override
	public TypeNode type()
	{
		return this.node;
	}
	
	@Override
	public List<? extends ASTNode> children()
	{
		return ImmutableList.of();
	}
}
