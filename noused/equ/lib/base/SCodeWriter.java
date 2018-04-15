/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.base;

import javax.annotation.Nonnull;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

/**
 * @author ueyudiud
 */
public interface SCodeWriter
{
	MethodVisitor visitor();
	
	void visitLabel(Label label);
	
	void visitJumpInsn(int opcode, Label label);
	
	@Nonnull Label currentLabel();
	
	void pushLabel();
}
