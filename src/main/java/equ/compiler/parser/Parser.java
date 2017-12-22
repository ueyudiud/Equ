/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	Scanner S;
	TokenType T;
	
	private boolean errored = false;
	private Iterator<Object[]> iterator;
	private Map<String, Replacement> replacements = new HashMap<>();
	private Dictionary<Integer> operatorLevels = new Dictionary<>();
	private long ifnest = 0;
	private int ifnestidx = 0;
	
	public Parser(Scanner scanner)
	{
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
		return this.T == TokenType.IDENTIFIER && key.equals(this.S.ident().name);
	}
	
	private int litInt()
	{
		if (this.T != TokenType.LITERAL || this.S.lit().type != LitType.INT)
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
		while (this.T == TokenType.DOT)
		{
			scan();
			value = new Object[] {TO, value, ident()};
			scan();
		}
		return value;
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
		if (this.T == TokenType.LPANC)
		{
			scan();
			if (this.T == TokenType.RPANC)
			{
				scan();
				return new Object[] {TYPE_UNIT};
			}
			Object[] types = types();
			if (this.T != TokenType.RPANC)
				err("rpanc.expected");
			scan();
			return new Object[] {TYPE_TUPLE, types};
		}
		else
		{
			Object[] t0 = type2();
			if (this.T == TokenType.IDENTIFIER && !ident("*") && !ident("=>"))
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
				if (this.T == TokenType.RBRACE)
				{
					scan();
					t0 = new Object[] {TYPE_ARRAY, t0};
					break;
				}
				else
				{
					Object[] matcher = lengthMatcher();
					if (this.T != TokenType.RBRACE)
						err("illegal.array.type");
					return new Object[] {TYPE_ARRAY_CHECK, t0, matcher};
				}
			case IDENTIFIER :
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
					if (this.T == TokenType.LITERAL)
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
		if (this.T == TokenType.LNAMING)
		{
			scan();
			Object[] types = types();
			if (this.T != TokenType.RNAMING)
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
		if (this.T == TokenType.SHARP)
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
		while (this.T == TokenType.COMMA)
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
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		String identifier = ident();
		this.T = this.S.scan();
		List<String> parameters;
		if (this.T == TokenType.LPANC)
		{
			parameters = new ArrayList<>();
			this.T = this.S.scan();
			parameters.add(ident());
			while ((this.T = this.S.scan()) == TokenType.COMMA)
			{
				this.T = this.S.scan();
				parameters.add(ident());
			}
			if (this.T != TokenType.RPANC)
				err("def.parameter.missing.bracket");
		}
		else parameters = new ArrayList<>(0);
		List<Object> transforms = new ArrayList<>();
		scan();
		while (this.T != TokenType.END && this.T != TokenType.MACRO_ENDING)
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
					transforms.add(new Object[] {TokenType.IDENTIFIER, this.S.ident()});
				}
				break;
			case LITERAL :
				transforms.add(new Object[] {TokenType.LITERAL, this.S.lit()});
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
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		String identifier = ident();
		this.replacements.remove(identifier);
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		if (this.T != TokenType.END && this.T != TokenType.MACRO_ENDING)
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
			if (this.T != TokenType.LPANC)
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
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		String identifier = ident();
		if (this.ifnestidx >= 64)
			err("ifnest.too.many");
		if (flag ^ this.replacements.containsKey(identifier))
			this.ifnest |= 1 << this.ifnestidx;
		this.ifnestidx++;
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		if (this.T != TokenType.END && this.T != TokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		return;
	}
	
	private void macroElseif()
	{
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		String identifier = ident();
		if (this.ifnestidx == 0)
			err("ifnest.no.exist");
		if (this.replacements.containsKey(identifier))
			this.ifnest |= 1 << (this.ifnestidx - 1);
		else
			this.ifnest &= ~(1 << (this.ifnestidx - 1));
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		if (this.T != TokenType.END && this.T != TokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		return;
	}
	
	private void macroElse()
	{
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		if (this.ifnestidx == 0)
			err("ifnest.no.exist");
		if (this.T != TokenType.END && this.T != TokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		this.ifnest ^= 1 << this.ifnestidx;
		return;
	}
	
	private void macroEndif()
	{
		while ((this.T = this.S.scan()) == TokenType.WHITE);
		if (this.ifnestidx == 0)
			err("ifnest.no.exist");
		if (this.T != TokenType.END && this.T != TokenType.MACRO_ENDING)
			err("undefine.unexpected.ending");
		this.ifnest &= ~(1 << --this.ifnestidx);
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
					switch (this.T = (TokenType) value[0])
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
	
	public void log()
	{
		while (this.T != TokenType.END)
		{
			switch (this.T)
			{
			case IDENTIFIER :
				System.out.println(this.S.ident());
				break;
			case LITERAL :
				System.out.println(this.S.lit());
				break;
			default:
				System.out.println(this.T);
				break;
			}
			scan();
		}
	}
	
	private void err(String cause)
	{
		this.errored = true;
	}
}
