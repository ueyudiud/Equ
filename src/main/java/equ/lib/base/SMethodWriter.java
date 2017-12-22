/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.base;

import java.util.function.Consumer;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * @author ueyudiud
 */
public interface SMethodWriter extends Opcodes
{
	void write(boolean needResult, SCodeWriter writer, Consumer<SCodeWriter>...parameters);
	
	default void write(Label iftrue, Label iffalse, SCodeWriter writer, Consumer<SCodeWriter>...parameters)
	{
		assert iftrue != null || iffalse != null;
		write(true, writer, parameters);
		if (iffalse != null)
		{
			writer.visitJumpInsn(IFEQ, iffalse);
			if (iftrue != null)
				writer.visitJumpInsn(GOTO, iftrue);
		}
		else
		{
			writer.visitJumpInsn(IFNE, iftrue);
		}
	}
}
