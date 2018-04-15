/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.scanner;

/**
 * @author ueyudiud
 */
public interface SpecialCodePoint
{
	int
	UNCHECK    = 0,
	SINGLE     = 1,
	DOUBLE     = 2,
	SINGLEONLY = 3;
	
	int
	CP_LEFT_NAMING  	= 0x100001,
	CP_RIGHT_NAMING 	= 0x100002,
	CP_LEFT_TEMPLATE	= 0x100003,
	CP_RIGHT_TEMPLATE	= 0x100004,
	CP_SINGLE_COMMENT	= 0x100005,
	CP_LEFT_MCOMMENT	= 0x100006,
	CP_RIGHT_MCOMMENT	= 0x100007;
}
