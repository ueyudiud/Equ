/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import equ.compiler.Literal.LiteralType;
import equ.util.Dictionary;

/**
 * @author ueyudiud
 */
abstract class Parser
{
	private static final Pattern PATTERN = Pattern.compile("[a-zA-Z_]\\w*");
	
	protected final IScannerFactory factory;
	
	private static class Lexeme
	{
		LexType type;
		Object value;
		
		Lexeme(LexType type, Object value)
		{
			this.type = type;
			this.value = value;
		}
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
		
		IScanner create(SourcePosition err, Lexeme[]...args)
		{
			if (args.length != this.parameters)
			{
				Parser.this.S.setErrPos(err);
				Parser.this.factory.error(Parser.this.S, "illegal.replacement.count");
			}
			return new AbstractScanner(Parser.this.factory)
			{
				int i1, i2, mi1, mi2;
				
				@Override
				public void scan()
				{
					if (Replacement.this.transforms[this.i1] instanceof Object[])
					{
						parseLexeme((Lexeme) Replacement.this.transforms[this.i1++]);
					}
					else//if (transforms[i1] instanceof Byte)
					{
						Lexeme[] array = args[(Byte) Replacement.this.transforms[this.i1]];
						Lexeme result = array[this.i2++];
						if (this.i2 >= array.length)
						{
							this.i1 ++;
							this.i2 = 0;
						}
						parseLexeme(result);
					}
				}
				
				private void parseLexeme(Lexeme lexeme)
				{
					switch (this.type = lexeme.type)
					{
					case IDENTIFIER :
					case OPERATOR :
						this.ident = (Identifier) lexeme.value;
						break;
					case LITERAL :
						this.lit = (Literal) lexeme.value;
						break;
					default:
						break;
					}
				}
				
				@Override
				public void mark()
				{
					this.mi1 = this.i1;
					this.mi2 = this.i2;
				}
				
				@Override
				public void reset()
				{
					this.i1 = this.mi1;
					this.i2 = this.mi2;
					scan();
				}
				
				@Override
				public SourcePosition current()
				{
					return err;
				}
				
				@Override
				public SourcePosition previous()
				{
					return err;
				}
			};
		}
	}
	
	protected IScanner S;
	protected LexType T;
	private Map<String, Replacement> replacements = new HashMap<>();
	private LinkedList<IScanner> scanners = new LinkedList<>();
	private Dictionary<Integer> operatorLevels = new Dictionary<>();
	private long nest = 0;
	private int nestidx = 0;
	
	protected Parser(IScannerFactory factory, IScanner scanner)
	{
		this.factory = factory;
		this.S = scanner;
	}
	
	protected String symbol()
	{
		switch (this.T)
		{
		case PACKAGE : return "package";
		case CLASS : return "class";
		case ENUM : return "enum";
		case INTERFACE : return "interface";
		case OBJECT : return "object";
		case ATTRIBUTE : return "attribute";
		case THAT : return "that";
		case THIS : return "this";
		case SUPER : return "super";
		case VAL : return "val";
		case VAR : return "var";
		case IF : return "if";
		case ELSE : return "else";
		case DO : return "do";
		case WHILE : return "while";
		case SWITCH : return "switch";
		case CASE : return "case";
		case IMPORT : return "import";
		case EXTENDS : return "extends";
		case IDENTIFIER :
		case OPERATOR :
			return this.S.ident().name;
		default:
			this.factory.error(this.S, "identifier.expected");
			return "";
		}
	}
	
	protected boolean ident(String ident)
	{
		switch (this.T)
		{
		case PACKAGE : return "package".equals(ident);
		case CLASS : return "class".equals(ident);
		case ENUM : return "enum".equals(ident);
		case INTERFACE : return "interface".equals(ident);
		case OBJECT : return "object".equals(ident);
		case ATTRIBUTE : return "attribute".equals(ident);
		case THAT : return "that".equals(ident);
		case THIS : return "this".equals(ident);
		case SUPER : return "super".equals(ident);
		case VAL : return "val".equals(ident);
		case VAR : return "var".equals(ident);
		case IF : return "if".equals(ident);
		case ELSE : return "else".equals(ident);
		case DO : return "do".equals(ident);
		case WHILE : return "while".equals(ident);
		case SWITCH : return "switch".equals(ident);
		case CASE : return "case".equals(ident);
		case IMPORT : return "import".equals(ident);
		case EXTENDS : return "extends".equals(ident);
		case IDENTIFIER :
		case OPERATOR :
			return this.S.ident().name.equals(ident);
		default:
			return false;
		}
	}
	
	protected final void next()
	{
		scanMacroExpaned();
	}
	
	private void scanMacroExpaned()
	{
		scanInternal();
		label: while (true)
		{
			switch (this.T = this.S.type())
			{
			case NL :
				scanInternal();
				if (this.S.type() == LexType.SHARP)
				{
					scanMacro();
				}
				continue;
			case END :
				return;
			case PACKAGE : case CLASS : case ENUM : case INTERFACE :
			case OBJECT : case ATTRIBUTE : case THAT : case THIS :
			case SUPER : case VAL : case VAR : case IF : case ELSE :
			case DO : case WHILE : case SWITCH : case CASE : case IMPORT :
			case EXTENDS :
			case IDENTIFIER :
				if (this.nest == 0L)
				{
					if (!this.replacements.isEmpty())
					{
						String id = symbol();
						Replacement r = this.replacements.get(id);
						if (r != null)
						{
							final SourcePosition pos = this.S.previous();
							if (r.parameters == 0)
							{
								this.S.scan();
								this.scanners.addLast(this.S);
								this.S = r.create(pos);
							}
							else
							{
								Lexeme[][] values = new Lexeme[r.parameters][];
								int idx = 0;
								scanMacroExpaned();
								if (this.T != LexType.LPANC)
								{
									previousErr("replace.illegal.paramameter");
								}
								scanMacroExpaned();
								int i = 0;
								List<Lexeme> value = new ArrayList<>();
								while (true)
								{
									switch (this.T)
									{
									case LPANC :
									case LBRACE :
									case LBRACKET :
										i++;
										value.add(new Lexeme(this.T, null));
										break;
									case RPANC :
									case RBRACKET :
									case RBRACE :
										if (i == 0)
										{
											if (idx + 1 < values.length)
												previousErr("replace.illegal.paramameter");
											values[idx] = value.toArray(new Lexeme[value.size()]);
											this.S.scan();
											this.scanners.add(this.S);
											this.S = r.create(pos, values);
											continue label;
										}
										value.add(new Lexeme(this.S.type(), null));
										i--;
										break;
									case COMMA :
										if (i == 0)
										{
											if (idx == values.length)
												previousErr("replace.illegal.paramameter");
											values[idx++] = value.toArray(new Lexeme[value.size()]);
											value = new ArrayList<>();
										}
										else
										{
											value.add(new Lexeme(this.S.type(), null));
										}
										break;
									case IDENTIFIER :
									case OPERATOR :
										value.add(new Lexeme(this.S.type(), this.S.ident()));
										break;
									case LITERAL :
										value.add(new Lexeme(this.S.type(), this.S.lit()));
										break;
									default:
										value.add(new Lexeme(this.S.type(), null));
										break;
									}
									this.S.scan();
								}
							}
							break;
						}
					}
					return;
				}
				scanInternal();
				break;
			default :
				if (this.nest == 0L)
				{
					return;
				}
				scanInternal();
				break;
			}
		}
	}
	
	private void macroPattenMatch(String ident)
	{
		if (!PATTERN.matcher(ident).matches())
		{
			previousErr("illegal.macro", ident);
		}
	}
	
	private void macroEndingMatch()
	{
		if (this.S.type() != LexType.END && this.S.type() != LexType.NL)
		{
			previousErr("macro.unexpected.ending");
		}
	}
	
	private void scanMacro()
	{
		this.S.scan();
		match(LexType.IDENTIFIER);
		switch (this.S.ident().name)
		{
		case "def" :
			this.S.scan();
			match(LexType.IDENTIFIER);
			String identifier = this.S.ident().name;
			macroPattenMatch(identifier);
			this.S.scan();
			List<String> parameters;
			if (this.S.type() == LexType.LPANC)
			{
				parameters = new ArrayList<>();
				this.S.scan();
				parameters.add(symbol());
				while (true)
				{
					if (this.S.type() == LexType.COMMA)
					{
						this.S.scan();
						parameters.add(symbol());
						this.S.scan();
					}
					else
					{
						break;
					}
				}
				if (this.S.type() != LexType.RPANC)
				{
					previousErr("def.parameter.missing.bracket");
				}
				this.S.scan();
			}
			else parameters = new ArrayList<>(0);
			List<Object> transforms = new ArrayList<>();
			label: while (true)
			{
				switch (this.S.type())
				{
				case NL :
				case END :
					break label;
				case IDENTIFIER :
					if (parameters.contains(this.S.ident().name))
					{
						transforms.add(Byte.valueOf((byte) parameters.indexOf(this.S.ident().name)));
					}
					else
					{
						transforms.add(new Lexeme(LexType.IDENTIFIER, this.S.ident()));
					}
					break;
				case LITERAL :
					transforms.add(new Lexeme(LexType.LITERAL, this.S.lit()));
					break;
				default :
					transforms.add(new Lexeme(this.S.type(), null));
					break;
				}
				this.S.scan();
			}
			this.replacements.put(identifier, new Replacement(parameters.size(), transforms.toArray()));
			break;
		case "undef" :
			this.S.scan();
			match(LexType.IDENTIFIER);
			identifier = this.S.ident().name;
			macroPattenMatch(identifier);
			this.replacements.remove(identifier);
			this.S.scan();
			macroEndingMatch();
			break;
		case "ifdef" :
			this.S.scan();
			match(LexType.IDENTIFIER);
			identifier = this.S.ident().name;
			macroPattenMatch(identifier);
			if (this.nestidx >= 64)
				previousErr("ifnest.too.many");
			if (!this.replacements.containsKey(identifier))
				this.nest |= 1 << this.nestidx;
			this.nestidx++;
			this.S.scan();
			macroEndingMatch();
			break;
		case "ifndef" :
			this.S.scan();
			match(LexType.IDENTIFIER);
			identifier = this.S.ident().name;
			macroPattenMatch(identifier);
			if (this.nestidx >= 64)
				previousErr("ifnest.too.many");
			if (this.replacements.containsKey(identifier))
				this.nest |= 1 << this.nestidx;
			this.nestidx++;
			this.S.scan();
			macroEndingMatch();
			break;
		case "elseif" :
			this.S.scan();
			match(LexType.IDENTIFIER);
			identifier = this.S.ident().name;
			macroPattenMatch(identifier);
			if (this.nestidx == 0)
				previousErr("ifnest.no.exist");
			if (this.replacements.containsKey(identifier))
				this.nest |= 1 << (this.nestidx - 1);
			else
				this.nest &= ~(1 << (this.nestidx - 1));
			this.S.scan();
			macroEndingMatch();
			break;
		case "else" :
			if (this.nestidx == 0)
				previousErr("ifnest.no.exist");
			this.nest ^= 1 << (this.nestidx - 1);
			this.S.scan();
			macroEndingMatch();
			break;
		case "endif" :
			if (this.nestidx == 0)
				previousErr("ifnest.no.exist");
			this.nest &= ~(1 << --this.nestidx);
			this.S.scan();
			macroEndingMatch();
			break;
		case "include" :
			this.S.scan();
			match(LiteralType.STRING);
			identifier = (String) this.S.lit().value;
			this.S.scan();
			macroEndingMatch();
			this.scanners.addLast(this.S);
			try
			{
				this.S = this.factory.openSource(this.S, identifier);
			}
			catch (IOException e)
			{
				previousErr("fail.open.source", e.getMessage());
			}
			break;
		case "cal" :
			this.S.scan();
			identifier = symbol();
			this.S.scan();
			match(LiteralType.INT);
			this.operatorLevels.put(identifier, (Integer) this.S.lit().value);
			this.S.scan();
			macroEndingMatch();
			break;
		case "calrem" :
			this.S.scan();
			identifier = symbol();
			this.operatorLevels.remove(identifier);
			this.S.scan();
			macroEndingMatch();
			break;
		default:
			if (this.S.type() == LexType.IDENTIFIER)
				previousErr("unknown.macro", this.S.ident().name);
			else
				match(LexType.IDENTIFIER);
			break;
		}
	}
	
	private void scanInternal()
	{
		for (;;)
		{
			this.S.scan();
			if (this.S.type() == LexType.END)
			{
				this.factory.closeScanner(this.S);
				if (!this.scanners.isEmpty())
				{
					this.S = this.scanners.removeLast();
					continue;
				}
			}
			return;
		}
	}
	
	protected void match(LexType type)
	{
		if (this.S.type() != type)
		{
			this.factory.error(this.S, "token.unexpected", type, this.S.type());
		}
	}
	
	protected void match(LiteralType type)
	{
		if (this.S.type() != LexType.LITERAL || this.S.lit().type != type)
		{
			this.factory.error(this.S, "token.lit.unexpected", type);
		}
	}
	
	protected void previousErr(String cause, Object...formats)
	{
		this.S.setErrPos(this.S.previous());
		this.factory.error(this.S, cause, formats);
	}
}

class SimpleParser extends Parser implements IScanner
{
	protected SimpleParser(IScannerFactory factory, IScanner scanner)
	{
		super(factory, scanner);
	}
	
	@Override
	public IScannerFactory getFactory()
	{
		return this.factory;
	}
	
	@Override
	public void scan()
	{
		next();
	}
	
	@Override
	public void mark()
	{
		this.S.mark();
	}
	
	@Override
	public void reset()
	{
		this.S.reset();
	}
	
	@Override
	public LexType type()
	{
		return this.T;
	}
	
	@Override
	public void setIdent(Identifier identifier)
	{
		this.S.setIdent(identifier);
	}
	
	@Override
	public Identifier ident()
	{
		return this.S.ident();
	}
	
	@Override
	public void setLit(Literal literal)
	{
		this.S.setLit(literal);
	}
	
	@Override
	public Literal lit()
	{
		return this.S.lit();
	}
	
	@Override
	public void setCom(Comment comment)
	{
		this.S.setCom(comment);
	}
	
	@Override
	public Comment com()
	{
		return this.S.com();
	}
	
	@Override
	public SourcePosition current()
	{
		return this.S.current();
	}
	
	@Override
	public SourcePosition previous()
	{
		return this.S.previous();
	}
	
	@Override
	public void setErrPos(SourcePosition pos)
	{
		this.S.setErrPos(pos);
	}
	
	@Override
	public SourcePosition err()
	{
		return this.S.err();
	}
}
