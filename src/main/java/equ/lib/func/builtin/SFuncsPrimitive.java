/*
 * copyright© 2017 ueyudiud
 */
package equ.lib.func.builtin;

import static equ.lib.type.STypes.BYTE;
import static equ.lib.type.STypes.CHAR;
import static equ.lib.type.STypes.DOUBLE;
import static equ.lib.type.STypes.FLOAT;
import static equ.lib.type.STypes.INT;
import static equ.lib.type.STypes.LONG;
import static equ.lib.type.STypes.SHORT;
import static org.objectweb.asm.Opcodes.D2F;
import static org.objectweb.asm.Opcodes.D2I;
import static org.objectweb.asm.Opcodes.D2L;
import static org.objectweb.asm.Opcodes.DADD;
import static org.objectweb.asm.Opcodes.DDIV;
import static org.objectweb.asm.Opcodes.DMUL;
import static org.objectweb.asm.Opcodes.DREM;
import static org.objectweb.asm.Opcodes.DSUB;
import static org.objectweb.asm.Opcodes.F2D;
import static org.objectweb.asm.Opcodes.F2I;
import static org.objectweb.asm.Opcodes.F2L;
import static org.objectweb.asm.Opcodes.FADD;
import static org.objectweb.asm.Opcodes.FDIV;
import static org.objectweb.asm.Opcodes.FMUL;
import static org.objectweb.asm.Opcodes.FREM;
import static org.objectweb.asm.Opcodes.FSUB;
import static org.objectweb.asm.Opcodes.I2D;
import static org.objectweb.asm.Opcodes.I2F;
import static org.objectweb.asm.Opcodes.I2L;
import static org.objectweb.asm.Opcodes.IADD;
import static org.objectweb.asm.Opcodes.IAND;
import static org.objectweb.asm.Opcodes.IDIV;
import static org.objectweb.asm.Opcodes.IMUL;
import static org.objectweb.asm.Opcodes.IOR;
import static org.objectweb.asm.Opcodes.IREM;
import static org.objectweb.asm.Opcodes.ISHL;
import static org.objectweb.asm.Opcodes.ISHR;
import static org.objectweb.asm.Opcodes.ISUB;
import static org.objectweb.asm.Opcodes.IUSHR;
import static org.objectweb.asm.Opcodes.IXOR;
import static org.objectweb.asm.Opcodes.L2D;
import static org.objectweb.asm.Opcodes.L2F;
import static org.objectweb.asm.Opcodes.L2I;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LAND;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LOR;
import static org.objectweb.asm.Opcodes.LREM;
import static org.objectweb.asm.Opcodes.LSHL;
import static org.objectweb.asm.Opcodes.LSHR;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.LUSHR;
import static org.objectweb.asm.Opcodes.LXOR;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import equ.lib.base.SCodeWriter;
import equ.lib.base.SMethodWriter;
import equ.lib.func.SFunc;
import equ.lib.type.SClass;
import equ.lib.type.SType;
import equ.lib.type.STypes;

/**
 * @author ueyudiud
 */
public final class SFuncsPrimitive
{
	private static final SFunc[][][] FUNCS1;
	private static final SFunc[][][] FUNCS2;
	
	private static final SFunc[] FUNCS_TOSTRING;
	private static final SFunc[] FUNCS_HASHCODE;
	
	public static List<?> ofInteger(int i)
	{
		List<Object> list = new ArrayList<>();
		for (SFunc[][] funcs : FUNCS1)
		{
			list.add(funcs[i]);
		}
		for (SFunc[][] funcs : FUNCS2)
		{
			list.add(funcs[i]);
		}
		list.add(FUNCS_TOSTRING[i]);
		list.add(FUNCS_HASHCODE[i]);
		return list;
	}
	
	public static List<?> ofFloat(int i)
	{
		List<Object> list = new ArrayList<>();
		for (SFunc[][] funcs : FUNCS1)
		{
			list.add(funcs[i]);
		}
		list.add(FUNCS_TOSTRING[i]);
		list.add(FUNCS_HASHCODE[i]);
		return list;
	}
	
	public static List<?> ofBool(int i)
	{
		List<Object> list = new ArrayList<>();
		list.add(FUNCS_TOSTRING[i]);
		list.add(FUNCS_HASHCODE[i]);
		return list;
	}
	
	static class Writer1 implements SMethodWriter
	{
		private final int cast1, cast2, opcode;
		
		Writer1(int cast1, int cast2, int opcode)
		{
			this.cast1 = cast1;
			this.cast2 = cast2;
			this.opcode = opcode;
		}
		
		public void write(boolean needResult, SCodeWriter writer, Consumer<SCodeWriter>...parameters)
		{
			assert needResult && parameters.length == 2;
			parameters[0].accept(writer);
			if (this.cast1 >= 0)
				writer.visitor().visitInsn(this.cast1);
			parameters[1].accept(writer);
			if (this.cast2 >= 0)
				writer.visitor().visitInsn(this.cast2);
			
			writer.visitor().visitInsn(this.opcode);
		}
	}
	
	static class WriterToString implements SMethodWriter
	{
		private final String type;
		private final String desc;
		
		WriterToString(String type, String desc)
		{
			this.type = type;
			this.desc = "(" + desc + ")V";
		}
		
		public void write(boolean needResult, SCodeWriter writer, Consumer<SCodeWriter>...parameters)
		{
			assert needResult && parameters.length == 1;
			parameters[0].accept(writer);
			
			writer.visitor().visitMethodInsn(INVOKESTATIC, this.type, "toString", this.desc, false);
		}
	}
	
	static class WriterHashCode implements SMethodWriter
	{
		private final String type;
		private final String desc;
		
		WriterHashCode(String type, String desc)
		{
			this.type = type;
			this.desc = "(" + desc + ")I";
		}
		
		public void write(boolean needResult, SCodeWriter writer, Consumer<SCodeWriter>...parameters)
		{
			assert needResult && parameters.length == 1;
			parameters[0].accept(writer);
			
			writer.visitor().visitMethodInsn(INVOKESTATIC, this.type, "hashCode", this.desc, false);
		}
	}
	
	static
	{
		SType[] types1 = {BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE};
		int[] levels1 = {0, 0, 0, 0, 1, 2, 3};
		SType[] levelfuncs1 = {INT, LONG, FLOAT, DOUBLE};
		
		final int[][] ts = {
				{-1, I2L, I2F, I2D},
				{-1, I2L, I2F, I2D},
				{-1, I2L, I2F, I2D},
				{-1, I2L, I2F, I2D},
				{L2I, -1, L2F, L2D},
				{F2I, F2L, -1, F2D},
				{D2I, D2L, D2F, -1}
		};
		
		int l1 = types1.length;
		final int[][] codes1 = {
				{IADD, LADD, FADD, DADD},
				{ISUB, LSUB, FSUB, DSUB},
				{IMUL, LMUL, FMUL, DMUL},
				{IDIV, LDIV, FDIV, DDIV},
				{IREM, LREM, FREM, DREM}
		};
		
		FUNCS1 = new SFunc[codes1.length][l1][l1];
		
		for (int i = 0; i < l1; ++i)
		{
			final int r0 = levels1[i];
			for (int j = 0; j < l1; ++j)
			{
				int r = Math.max(r0, levels1[j]);
				FUNCS1[0][i][j] = new SFuncBuiltIn1("+", new Writer1(ts[i][r], ts[j][r], codes1[0][r]), types1[i], levelfuncs1[r], types1[j]);
				FUNCS1[1][i][j] = new SFuncBuiltIn1("-", new Writer1(ts[i][r], ts[j][r], codes1[1][r]), types1[i], levelfuncs1[r], types1[j]);
				FUNCS1[2][i][j] = new SFuncBuiltIn1("*", new Writer1(ts[i][r], ts[j][r], codes1[2][r]), types1[i], levelfuncs1[r], types1[j]);
				FUNCS1[3][i][j] = new SFuncBuiltIn1("/", new Writer1(ts[i][r], ts[j][r], codes1[3][r]), types1[i], levelfuncs1[r], types1[j]);
				FUNCS1[4][i][j] = new SFuncBuiltIn1("%", new Writer1(ts[i][r], ts[j][r], codes1[4][r]), types1[i], levelfuncs1[r], types1[j]);
			}
		}
		
		SType[] types2 = {BYTE, SHORT, CHAR, INT, LONG};
		int[] levels2 = {0, 0, 0, 0, 1};
		SType[] levelfuncs2 = {INT, LONG};
		
		int l2 = types2.length;
		final int[][] codes2 = {
				{ISHL, LSHL},
				{ISHR, LSHR},
				{IUSHR, LUSHR},
				{IOR, LOR},
				{IAND, LAND},
				{IXOR, LXOR}
		};
		
		FUNCS2 = new SFunc[codes2.length][l2][l2];
		
		for (int i = 0; i < l2; ++i)
		{
			final int r0 = levels2[i];
			for (int j = 0; j < l2; ++j)
			{
				int r = Math.max(r0, levels2[j]);
				FUNCS2[0][i][j] = new SFuncBuiltIn1("<<" , new Writer1(ts[i][r], ts[j][r], codes2[0][r]), types2[i], levelfuncs2[r], types2[j]);
				FUNCS2[1][i][j] = new SFuncBuiltIn1(">>" , new Writer1(ts[i][r], ts[j][r], codes2[1][r]), types2[i], levelfuncs2[r], types2[j]);
				FUNCS2[2][i][j] = new SFuncBuiltIn1(">>>", new Writer1(ts[i][r], ts[j][r], codes2[2][r]), types2[i], levelfuncs2[r], types2[j]);
				FUNCS2[3][i][j] = new SFuncBuiltIn1("|"  , new Writer1(ts[i][r], ts[j][r], codes2[3][r]), types2[i], levelfuncs2[r], types2[j]);
				FUNCS2[4][i][j] = new SFuncBuiltIn1("&"  , new Writer1(ts[i][r], ts[j][r], codes2[4][r]), types2[i], levelfuncs2[r], types2[j]);
				FUNCS2[5][i][j] = new SFuncBuiltIn1("^"  , new Writer1(ts[i][r], ts[j][r], codes2[4][r]), types2[i], levelfuncs2[r], types2[j]);
			}
		}
		
		final SClass[] types11 = {BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE};
		int l11 = types11.length;
		FUNCS_TOSTRING = new SFunc[l11];
		FUNCS_HASHCODE = new SFunc[l11];
		for (int i = 0; i < l11; ++i)
		{
			SClass class1 = types11[i];
			String owner = STypes.getBoxedInternalName(class1);
			FUNCS_TOSTRING[i] = new SFuncBuiltIn1("toString", new WriterToString(owner, class1.getDescriptor()), class1, STypes.STRING);
			FUNCS_HASHCODE[i] = new SFuncBuiltIn1("hashCode", new WriterHashCode(owner, class1.getDescriptor()), class1, STypes.INT);
		}
	}
}
