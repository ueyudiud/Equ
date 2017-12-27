/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.coder;

/**
 * @author ueyudiud
 */
public interface ICodingFormat
{
	int
	IDENT = 0,
	EXPR = 16,//values[]
	EXPR_BLOCK = 17,//values[], flag1, flag2
	LITERAL = 18,//lit
	BLOCK = 19,//blocks[]
	RETURN = 20,//arguments
	IF = 21,//predicate, block
	IF_ELSE = 22,//predicate, block1, block2
	WHILE = 23,//predicate, block
	DO_WHILE = 24,//predicate, block
	VARIABLE = 25,//ident, type, expr, var/val
	TO = 64,//source[], to
	FUNC = 65,//source, arguments
	TYPE_SIMPLE = 66,//source
	TYPE_REFLECT = 67,//source, reflect
	TYPE_ARRAY = 68,//source
	TYPE_ARRAY_CHECK = 69,//source, matcher
	TYPE_POINTER = 70,//source
	ARRAY_RANGE_MATCHER = 71,//formats...
	TYPE_PARAMETERIZED = 72,//source, parameters[]
	TYPE_TUPLE = 73,//parameters[]
	TYPE_FUNC = 74,//input[], output
	TYPE_UNIT = 75,//
	TYPE_CALLER = 76,//source
	TYPE_EXPR = 77,//source
	TYPE_BRACE = 78,//source, flag1, flag2
	ARGUMENTS = 79,
	PARAMETERIZED = 80,//source, type_parameters
	OPERATOR_UNARY = 81,//source, name
	OPERATOR = 82,//source, name, parameter
	NEW = 83;//type
}
