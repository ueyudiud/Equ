/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import equ.ast.ArrayExprNode;
import equ.ast.ConstNode;
import equ.ast.ExprNode;
import equ.ast.FullExprNode;
import equ.ast.IdentNode;
import equ.ast.IdentToNode;
import equ.ast.MethodExprNode;
import equ.ast.NestExprNode;
import equ.ast.OpBinaryNode;
import equ.ast.OpUnaryNode;
import equ.ast.TypeArrayNode;
import equ.ast.TypeFullNode;
import equ.ast.TypeFunctionalNode;
import equ.ast.TypeNode;
import equ.ast.TypeParameterizedNode;
import equ.ast.TypeReflectionNode;
import equ.ast.TypeSimpleNode;
import equ.ast.TypeTupleNode;
import equ.ast.TypeWildcardNode;
import equ.compiler1.Literal.LiteralType;

/**
 * @author ueyudiud
 */
class CodeParser extends Parser implements IParser
{
	protected CodeParser(IScannerFactory factory, IScanner scanner)
	{
		super(factory, scanner);
		next();
	}
	
	@Override
	protected void next()
	{
		do
		{
			super.next();
		}
		while (this.T == LexType.COMMENT_BLOCK || this.T == LexType.COMMENT_DOC || this.T == LexType.COMMENT_LINE);
	}
	
	void parseType(Context context)
	{
		switch (this.T)
		{
		case OPERATOR :
			if (ident("=>"))
			{
				
			}
			break;
		case LBRACE :
		default:
			break;
		}
		previousErr("unknown.type");
	}
	
	public TypeNode parseType()
	{
		return parseType1();
	}
	
	private TypeNode parseType1()
	{
		return parseType5();
	}
	
	private TypeNode parseType5()
	{
		TypeNode node = parseType6();
		while (ident("=>"))
		{
			next();
			if (node instanceof TypeTupleNode)
			{
				node = new TypeFunctionalNode(parseType5(), ((TypeTupleNode) node).getParameters());
			}
			else
			{
				node = new TypeFunctionalNode(parseType5(), node);
			}
		}
		return node;
	}
	
	private TypeNode parseType6()
	{
		switch (this.T)
		{
		case LPANC :
			next();
			TypeNode[] types = parseTypes();
			match(LexType.RPANC);
			next();
			switch (types.length)
			{
			case 0 :
				return new TypeFullNode("equ", "Unit");
			case 1 :
				return types[0];
			default:
				return parseTypeSuffix(new TypeTupleNode(types));
			}
		default:
			return parseType7();
		}
	}
	
	private TypeNode parseType7()
	{
		if (this.T == LexType.OPERATOR && "?".equals(this.S.ident().name))
		{
			next();
			TypeNode lower = null;
			TypeNode[] upper = new TypeNode[0];
			label: while (this.T == LexType.OPERATOR)
			{
				switch (this.S.ident().name)
				{
				case "<:" :
					if (upper.length != 0)
						previousErr("already.defined.upper.bound");
					next();
					upper = parseTypes();
					if (upper.length == 0)
						previousErr("no.bound.types.found");
					break;
				case ">:" :
					if (lower != null)
						previousErr("already.defined.lower.bound");
					next();
					lower = parseType();
				default:
					break label;
				}
			}
			return new TypeWildcardNode(lower, upper);
		}
		else
		{
			TypeNode node = parseTypeSuffix(parseType9());
			while (true)
			{
				switch (this.T)
				{
				case OPERATOR :
					switch (this.S.ident().name)
					{
					case "=>" :
					case "<:" :
					case ">:" :
						return node;
					default:
						break;
					}
				case IDENTIFIER :
					break;
				default:
					return node;
				}
				node = new TypeParameterizedNode(parseType10(), node, parseType7());
			}
		}
	}
	
	private TypeNode parseTypeSuffix(TypeNode node)
	{
		while (true)
		{
			switch (this.T)
			{
			case LBRACE :
				node = new TypeArrayNode(node, parseArray());
				next();
				match(LexType.RBRACE);
				next();
				continue;
			case OPERATOR :
				switch (this.S.ident().name)
				{
				case "*" :
					node = new TypeParameterizedNode(new TypeFullNode("equ", "Pointer"), node);
					next();
					break;
				case "?" :
					node = new TypeParameterizedNode(new TypeFullNode("equ", "Option"), node);
					next();
					break;
				default :
					return node;
				}
				continue;
			default:
				return node;
			}
		}
	}
	
	private int[][] parseArray()
	{
		List<int[]> list = new ArrayList<>();
		int[] is;
		while ((is = parseArrayOrderPart()) != null)
		{
			list.add(is);
		}
		return list.toArray(new int[list.size()][]);
	}
	
	private int[] parseArrayOrderPart()
	{
		int start, end;
		switch (this.T)
		{
		case LITERAL :
			match(LiteralType.INT);
			start = (int) this.S.lit().value;
			next();
			if (ident("~"))
			{
				if (this.T == LexType.LITERAL)
				{
					match(LiteralType.INT);
					end = (int) this.S.lit().value;
				}
				else
				{
					end = Integer.MAX_VALUE;
				}
				next();
			}
			else
			{
				end = start;
			}
			return new int[] {start, end};
		case OPERATOR:
			if (this.S.ident().name.equals("~"))
			{
				next();
				match(LiteralType.INT);
				end = (int) this.S.lit().value;
				next();
				return new int[] {0, end};
			}
		default:
			return null;
		}
	}
	
	private TypeNode parseType9()
	{
		TypeNode node = parseType10();
		while (this.T == LexType.SHARP)
		{
			next();
			node = new TypeReflectionNode(node, symbol());
			next();
		}
		if (this.T == LexType.LNAMING)
		{
			next();
			TypeNode[] parameters = parseTypes();
			match(LexType.RNAMING);
			next();
			return new TypeParameterizedNode(node, parameters);
		}
		return node;
	}
	
	private TypeNode parseType10()
	{
		String name = symbol();
		next();
		if (this.T == LexType.DOT)
		{
			List<String> list = new ArrayList<>();
			list.add(name);
			while (this.T == LexType.DOT)
			{
				next();
				list.add(symbol());
				next();
			}
			return new TypeFullNode(list.toArray(new String[list.size()]));
		}
		else
		{
			return new TypeSimpleNode(name);
		}
	}
	
	private TypeNode[] parseTypes()
	{
		List<TypeNode> list = new ArrayList<>(4);
		list.add(parseType1());
		while (this.T == LexType.COMMA)
		{
			next();
			list.add(parseType1());
		}
		return list.toArray(new TypeNode[list.size()]);
	}
	
	private enum State
	{
		I, S, O;
	}
	
	@Override
	public FullExprNode parseExpression()
	{
		List<ExprNode> nodes = new ArrayList<>(3);
		LinkedList<ExprNode> lbuf = new LinkedList<>();
		LinkedList<Object[]> rbuf = new LinkedList<>();
		State state = State.S;
		label: while (true)
		{
			boolean nested = false;
			switch (this.T)
			{
			case END :
			case RBRACE :
			case RPANC :
			case RBRACKET :
				break label;
			case LBRACE :
				nested = true;
			case LPANC :
			{
				boolean left = nested;
				next();
				FullExprNode node = parseExpression();
				nested = false;
				switch (this.T)
				{
				case RBRACE :
					nested = true;
				case RPANC :
					boolean right = nested;
					switch (state)
					{
					case S :
						if (left && right)
							lbuf.add(new ArrayExprNode(node));
						else
							lbuf.add(new NestExprNode(node, left, right));
						break;
					case O :
					case I :
						lbuf.add(new MethodExprNode(lbuf.removeLast(), new NestExprNode(node, left, right)));
						break;
					}
					state = State.I;
					next();
					break;
				default:
					previousErr("wrong.bracket.nested");
					break;
				}
				break;
			}
			case COMMA :
				while (!rbuf.isEmpty())
				{
					Object[] values = rbuf.getFirst();
					int level1 = (int) values[0];
					if (level1 == UNARY_CACULATE_LEVEL)
					{
						lbuf.addLast(new OpUnaryNode(lbuf.removeLast(), (IdentNode) values[1]));
						rbuf.removeFirst();
					}
					else
					{
						ExprNode node2 = lbuf.removeLast();
						lbuf.addLast(new OpBinaryNode(lbuf.removeLast(), node2, (IdentNode) values[1]));
						rbuf.removeFirst();
					}
				}
				if (lbuf.size() != 1)
					previousErr("illegal.expression");
				nodes.add(lbuf.remove());
				state = State.S;
				next();
				break;
			case DOT :
				if (state != State.I)
					previousErr("illegal.expression");
				next();
				lbuf.addLast(new IdentToNode(lbuf.removeLast(), parseIdentWithParameterized()));
				break;
			case LITERAL :
				lbuf.add(new ConstNode(this.S.lit().type, this.S.lit().value));
				next();
				break;
			case IDENTIFIER :
				if (state == State.I)
				{
					IdentNode node = parseIdentWithParameterized();
					int level = getCaculateLevel(node.getName());
					while (!rbuf.isEmpty())
					{
						Object[] values = rbuf.getFirst();
						int level1 = (int) values[0];
						if (level1 == UNARY_CACULATE_LEVEL)
						{
							lbuf.addLast(new OpUnaryNode(lbuf.removeLast(), (IdentNode) values[1]));
							rbuf.removeFirst();
						}
						else if (level < level1)
						{
							ExprNode node2 = lbuf.removeLast();
							lbuf.addLast(new OpBinaryNode(lbuf.removeLast(), node2, (IdentNode) values[1]));
							rbuf.removeFirst();
						}
						else
						{
							break;
						}
					}
					rbuf.addFirst(new Object[] {getCaculateLevel(node.getName()), node});
					state = State.O;
				}
				else
				{
					lbuf.addFirst(parseIdentWithParameterized());
					state = State.I;
				}
				break;
			case OPERATOR :
				switch (state)
				{
				case O :
				case S :
					IdentNode node = parseIdentWithParameterized();
					rbuf.addFirst(new Object[] {UNARY_CACULATE_LEVEL, node});
					break;
				case I :
				{
					node = parseIdentWithParameterized();
					int level = getCaculateLevel(node.getName());
					boolean leftConnect = isLeftConnect(node.getName());
					while (!rbuf.isEmpty())
					{
						Object[] values = rbuf.getFirst();
						int level1 = (int) values[0];
						if (level1 == UNARY_CACULATE_LEVEL)
						{
							lbuf.addLast(new OpUnaryNode(lbuf.removeLast(), (IdentNode) values[1]));
							rbuf.removeFirst();
						}
						else if (level < level1 || (leftConnect && level == level1))
						{
							ExprNode node2 = lbuf.removeLast();
							lbuf.addLast(new OpBinaryNode(lbuf.removeLast(), node2, (IdentNode) values[1]));
							rbuf.removeFirst();
						}
						else
						{
							break;
						}
					}
					rbuf.addFirst(new Object[] {level, node});
					break;
				}
				}
				state = State.O;
				break;
			default:
				previousErr("unexpected.token", this.T);
				next();
				break;
			}
		}
		while (!rbuf.isEmpty())
		{
			Object[] values = rbuf.getFirst();
			int level1 = (int) values[0];
			if (level1 == UNARY_CACULATE_LEVEL)
			{
				lbuf.addLast(new OpUnaryNode(lbuf.removeLast(), (IdentNode) values[1]));
				rbuf.removeFirst();
			}
			else
			{
				ExprNode node2 = lbuf.removeLast();
				lbuf.addLast(new OpBinaryNode(lbuf.removeLast(), node2, (IdentNode) values[1]));
				rbuf.removeFirst();
			}
		}
		if (lbuf.size() == 1)
		{
			nodes.add(lbuf.remove());
		}
		else if (lbuf.size() != 0)
		{
			previousErr("illegal.expression");
		}
		return new FullExprNode(nodes);
	}
	
	private boolean isLeftConnect(String key)
	{
		return key.charAt(key.length() - 1) == '=';
	}
	
	private IdentNode parseIdentWithParameterized()
	{
		String name = symbol();
		next();
		switch (this.T)
		{
		case LNAMING :
			TypeNode[] types = parseTypes();
			match(LexType.RNAMING);
			next();
			return new IdentNode(name, types);
		default:
			return new IdentNode(name, null);
		}
	}
}
