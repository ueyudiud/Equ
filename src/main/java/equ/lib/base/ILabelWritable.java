/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.base;

import org.objectweb.asm.Label;

/**
 * @author ueyudiud
 */
public interface ILabelWritable
{
	void write(Label iftrue, Label iffalse, SCodeWriter writer);
}
