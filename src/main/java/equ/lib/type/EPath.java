/*
 * copyright© 2018 ueyudiud
 */
package equ.lib.type;

/**
 * @author ueyudiud
 */
public interface EPath
{
	EPath parentPath();
	
	String name();
	
	@Override
	String toString();
}
