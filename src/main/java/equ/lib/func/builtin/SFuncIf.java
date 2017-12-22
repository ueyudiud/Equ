/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func.builtin;

import static org.objectweb.asm.Opcodes.IFEQ;

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
class SFuncIf implements SFuncStruct
{
	private final SType[] parameters = {STypes.BOOL, STypes.BLOCK};
	private final STypeMethod type = SFuncs.toMethodType(null, new SType[][] {{STypes.BOOL}, {STypes.BLOCK}}, STypes.UNIT, new SVariableType[0]);
	private final SMethodWriter writer = (needResult, writer, parameters) -> {
		Arguments.checkLength(2, parameters, "Illegal parameter count.");
		
		writer.pushLabel();
		Label label = new Label();
		if (parameters[0] instanceof ILabelWritable)
			((ILabelWritable) parameters[0]).write(null, label, writer);
		else
		{
			parameters[0].accept(writer);
			writer.visitJumpInsn(IFEQ, label);
		}
		writer.pushLabel();
		parameters[1].accept(writer);
		writer.pushLabel();
		writer.visitLabel(label);
	};
	
	@Override
	public String getName()
	{
		return "if";
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
