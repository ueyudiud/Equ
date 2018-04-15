/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import equ.util.Numbers;

/**
 * @author ueyudiud
 */
public class MacroEvaluator
{
	private final CompileContext context;
	private SourceTrace header;
	
	enum Operator
	{
		NOT(Integer.MAX_VALUE),
		INV(Integer.MAX_VALUE),
		NEG(Integer.MAX_VALUE),
		MUL(14),
		DIV(14),
		REM(14),
		ADD(13),
		SUB(13),
		SHL(12),
		SHR(12),
		USHR(12),
		IXOR(11),
		IAND(10),
		IOR(9),
		GT(8),
		GE(8),
		LT(8),
		LE(8),
		EQ(7),
		NE(7),
		XOR(6),
		AND(5),
		OR(4),
		DEF(3),
		COM(2),
		FUN(1),
		LPA(1);
		
		final int lv;
		
		Operator(int lv)
		{
			this.lv = lv;
		}
	}
	
	public MacroEvaluator(CompileContext context)
	{
		this.context = context;
	}
	
	public ILexeme eval(SourceTrace evalCaller, List<ILexeme> values)
	{
		header = evalCaller;
		try
		{
			LinkedList<ILexeme> left = new LinkedList<>();
			LinkedList<ILexeme> right = new LinkedList<>();
			Iterator<ILexeme> itr = values.iterator();
			while (itr.hasNext())
			{
				ILexeme l = itr.next();
				switch (l.type())
				{
				case LIN: case LIB: case LIC:
				case LIF: case LII: case LIS:
					left.addLast(l);
					break;
				case OP :
					reduceOperator(parseBinaryOperator(l).lv, left, right);
					right.addFirst(l);
					break;
				case ID :
					IMacro macro = context.get(l.display());
					if (macro.parameterCount() == -1)
					{
						left.add(eval(l.trace(), macro.transform(l)));
					}
					else
					{
						right.addFirst(l);
					}
					break;
				case LPA :
					if (right.getFirst().type() == EnumLexType.ID)
					{
						ILexeme l2 = right.removeFirst();
						macro = context.get(l2.display());
						List<List<ILexeme>> lexemess = new ArrayList();
						label: while (true)
						{
							LinkedList<ILexeme> lexemes = new LinkedList<>();
							lexemess.add(lexemes);
							l = stopOnComma(itr, lexemes);
							if (l == null)
								throw new CompileException("Fail to evaluate macro expression.");
							switch (l.type())
							{
							case RPA:
								break label;
							default:
							case COM:
								continue;
							}
						}
						if (macro.parameterCount() != lexemess.size())
							throw new CompileException("Fail to evaluate macro expression.");
						left.add(eval(l2.trace(), macro.transform(l2, lexemess.toArray(new List[lexemess.size()]))));
					}
					else
					{
						right.addFirst(l);
					}
					break;
				case RPA:
					reduceOperator(Operator.LPA.lv + 1, left, right);
					if (right.isEmpty() || right.getFirst().type() != EnumLexType.LPA)
						throw new CompileException("Fail to evaluate macro expression.");
					right.removeFirst();
					break;
				default:
					throw new CompileException("Fail to evaluate macro expression.");
				}
			}
			reduceOperator(0, left, right);
			if (left.size() != 1 || !right.isEmpty())
			{
				throw new CompileException("Illegal macro expression.");
			}
			return left.getFirst();
		}
		finally
		{
			header = null;
		}
	}
	
	private ILexeme stopOnComma(Iterator<ILexeme> source, List<ILexeme> store)
	{
		while (source.hasNext())
		{
			ILexeme lexeme = source.next();
			switch (lexeme.type())
			{
			case COM:
			case RPA:
				return lexeme;
			case LPA:
				lexeme = stopOnPanc(source, store);
				if (lexeme == null)
					throw new CompileException("Illegal nested in macro.");
				break;
			default:
				store.add(lexeme);
				break;
			}
		}
		return null;
	}
	
	private ILexeme stopOnPanc(Iterator<ILexeme> source, List<ILexeme> store)
	{
		while (source.hasNext())
		{
			ILexeme lexeme = source.next();
			switch (lexeme.type())
			{
			case RPA:
				return lexeme;
			case LPA:
				lexeme = stopOnPanc(source, store);
				if (lexeme == null)
					throw new CompileException("Illegal nested in macro.");
				break;
			default:
				store.add(lexeme);
				break;
			}
		}
		return null;
	}
	
	private Operator parseBinaryOperator(ILexeme lexeme)
	{
		switch (lexeme.type())
		{
		case ID:
			return Operator.FUN;
		case LPA:
			return Operator.LPA;
		default:
		}
		assert lexeme.type() == EnumLexType.OP;
		switch (lexeme.display())
		{
		case "*": return Operator.MUL;
		case "/": return Operator.DIV;
		case "%": return Operator.REM;
		case "+": return Operator.ADD;
		case "-": return Operator.SUB;
		case "<<": return Operator.SHL;
		case ">>": return Operator.SHR;
		case ">>>": return Operator.USHR;
		case "^": return Operator.IXOR;
		case "&": return Operator.IAND;
		case "|": return Operator.IOR;
		case ">": return Operator.GT;
		case ">=": return Operator.GE;
		case "<": return Operator.LT;
		case "<=": return Operator.LE;
		case "==": return Operator.EQ;
		case "!=": return Operator.NE;
		case "^^": return Operator.XOR;
		case "&&": return Operator.AND;
		case "||": return Operator.OR;
		case "=": return Operator.DEF;
		default : throw new CompileException("Unknown operator.");
		}
	}
	
	private void reduceOperator(int acclv, LinkedList<ILexeme> left, LinkedList<ILexeme> right)
	{
		while (!right.isEmpty())
		{
			ILexeme l = right.getFirst();
			Operator o = parseBinaryOperator(l);
			if (o.lv >= acclv)
			{
				right.removeFirst();
				switch (o)
				{
				case MUL:
					left.addLast(mul(l, left.removeLast(), left.removeLast()));
					break;
				case DIV:
					left.addLast(div(l, left.removeLast(), left.removeLast()));
					break;
				case REM:
					left.addLast(rem(l, left.removeLast(), left.removeLast()));
					break;
				case ADD:
					left.addLast(add(l, left.removeLast(), left.removeLast()));
					break;
				case SUB:
					left.addLast(sub(l, left.removeLast(), left.removeLast()));
					break;
				case GT:
					left.addLast(gt(l, left.removeLast(), left.removeLast()));
					break;
				case GE:
					left.addLast(ge(l, left.removeLast(), left.removeLast()));
					break;
				case LT:
					left.addLast(lt(l, left.removeLast(), left.removeLast()));
					break;
				case LE:
					left.addLast(le(l, left.removeLast(), left.removeLast()));
					break;
				default:
					//TODO
					break;
				}
			}
			else return;
		}
	}
	
	private ILexeme mul(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeFloat(Numbers.fromString(Double.toHexString(left.doubleValue() * right.doubleValue()), Numbers.MODE_HEX),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeInt(Numbers.fromInteger(left.longValue() * right.longValue()),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme div(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeFloat(Numbers.fromString(Double.toHexString(left.doubleValue() / right.doubleValue()), Numbers.MODE_HEX),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeInt(Numbers.fromInteger(left.longValue() / right.longValue()),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme rem(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeFloat(Numbers.fromString(Double.toHexString(left.doubleValue() % right.doubleValue()), Numbers.MODE_HEX),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeInt(Numbers.fromInteger(left.longValue() % right.longValue()),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme add(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left) || !isNumber(right))
		{
			String s1 = left.stringValue(), s2 = right.stringValue();
			char[] connected = new char[s1.length() + s2.length()];
			s1.getChars(0, s1.length(), connected, 0);
			s2.getChars(0, s2.length(), connected, s1.length());
			return new LexemeString(connected,
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeFloat(Numbers.fromString(Double.toHexString(left.doubleValue() + right.doubleValue()), Numbers.MODE_HEX),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeInt(Numbers.fromInteger(left.longValue() + right.longValue()),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme sub(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeFloat(Numbers.fromString(Double.toHexString(left.doubleValue() - right.doubleValue()), Numbers.MODE_HEX),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeInt(Numbers.fromInteger(left.longValue() - right.longValue()),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme gt(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeBool(left.doubleValue() > right.doubleValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeBool(left.longValue() > right.longValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme ge(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeBool(left.doubleValue() >= right.doubleValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeBool(left.longValue() >= right.longValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme lt(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeBool(left.doubleValue() < right.doubleValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeBool(left.longValue() < right.longValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private ILexeme le(ILexeme operator, ILexeme right, ILexeme left)
	{
		if (!isNumber(left))
			throw new CompileException("Can not cast " + left + " to a number.");
		if (!isNumber(right))
			throw new CompileException("Can not cast " + right + " to a number.");
		if (left.type() == EnumLexType.LIF || right.type() == EnumLexType.LIF)
		{
			return new LexemeBool(left.doubleValue() <= right.doubleValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
		else
		{
			return new LexemeBool(left.longValue() <= right.longValue(),
					new SourceTrace(operator.trace(), left.trace(), right.trace()));
		}
	}
	
	private boolean isNumber(ILexeme l)
	{
		return l.type() == EnumLexType.LII || l.type() == EnumLexType.LIF;
	}
}
