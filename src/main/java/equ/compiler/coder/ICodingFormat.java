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
	TO = 64,//source[], to
	FUNC = 65,//
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
	TYPE_CALLER = 76;//source
}
