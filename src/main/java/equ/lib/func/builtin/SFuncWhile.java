/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func.builtin;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFNE;

import org.objectweb.asm.Label;

import equ.lib.base.ILabelWritable;
import equ.lib.base.SMethodWriter;
import equ.lib.func.SFuncs;
import equ.lib.func.STypeMethod;
import equ.lib.type.SType;
import equ.lib.type.STypes;
import equ.lib.type.SVariableType;
import equ.util.Arguments;

/**
 * @author ueyudiud
 */
class SFuncWhile implements SFuncStruct
{
	private final SType[] parameters = {STypes.BOOL, STypes.BLOCK};
	private final STypeMethod type = SFuncs.toMethodType(null, new SType[][] {{STypes.BOOL}, {STypes.BLOCK}}, STypes.UNIT, new SVariableType[0]);
	private final SMethodWriter writer = (needResult, writer, parameters) -> {
		Arguments.checkLength(2, parameters, "Illegal parameter count.");
		
		Label label1 = new Label(), label2 = new Label();
		
		writer.pushLabel();
		writer.visitLabel(label1);
		if (parameters[0] instanceof ILabelWritable)
			((ILabelWritable) parameters[0]).write(null, label2, writer);
		else
		{
			parameters[0].accept(writer);
			writer.visitJumpInsn(IFNE, label2);
		}
		writer.pushLabel();
		parameters[1].accept(writer);
		writer.visitJumpInsn(GOTO, label1);
		writer.pushLabel();
		writer.visitLabel(label2);
	};
	
	@Override
	public String getName()
	{
		return "while";
	}
	
	@Override
	public STypeMethod getMethodType()
	{
		return this.type;
	}
	
	@Override
	public SType[] getParameters()
	{
		return this.parameters;
	}
	
	@Override
	public SType getResult()
	{
		return STypes.UNIT;
	}
	
	@Override
	public SMethodWriter writer()
	{
		return this.writer;
	}
}
