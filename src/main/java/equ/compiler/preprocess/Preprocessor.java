/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler.preprocess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import equ.compiler.CompileContext;
import equ.compiler.EnumLexType;
import equ.compiler.ILexeme;
import equ.compiler.IMacro;
import equ.compiler.IScanner;
import equ.compiler.ScannerException;
import equ.util.BoolLinkedSet;

/**
 * @author ueyudiud
 */
public
class Preprocessor implements IScanner
{
	final CompileContext C;
	
	final String path;
	final IScanner main;
	
	LinkedList<IScanner> Ss = new LinkedList<>();
	IScanner S;
	
	ILexeme lexeme;
	LinkedList<ILexeme> lexemes = new LinkedList<>();
	
	int mode;
	BoolLinkedSet nestedSet = new BoolLinkedSet();
	
	public
	Preprocessor(CompileContext context, String path, IScanner main)
	{
		C = context;
		this.path = path;
		S = this.main = main;
	}
	
	@Override
	public String path()
	{
		return path;
	}
	
	@Override
	public ILexeme lexeme()
	{
		return lexeme;
	}
	
	@Override
	public void next() throws ScannerException
	{
		lexeme = lexeme2();
	}
	
	private ILexeme lexeme2() throws ScannerException
	{
		return lexeme1();
	}
	
	private ILexeme lexeme1() throws ScannerException
	{
		while (lexemes.isEmpty())
		{
			ILexeme lexeme = lexeme0();
			switch (lexeme.type())
			{
			case MAS:
				switch (lexeme.stringValue())
				{
				case "def":
				{
					if (nestedSet.all())
					{
						ILexeme l = lexeme0();
						if (l.type() != EnumLexType.ID)
							throw new ScannerException("Illegal define macro.", l.trace());
						String name = l.display();
						lexeme = lexeme0();
						boolean flag = false;
						List<LexemeMacroVariable> variableList = ImmutableList.of();
						Map<String, ILexeme> variableMap = ImmutableMap.of();
						if (lexeme.type() == EnumLexType.LPA &&
								lexeme.trace().col == l.trace().col + l.trace().ln)
						{
							flag = true;
							variableList = new ArrayList<>();
							variableMap = new HashMap<>();
							label: while (true)
							{
								lexeme = lexeme0();
								if (lexeme.type() != EnumLexType.ID)
									throw new ScannerException("Illegal define macro.", l.trace());
								lexeme = new LexemeMacroVariable(l.trace(), lexeme, variableList.size());
								variableList.add((LexemeMacroVariable) lexeme);
								variableMap.put(lexeme.stringValue(), lexeme);
								switch ((lexeme = lexeme0()).type())
								{
								case COM:
									continue;
								case RPA:
									lexeme = lexeme0();
									break label;
								default :
									throw new ScannerException("Illegal define macro.", l.trace());
								}
							}
						}
						final Map<String, ILexeme> list$map = variableMap;
						List<ILexeme> list = new ArrayList<ILexeme>()
						{
							private static final long serialVersionUID = -8899213526353756724L;
							
							@Override
							public boolean add(ILexeme l)
							{
								return super.add(l.type() == EnumLexType.ID ?
										list$map.getOrDefault(l.display(), l) : l);
							}
							
							@Override
							public boolean addAll(Collection<? extends ILexeme> c)
							{
								ensureCapacity(size() + c.size());
								c.forEach(this::add);
								return true;
							}
						};
						while (lexeme.type() != EnumLexType.MAE)
						{
							if (lexeme.type() == EnumLexType.ID)
							{
								lexeme$ident(lexeme, list);
							}
							else
							{
								list.add(lexeme);
							}
							lexeme = lexeme0();
						}
						C.put(name, new MacroSimple(name,
								flag ? variableList.toArray(new LexemeMacroVariable[variableList.size()]) : null,
										list.toArray(new ILexeme[list.size()])));
					}
					else
					{
						while (lexeme0().type() != EnumLexType.MAE);
					}
					break;
				}
				case "undef":
				{
					if (nestedSet.all())
					{
						lexeme = lexeme0();
						if (lexeme.type() != EnumLexType.ID)
							throw new ScannerException("Illegal undefine macro.", lexeme.trace());
						C.remove(lexeme.display());
						if ((lexeme = lexeme0()).type() != EnumLexType.MAE)
							throw new ScannerException("Illegal undefine macro.", lexeme.trace());
						break;
					}
					else
					{
						while (lexeme0().type() != EnumLexType.MAE);
					}
				}
				case "if":
				{
					List<ILexeme> lexemes = new ArrayList<>();
					ILexeme l;
					while ((l = lexeme0()).type() != EnumLexType.MAE)
					{
						lexemes.add(l);
					}
					nestedSet.append(C.eval(lexeme.trace(), lexemes).boolValue());
					break;
				}
				case "ifdef":
				{
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.ID)
						throw new ScannerException("Illegal predicate macro.", lexeme.trace());
					nestedSet.append(C.containsKey(lexeme.display()));
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.MAE)
						throw new ScannerException("Unexpected macro ending.", lexeme.trace());
					break;
				}
				case "ifndef":
				{
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.ID)
						throw new ScannerException("Illegal predicate macro.", lexeme.trace());
					nestedSet.append(!C.containsKey(lexeme.display()));
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.MAE)
						throw new ScannerException("Unexpected macro ending.", lexeme.trace());
					break;
				}
				case "else":
				{
					if (!nestedSet.swap())
						throw new ScannerException("Illegal nested macro.", lexeme.trace());
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.MAE)
						throw new ScannerException("Unexpected macro ending.", lexeme.trace());
					break;
				}
				case "elseif":
				{
					List<ILexeme> lexemes = new ArrayList<>();
					ILexeme l;
					while ((l = lexeme0()).type() != EnumLexType.MAE)
					{
						lexemes.add(l);
					}
					if (nestedSet.last())
					{
						nestedSet.swap();
					}
					else
					{
						nestedSet.set(C.eval(lexeme.trace(), lexemes).boolValue());
					}
					break;
				}
				case "elseifdef":
				{
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.ID)
						throw new ScannerException("Illegal predicate macro.", lexeme.trace());
					if (nestedSet.size() == 0)
						throw new ScannerException("Illegal nested macro.", lexeme.trace());
					if (nestedSet.last())
					{
						nestedSet.swap();
					}
					else
					{
						nestedSet.set(C.containsKey(lexeme.stringValue()));
					}
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.MAE)
						throw new ScannerException("Unexpected macro ending.", lexeme.trace());
					break;
				}
				case "elseifndef":
				{
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.ID)
						throw new ScannerException("Illegal predicate macro.", lexeme.trace());
					if (nestedSet.size() == 0)
						throw new ScannerException("Illegal nested macro.", lexeme.trace());
					if (nestedSet.last())
					{
						nestedSet.swap();
					}
					else
					{
						nestedSet.set(!C.containsKey(lexeme.stringValue()));
					}
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.MAE)
						throw new ScannerException("Unexpected macro ending.", lexeme.trace());
					break;
				}
				case "endif":
				{
					if (!nestedSet.removeLast())
						throw new ScannerException("Illegal nested macro.", lexeme.trace());
					lexeme = lexeme0();
					if (lexeme.type() != EnumLexType.MAE)
						throw new ScannerException("Unexpected macro ending.", lexeme.trace());
					break;
				}
				case "warn":
				{
					List<ILexeme> lexemes = new ArrayList<>();
					ILexeme l;
					while ((l = lexeme0()).type() != EnumLexType.MAE)
					{
						lexemes.add(l);
					}
					C.warn(C.eval(lexeme.trace(), lexemes).stringValue(), lexeme.trace());
					break;
				}
				case "error":
				{
					List<ILexeme> lexemes = new ArrayList<>();
					ILexeme l;
					while ((l = lexeme0()).type() != EnumLexType.MAE)
					{
						lexemes.add(l);
					}
					C.error(C.eval(lexeme.trace(), lexemes).stringValue(), lexeme.trace());
					break;
				}
				case "include":
				{
					List<ILexeme> lexemes = new ArrayList<>();
					ILexeme l;
					while ((l = lexeme0()).type() != EnumLexType.MAE)
					{
						lexemes.add(l);
					}
					Ss.add(S);
					S = C.openScanner(S.path(), false, C.eval(lexeme.trace(), lexemes).stringValue());
					break;
				}
				default:
					throw new ScannerException("Unknown macro token.", lexeme.trace());
				}
				break;
			case MAE:
				throw new InternalError();
			case ID :
				if (nestedSet.all())
				{
					lexeme$ident(lexeme, lexemes);
				}
				break;
			default:
				if (nestedSet.all())
				{
					lexemes.addLast(lexeme);
				}
				break;
			}
		}
		return lexemes.removeFirst();
	}
	
	private void lexeme$ident(final ILexeme lexeme, List<ILexeme> list) throws ScannerException
	{
		final ILexeme l0 = lexeme;
		IMacro macro = C.get(lexeme.display());
		if (macro != null)
		{
			if (macro.parameterCount() >= 0)
			{
				if (lexeme0().type() != EnumLexType.LPA)
					throw new ScannerException("Unexpected token for macro transformer.", l0.trace());
				final int c = macro.parameterCount();
				LinkedList<ILexeme>[] lists = new LinkedList[c];
				for (int i = 0; i < c; ++i)
				{
					ILexeme l = stopToComma(lists[i] = new LinkedList<>());
					switch (l.type())
					{
					case COM:
						if (i < c - 1)
							break;
						throw new ScannerException("Unexpected token for macro transformer.", l0.trace());
					case RPA:
						if (i == c - 1)
							break;
						throw new ScannerException("Unexpected token for macro transformer.", l0.trace());
					default:
						throw new ScannerException("Illegal macro transformer.", l0.trace());
					}
				}
				list.addAll(macro.transform(l0, lists));
			}
			else
			{
				list.addAll(macro.transform(l0));
			}
		}
		else
		{
			list.add(lexeme);
		}
	}
	
	private ILexeme stopToPanc(List<ILexeme> list) throws ScannerException
	{
		while (true)
		{
			ILexeme lexeme = lexeme0();
			switch (lexeme.type())
			{
			case END:
			case RPA:
				return lexeme;
			case LPA:
				list.add(lexeme);
				lexeme = stopToPanc(list);
				if (lexeme.type() != EnumLexType.RPA)
					C.error("Missing a ')' in expression.", lexeme.trace());
				list.add(lexeme);
				break;
			case ID :
				lexeme$ident(lexeme, list);
				break;
			default :
				list.add(lexeme);
				break;
			}
		}
	}
	
	private ILexeme stopToComma(List<ILexeme> list) throws ScannerException
	{
		while (true)
		{
			ILexeme lexeme = lexeme0();
			switch (lexeme.type())
			{
			case END:
			case RPA:
			case COM:
				return lexeme;
			case LPA:
				list.add(lexeme);
				lexeme = stopToPanc(list);
				if (lexeme.type() != EnumLexType.RPA)
					C.error("Missing a ')' in expression.", lexeme.trace());
				list.add(lexeme);
				break;
			case ID :
				lexeme$ident(lexeme, list);
				break;
			default :
				list.add(lexeme);
				break;
			}
		}
	}
	
	private ILexeme lexeme0() throws ScannerException
	{
		ILexeme lexeme;
		while (true)
		{
			S.next();
			lexeme = S.lexeme();
			if (lexeme.type() != EnumLexType.END || Ss.isEmpty())
			{
				return lexeme;
			}
			else if (!Ss.isEmpty())
			{
				S = Ss.removeLast();
			}
		}
	}
}
