/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import equ.ast.ExprNode;
import equ.ast.TypeNode;

/**
 * @author ueyudiud
 */
public interface IParser
{
	TypeNode parseType();
	
	ExprNode parseExpression();
}
