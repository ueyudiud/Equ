/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import equ.compiler.coder.ICodingFormat;
import equ.compiler.util.Literal;
import equ.compiler.util.Literal.LitType;
import equ.compiler.util.Name;
import equ.util.Dictionary;

/**
 * @author ueyudiud
 */
public class Parser implements ICodingFormat
{
	static final int LEVEL_UNARY = -100;
	static final Dictionary<Integer> DEFAULT_CACULATE_LEVEL = new Dictionary<>();
	
	static
	{
		DEFAULT_CACULATE_LEVEL.put("++", LEVEL_UNARY);
		DEFAULT_CACULATE_LEVEL.put("--", LEVEL_UNARY);
		DEFAULT_CACULATE_LEVEL.put("*", 200);
		DEFAULT_CACULATE_LEVEL.put("/", 200);
		DEFAULT_CACULATE_LEVEL.put("%", 200);
		DEFAULT_CACULATE_LEVEL.put("+", 180);
		DEFAULT_CACULATE_LEVEL.put("-", 180);
		DEFAULT_CACULATE_LEVEL.put("<<", 160);
		DEFAULT_CACULATE_LEVEL.put(">>", 160);
		DEFAULT_CACULATE_LEVEL.put(">>>", 160);
		DEFAULT_CACULATE_LEVEL.put("&", 150);
		DEFAULT_CACULATE_LEVEL.put("|", 140);
		DEFAULT_CACULATE_LEVEL.put("^", 130);
		DEFAULT_CACULATE_LEVEL.put("is", 120);
		DEFAULT_CACULATE_LEVEL.put("as", 120);
		DEFAULT_CACULATE_LEVEL.put(">", 100);
		DEFAULT_CACULATE_LEVEL.put("<", 100);
		DEFAULT_CACULATE_LEVEL.put(">=", 100);
		DEFAULT_CACULATE_LEVEL.put("<=", 100);
		DEFAULT_CACULATE_LEVEL.put("==", 80);
		DEFAULT_CACULATE_LEVEL.put("!=", 80);
		DEFAULT_CACULATE_LEVEL.put("&&", 70);
		DEFAULT_CACULATE_LEVEL.put("||", 60);
		DEFAULT_CACULATE_LEVEL.put("^^", 50);
		DEFAULT_CACULATE_LEVEL.put("~", 40);
		DEFAULT_CACULATE_LEVEL.put("<-", 30);
		DEFAULT_CACULATE_LEVEL.put("=", 0);
		DEFAULT_CACULATE_LEVEL.put("*=", 0);
		DEFAULT_CACULATE_LEVEL.put("/=", 0);
		DEFAULT_CACULATE_LEVEL.put("%=", 0);
		DEFAULT_CACULATE_LEVEL.put("+=", 0);
		DEFAULT_CACULATE_LEVEL.put("-=", 0);
		DEFAULT_CACULATE_LEVEL.put("++=", 0);
		DEFAULT_CACULATE_LEVEL.put("--=", 0);
		DEFAULT_CACULATE_LEVEL.put("<<=", 0);
		DEFAULT_CACULATE_LEVEL.put(">>=", 0);
		DEFAULT_CACULATE_LEVEL.put(">>>=", 0);
		DEFAULT_CACULATE_LEVEL.put("&=", 0);
		DEFAULT_CACULATE_LEVEL.put("|=", 0);
		DEFAULT_CACULATE_LEVEL.put("^=", 0);
	}
	
	final IParsingFactory factory;
	IScanner S;
	LexemeTokenType T;
	
	private LinkedList<IScanner> scanners = new LinkedList<>();
	private boolean errored = false;
	private Iterator<Object[]> iterator;
	private Map<String, Replacement> replacements = new HashMap<>();
	private Dictionary<Integer> operatorLevels = new Dictionary<>(DEFAULT_CACULATE_LEVEL);
	private long ifnest = 0;
	private int ifnestidx = 0;
	
	public Parser(IParsingFactory factory, Scanner scanner)
	{
		this.factory = factory;
		this.S = scanner;
		scan();
	}
	
	/**
	 * 语法：
	 * identifier := id
	 */
	private String ident()
	{
		if (this.T.keyword)
			return this.T.name;
		else
			return this.S.ident().name;
	}
	
	private boolean ident(String key)
	{
		return this.T.identifier && key.equals(this.S.ident().name);
	}
	
	/**
	 * 语法：
	 * intValue := int
	 */
	private int litInt()
	{
		if (this.T != LexemeTokenType.LITERAL || this.S.lit().type != LitType.INT)
			err("lit.expected.int");
		return (int) this.S.lit().value;
	}
	
	/**
	 * 语法：
	 * qualident := identifier ['.' identifier]
	 */
	private Object[] identFully()
	{
		Object[] value = {IDENT, ident()};
		scan();
		while (this.T == LexemeTokenType.DOT)
		{
			scan();
			value = new Object[] {TO, value, ident()};
			scan();
		}
		return value;
	}
	
	private static enum ExprState
	{
		S,//Separator
		I,//Identifier
		O;//Operator
	}
	
	private static boolean isRightConnect(String operator)
	{
		return operator.charAt(operator.length() - 1) == '=';
	}
	
	public Object[] blockFile()
	{
		return blockBracket(false);
	}
	
	private Object[] exprOrBlockBracket()
	{
		if (this.T == LexemeTokenType.LBRACKET)
		{
			scan();
			return blockBracket(true);
		}
		else
		{
			return new Object[] {BLOCK, new Object[] {expr()}};
		}
	}
	
	private Object[] blockBracket(boolean bracket)
	{
		List<Object[]> list = new ArrayList<>();
		label: while (true)
		{
			switch (this.T)
			{
			case RBRACE :
			case RNAMING :
			case RPANC :
				err(bracket ? "block.rbracket.missing" : "block.unexpected.token");
				break label;
			case END :
				if (bracket)
					err("block.rbracket.missing");
				break label;
			case RBRACKET :
				if (!bracket)
					err("block.unexpected.token");
				scan();
				break label;
			case LBRACKET :
				scan();
				list.add(blockBracket(true));
				break;
			case RETURN :
				scan();
				list.add(new Object[] {RETURN, expr()[1]});
				break;
			case IF :
			{
				scan();
				if (this.T != LexemeTokenType.LPANC)
					err("block.lbracket.missing");
				scan();
				Object value1 = expr();
				if (this.T != LexemeTokenType.RPANC)
					err("block.rbracket.missing");
				scan();
				Object[] value2 = exprOrBlockBracket();
				if (this.T == LexemeTokenType.ELSE)
				{
					scan();
					list.add(new Object[] {IF_ELSE, value1, value2[1], exprOrBlockBracket()[1]});
				}
				else
				{
					list.add(new Object[] {IF, value1, value2[1]});
				}
				break;
			}
			case WHILE :
			{
				scan();
				if (this.T != LexemeTokenType.LPANC)
					err("block.lbracket.missing");
				Object value1 = expr();
				if (this.T != LexemeTokenType.RPANC)
					err("block.rbracket.missing");
				scan();
				Object[] value2 = exprOrBlockBracket();
				list.add(new Object[] {WHILE, value1, value2[1]});
				break;
			}
			case DO :
			{
				scan();
				Object[] value2 = exprOrBlockBracket();
				if (this.T != LexemeTokenType.WHILE)
					err("block.while.missing");
				scan();
				if (this.T != LexemeTokenType.LPANC)
					err("block.lbracket.missing");
				Object value1 = expr();
				if (this.T != LexemeTokenType.RPANC)
					err("block.lbracket.missing");
				scan();
				list.add(new Object[] {DO_WHILE, value1, value2[1]});
				break;
			}
			case VAR :
			case VAL :
			{
				boolean flag = this.T == LexemeTokenType.VAL;
				scan();
				Object[] type = null;
				if (this.T == LexemeTokenType.OPERATOR && ident(":"))
				{
					scan();
					type = type();
				}
				String ident = ident();
				list.add(new Object[] {VARIABLE, type, ident, expr(), flag});
				break;
			}
			default:
			{
				list.add(expr());
				if (this.T == LexemeTokenType.SEMI)
					scan();
				break;
			}
			}
		}
		return new Object[] {BLOCK, list.toArray()};
	}
	
	public Object[] expr()
	{
		LinkedList<Object[]> result = new LinkedList<>();
		LinkedList<Object[]> lbuf = new LinkedList<>();
		LinkedList<Object[]> rbuf = new LinkedList<>();
		ExprState state = ExprState.S;
		label: while (true)
		{
			switch (this.T)
			{
			case RPANC :
			case RBRACE :
			case RBRACKET :
			case END :
			case SEMI :
				break label;
			case DOT :
			{
				if (lbuf.size() == 0)
					err("expr.unexpected.token");
				else
				{
					scan();
					lbuf.addLast(new Object[] {TO, lbuf.removeLast(), ident()});
				}
				break;
			}
			case COMMA :
			{
				while (!rbuf.isEmpty())
				{
					Object[] values = rbuf.removeFirst();
					if ((int) values[0] == LEVEL_UNARY)
					{
						lbuf.add(new Object[] {OPERATOR_UNARY, values[1], lbuf.removeLast()});
					}
					else
					{
						Object arg = lbuf.removeLast();
						lbuf.addLast(new Object[] {OPERATOR, lbuf.removeLast(), values[1], arg});
					}
				}
				switch (lbuf.size())
				{
				case 0 :
					//TODO
					err("expr.non.argument");
					result.add(new Object[] {LITERAL, new Literal(LitType.NULL, null)});
					break;
				case 1 :
					result.add(lbuf.removeLast());
					break;
				default:
					err("expr.illegal.argument.count");
					break;
				}
				state = ExprState.S;
				break;
			}
			case LITERAL :
			{
				Object[] literal = new Object[] {LITERAL, this.S.lit()};
				if (state == ExprState.I)
				{
					lbuf.addLast(new Object[] {FUNC, new Object[] {TO, literal, "apply"}, new Object[0]});
				}
				else
				{
					lbuf.addLast(literal);
				}
				state = ExprState.I;
				break;
			}
			case IDENTIFIER :
			{
				Object[] identifier = identFully();
				if (state == ExprState.I)
				{
					lbuf.addLast(new Object[] {FUNC, new Object[] {TO, identifier, "apply"}, new Object[0]});
				}
				else
				{
					lbuf.addLast(identifier);
				}
				state = ExprState.I;
				continue;
			}
			case OPERATOR :
			{
				int level;
				if (state == ExprState.I)
				{
					String ident = ident();
					level = this.operatorLevels.getOrDefault(ident, 20);
					if (level != LEVEL_UNARY)
					{
						final boolean flag = isRightConnect(ident);
						while (!rbuf.isEmpty())
						{
							Object[] values = rbuf.getFirst();
							int level1 = (int) values[0];
							if (level1 == LEVEL_UNARY)
							{
								lbuf.add(new Object[] {OPERATOR_UNARY, values[1], lbuf.removeLast()});
								rbuf.pollFirst();
							}
							else if (flag ? level < level1 : level <= level1)
							{
								Object arg = lbuf.removeLast();
								lbuf.addLast(new Object[] {OPERATOR, lbuf.removeLast(), values[1], arg});
								rbuf.pollFirst();
							}
							else break;
						}
						rbuf.addFirst(new Object[] {level, ident});
					}
					else
					{
						lbuf.add(new Object[] {OPERATOR_UNARY, lbuf.removeLast(), "runary_" + ident()});
					}
				}
				else
				{
					String ident = ident();
					rbuf.addFirst(new Object[] {LEVEL_UNARY, "unary_" + ident});
				}
				state = ExprState.O;
				break;
			}
			case NEW :
			{
				scan();
				lbuf.add(new Object[] {NEW, type()});
				state = ExprState.I;
				break;
			}
			case LBRACKET :
			{
				scan();
				if (state == ExprState.I)
				{
					lbuf.addLast(new Object[] {FUNC, lbuf.removeLast(), blockBracket(true)});
				}
				else
				{
					lbuf.add(blockBracket(true));
				}
				state = ExprState.I;
				if (this.T == LexemeTokenType.COMMA)
				{
					break;
				}
				else
				{
					break label;
				}
			}
			case LBRACE :
			case LPANC :
			{
				boolean flag = this.T == LexemeTokenType.LBRACE;
				scan();
				Object[] expr = new Object[] {EXPR_BLOCK, expr()[1], flag, null};
				switch (this.T)
				{
				case RBRACE :
					expr[3] = true;
					break;
				case RPANC :
					expr[3] = false;
					break;
				default :
					err("wrong.bracket.nested");
					break;
				}
				if (state != ExprState.I)
				{
					lbuf.addLast(expr);
				}
				else
				{
					lbuf.addLast(new Object[] {FUNC, lbuf.removeLast(), expr});
				}
				state = ExprState.I;
				break;
			}
			case LNAMING :
			{
				if (state == ExprState.O)
				{
					Object[] expr = rbuf.removeFirst();
					scan();
					rbuf.addFirst(new Object[] {PARAMETERIZED, expr, types()});
				}
				else
				{
					Object[] expr = lbuf.removeFirst();
					scan();
					lbuf.addFirst(new Object[] {PARAMETERIZED, expr, types()});
				}
				if (this.T != LexemeTokenType.RNAMING)
					err("wrong.bracket.nested");
				break;
			}
			default:
			{
				err("expr.unexpected.token");
				break;
			}
			}
			scan();
		}
		while (!rbuf.isEmpty())
		{
			Object[] values = rbuf.removeFirst();
			if ((int) values[0] == LEVEL_UNARY)
			{
				lbuf.add(new Object[] {OPERATOR_UNARY, values[1], lbuf.removeLast()});
			}
			else
			{
				Object arg = lbuf.removeLast();
				lbuf.addLast(new Object[] {OPERATOR, lbuf.removeLast(), values[1], arg});
			}
		}
		switch (lbuf.size())
		{
		case 0 :
			if (result.size() != 0)
			{
				result.add(new Object[] {LITERAL, new Literal(LitType.NULL, null)});
			}
			break;
		case 1 :
			result.add(lbuf.removeLast());
			break;
		default:
			err("expr.illegal.argument.count");
			break;
		}
		return new Object[] {EXPR, result.toArray()};
	}
	
	/**
	 * 语法：
	 * Type := InfixType
	 *      | InfixType '=>' Type
	 *      | '(' [OrCallerType ','] OrCallerType ')' '=>' Type
	 *      | '()' '=>' Type
	 *      | NamelessType
	 */
	public Object[] type()
	{
		Object[] t0 = type3();
		if (ident("=>"))
		{
			scan();
			Object[] t1 = type();
			return new Object[] {TYPE_FUNC, t0, t1};
		}
		return t0;
	}
	
	/**
	 * 语法：
	 * OrCallerType := '=>'? Type
	 */
	private Object[] type4()
	{
		if (ident("=>"))
		{
			scan();
			return new Object[] {TYPE_CALLER, type()};
		}
		return type();
	}
	
	/**
	 * 语法：
	 * NonFuncType := '(' Types? ')'
	 *           | NonFuncType [InfixTypeName NonFuncType]
	 */
	private Object[] type3()
	{
		if (this.T == LexemeTokenType.LPANC)
		{
			scan();
			if (this.T == LexemeTokenType.RPANC)
			{
				scan();
				return new Object[] {TYPE_UNIT};
			}
			Object[] types = types();
			if (this.T != LexemeTokenType.RPANC)
				err("rpanc.expected");
			scan();
			return new Object[] {TYPE_TUPLE, types};
		}
		else
		{
			Object[] t0 = type2();
			if (this.T.identifier && !ident("*") && !ident("=>"))
			{
				Object[] t1 = type0();
				return new Object[] {TYPE_PARAMETERIZED, t1, new Object[] {t0, type3()}};
			}
			return t0;
		}
	}
	
	/**
	 * 语法：
	 * ArrayType := NonFuncType '[]'
	 *           | NonFuncType '[' lenMatcher ']'
	 *           | NonFuncType '*'
	 */
	private Object[] type2()
	{
		Object[] t0 = type1();
		label: while (true)
		{
			switch (this.T)
			{
			case LBRACE ://T[_]
				scan();
				if (this.T == LexemeTokenType.RBRACE)
				{
					scan();
					t0 = new Object[] {TYPE_ARRAY, t0};
					break;
				}
				else
				{
					Object[] matcher = lengthMatcher();
					if (this.T != LexemeTokenType.RBRACE)
						err("illegal.array.type");
					return new Object[] {TYPE_ARRAY_CHECK, t0, matcher};
				}
			case OPERATOR :
				if (ident("*"))
				{
					scan();
					return new Object[] {TYPE_POINTER, t0};
				}
			default:
				break label;
			}
		}
		return t0;
	}
	
	/**
	 * 语法：
	 * lenMatcher := lenRange ('|' lenMatcher)?
	 * lenRange	:= len|(len '~' len?)|(len? '~' len)
	 * len := {digit}
	 */
	private Object[] lengthMatcher()
	{
		List<int[]> range = new ArrayList<>();
		label: do
		{
			int min = 0, max = Integer.MAX_VALUE;
			switch (this.T)
			{
			case LITERAL :
				min = litInt();
				scan();
				if (ident("~"))
				{
					scan();
					if (this.T == LexemeTokenType.LITERAL)
					{
						max = litInt();
						scan();
					}
				}
				else
				{
					max = min;
				}
				range.add(new int[] {min, max});
				break;
			case OPERATOR :
			case IDENTIFIER :
				if (ident("~"))
				{
					scan();
					range.add(new int[] {min, litInt()});
					scan();
					break;
				}
			default:
				break label;
			}
			if (!ident("|"))
				break;
			scan();
		}
		while (true);
		return new Object[] {ARRAY_RANGE_MATCHER, range.toArray()};
	}
	
	/**
	 * 语法：
	 * NonFuncType := SimpleType '<|' Types '|>'
	 *             | SimpleType
	 */
	private Object[] type1()
	{
		Object[] t0 = type0();
		if (this.T == LexemeTokenType.LNAMING)
		{
			scan();
			Object[] types = types();
			if (this.T != LexemeTokenType.RNAMING)
				err("rnaming.expected");
			scan();
			return new Object[] {TYPE_PARAMETERIZED, t0, types};
		}
		return t0;
	}
	
	/**
	 * 语法：
	 * SimpleType := path
	 *            | path '#' id
	 */
	private Object[] type0()
	{
		Object[] value = identFully();
		if (this.T == LexemeTokenType.SHARP)
		{
			scan();
			return new Object[] {TYPE_REFLECT, value, ident()};
		}
		else
		{
			return new Object[] {TYPE_SIMPLE, value};
		}
	}
	
	/**
	 * 语法：
	 * Types := Type [',' Type]
	 */
	private Object[] types()
	{
		List<Object[]> types = new ArrayList<>(3);
		types.add(type4());
		while (this.T == LexemeTokenType.COMMA)
		{
			scan();
			types.add(type4());
		}
		return types.toArray();
	}
	
	class Replacement
	{
		final int parameters;
		private final Object[] transforms;
		
		Replacement(int parameters, Object[] transforms)
		{
			this.parameters = parameters;
			this.transforms = transforms;
		}
		
		Iterator<Object[]> create(Object[][]...args)
		{
			if (args.length != this.parameters)
				err("illegal.replacement.count");
			return new Iterator<Object[]>()
			{
				int i1, i2;
				
				@Override
				public boolean hasNext()
				{
					return this.i1 < Replacement.this.transforms.length;
				}
				
				@Override
				public Object[] next()
				{
					if (Replacement.this.transforms[this.i1] instanceof Object[])
					{
						return (Object[]) Replacement.this.transforms[this.i1++];
					}
					else//if (transforms[i1] instanceof Byte)
					{
						Object[][] array = args[(Byte) Replacement.this.transforms[this.i1]];
						Object[] result = array[this.i2++];
						if (this.i2 >= array.length)
						{
							this.i1 ++;
							this.i2 = 0;
						}
						return result;
					}
				}
			};
		}
	}
	
	private void define()
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		String identifier = ident();
		this.T = this.S.scan();
		List<String> parameters;
		if (this.T == LexemeTokenType.LPANC)
		{
			parameters = new ArrayList<>();
			this.T = this.S.scan();
			parameters.add(ident());
			while ((this.T = this.S.scan()) == LexemeTokenType.COMMA)
			{
				this.T = this.S.scan();
				parameters.add(ident());
			}
			if (this.T != LexemeTokenType.RPANC)
				err("def.parameter.missing.bracket");
		}
		else parameters = new ArrayList<>(0);
		List<Object> transforms = new ArrayList<>();
		scan();
		while (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
		{
			switch (this.T)
			{
			case IDENTIFIER :
				if (parameters.contains(this.S.ident().name))
				{
					transforms.add(Byte.valueOf((byte) parameters.indexOf(this.S.ident().name)));
				}
				else
				{
					transforms.add(new Object[] {LexemeTokenType.IDENTIFIER, this.S.ident()});
				}
				break;
			case LITERAL :
				transforms.add(new Object[] {LexemeTokenType.LITERAL, this.S.lit()});
				break;
			default:
				if (this.T.keyword)
				{
					if (parameters.contains(this.S.ident().name))
					{
						transforms.add(Byte.valueOf((byte) parameters.indexOf(this.T.name)));
						break;
					}
				}
				transforms.add(new Object[] {this.T});
				break;
			}
			scan();
		}
		this.replacements.put(identifier, new Replacement(parameters.size(), transforms.toArray()));
	}
	
	private void undefine()
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		String identifier = ident();
		this.replacements.remove(identifier);
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		return;
	}
	
	private void scanTransform()
	{
		Replacement r = this.replacements.get(ident());
		if (r.parameters == 0)
		{
			this.iterator = r.create();
		}
		else
		{
			Object[][][] values = new Object[r.parameters][][];
			int idx = 0;
			scan();
			if (this.T != LexemeTokenType.LPANC)
				err("replace.illegal.paramameter");
			scan();
			int i = 0;
			List<Object[]> value = new ArrayList<>();
			while (true)
			{
				switch (this.T)
				{
				case LPANC :
				case LBRACE :
				case LBRACKET :
					i++;
					value.add(new Object[] {this.T});
					break;
				case RPANC :
				case RBRACKET :
				case RBRACE :
					if (i == 0)
					{
						if (idx + 1 < values.length)
							err("replace.illegal.paramameter");
						values[idx] = value.toArray(new Object[value.size()][]);
						scan();
						this.iterator = r.create(values);
						return;
					}
					value.add(new Object[] {this.T});
					i--;
					break;
				case COMMA :
					if (i == 0)
					{
						if (idx == values.length)
							err("replace.illegal.paramameter");
						values[idx++] = value.toArray(new Object[value.size()][]);
						value = new ArrayList<>();
					}
					else
					{
						value.add(new Object[] {this.T});
					}
					break;
				case IDENTIFIER :
					value.add(new Object[] {this.T, this.S.ident()});
					break;
				case LITERAL :
					value.add(new Object[] {this.T, this.S.lit()});
					break;
				default:
					value.add(new Object[] {this.T});
					break;
				}
				scan();
			}
		}
	}
	
	private void macroIfdef(boolean flag)
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		String identifier = ident();
		if (this.ifnestidx >= 64)
			err("ifnest.too.many");
		if (flag ^ this.replacements.containsKey(identifier))
			this.ifnest |= 1 << this.ifnestidx;
		this.ifnestidx++;
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		return;
	}
	
	private void macroElseif()
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		String identifier = ident();
		if (this.ifnestidx == 0)
			err("ifnest.no.exist");
		if (this.replacements.containsKey(identifier))
			this.ifnest |= 1 << (this.ifnestidx - 1);
		else
			this.ifnest &= ~(1 << (this.ifnestidx - 1));
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		return;
	}
	
	private void macroElse()
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.ifnestidx == 0)
			err("ifnest.no.exist");
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		this.ifnest ^= 1 << this.ifnestidx;
		return;
	}
	
	private void macroEndif()
	{
		if (this.ifnestidx == 0)
			err("ifnest.no.exist");
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		this.ifnest &= ~(1 << --this.ifnestidx);
		return;
	}
	
	private void macroCal()
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		String identifier = ident();
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		int level;
		if (this.T == LexemeTokenType.LITERAL)
			level = litInt();
		else if (ident("right_unary"))
			level = LEVEL_UNARY;
		else
		{
			err("illeal.caculate.level");
			level = 1;
		}
		this.operatorLevels.put(identifier, level);
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		return;
	}
	
	private String identFullyToString()
	{
		StringBuilder builder = new StringBuilder(ident());
		while ((this.T = this.S.scan()) == LexemeTokenType.DOT)
		{
			this.T = this.S.scan();
			builder.append(',').append(ident());
		}
		return builder.toString();
	}
	
	private void macroInclude()
	{
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		IScanner scanner;
		try
		{
			if (this.T == LexemeTokenType.LITERAL)
			{
				Literal lit = this.S.lit();
				switch (lit.type)
				{
				case STRING :
					scanner = this.factory.openSource(this.S, false, (String) lit.value);
					break;
				default :
					scanner = null;
					err("include.unknown.resource.type");
					break;
				}
			}
			else if (ident("<"))
			{
				this.T = this.S.scan();
				scanner = this.factory.openSource(this.S, true, identFullyToString());
				if (!ident(">"))
					err("macro.lib.lit.missing.rbracket");
				scan();
			}
			else
			{
				err("include.unknown.resource.type");
				return;
			}
		}
		catch (IOException exception)
		{
			err("no.such.include.found");
			scanner = null;
		}
		while ((this.T = this.S.scan()) == LexemeTokenType.WHITE);
		if (this.T != LexemeTokenType.END && this.T != LexemeTokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		this.scanners.addLast(this.S);
		this.S = scanner;
		return;
	}
	
	private void scan()
	{
		while (true)
		{
			if (this.iterator != null)
			{
				if (this.iterator.hasNext())
				{
					Object[] value = this.iterator.next();
					switch (this.T = (LexemeTokenType) value[0])
					{
					case IDENTIFIER :
						this.S.setIdentifier((Name) value[1]);
						break;
					case LITERAL :
						this.S.setLiteral((Literal) value[1]);
						break;
					default:
						break;
					}
					return;
				}
				this.iterator = null;
			}
			switch (this.T = this.S.scan())
			{
			case WHITE : continue;
			case MACRO :
				switch (this.S.ident().name)
				{
				case "def" :
					define();
					break;
				case "undef" :
					undefine();
					break;
				case "ifdef" :
					macroIfdef(true);
					break;
				case "ifndef" :
					macroIfdef(false);
					break;
				case "elseif" :
					macroElseif();
					break;
				case "else" :
					macroElse();
					break;
				case "endif" :
					macroEndif();
					break;
				case "cal" :
					macroCal();
					break;
				case "include" :
					macroInclude();
					break;
				default:
					err("unknown.macro");
					break;
				}
				break;
			case IDENTIFIER :
				if (this.ifnest == 0)
				{
					if (this.replacements.containsKey(this.S.ident().name))
					{
						scanTransform();
						break;
					}
					else return;
				}
				break;
			case END :
				this.factory.cycledScanner(this.S);
				if (!this.scanners.isEmpty())
				{
					this.S = this.scanners.removeLast();
					break;
				}
				return;
			default :
				if (this.ifnest == 0)
				{
					if (this.T.keyword && this.replacements.containsKey(this.T.name))
					{
						scanTransform();
						break;
					}
					else return;
				}
				break;
			}
		}
	}
	
	private void err(String cause)
	{
		this.errored = true;
		System.out.println(cause);
	}
}
