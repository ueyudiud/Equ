/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler1;

import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

import equ.ast.AbstractASTVisitor;
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
import equ.ast.TypeDiamondNode;
import equ.ast.TypeFullNode;
import equ.ast.TypeFunctionalNode;
import equ.ast.TypeNode;
import equ.ast.TypeParameterizedNode;
import equ.ast.TypeReflectionNode;
import equ.ast.TypeSimpleNode;
import equ.ast.TypeTupleNode;
import equ.ast.TypeWildcardNode;
import equ.util.Strings;

/**
 * @author ueyudiud
 */
public class ASTPrintVisitor extends AbstractASTVisitor
{
	@Override
	public void visitTypeNode(TypeNode node)
	{
		print(node);
	}
	
	private void print(TypeNode node)
	{
		if (node instanceof TypeSimpleNode)
		{
			System.out.print(((TypeSimpleNode) node).getName());
		}
		else if (node instanceof TypeFullNode)
		{
			System.out.print(Strings.toString(((TypeFullNode) node).getName(), '.', UnaryOperator.identity()));
		}
		else if (node instanceof TypeReflectionNode)
		{
			print(((TypeReflectionNode) node).getParent());
			System.out.print('#');
			System.out.print(((TypeReflectionNode) node).getReflect());
		}
		else if (node instanceof TypeFunctionalNode)
		{
			System.out.print('(');
			printArray(((TypeFunctionalNode) node).getParameters());
			System.out.print(")=>");
			print(((TypeFunctionalNode) node).getResult());
		}
		else if (node instanceof TypeTupleNode)
		{
			System.out.print('(');
			printArray(((TypeTupleNode) node).getParameters());
			System.out.print(')');
		}
		else if (node instanceof TypeParameterizedNode)
		{
			print(((TypeParameterizedNode) node).getBase());
			System.out.print("<|");
			printArray(((TypeParameterizedNode) node).getParameters());
			System.out.print("|>");
		}
		else if (node instanceof TypeDiamondNode)
		{
			print(((TypeDiamondNode) node).getParent());
			System.out.print("<||>");
		}
		else if (node instanceof TypeWildcardNode)
		{
			if (((TypeWildcardNode) node).getLower() != null)
			{
				print(((TypeWildcardNode) node).getLower());
				System.out.print(" <: ");
			}
			System.out.print("?");
			if (((TypeWildcardNode) node).getUpper().length > 0)
			{
				System.out.print(" <: ");
				printArray(" & ", ((TypeWildcardNode) node).getUpper());
			}
		}
		else if (node instanceof TypeArrayNode)
		{
			print(((TypeArrayNode) node).getParent());
			System.out.print('[');
			if (((TypeArrayNode) node).getRequirements() != null)
			{
				for (int[] range : ((TypeArrayNode) node).getRequirements())
				{
					if (range[1] == Integer.MAX_VALUE)
					{
						System.out.print(range[0] + "~");
					}
					else if (range[0] == range[1])
					{
						System.out.print(range[0]);
					}
					else
					{
						System.out.print(range[0] + "~" + range[1]);
					}
					System.out.print('|');
				}
			}
			System.out.print(']');
		}
		else throw new InternalError();
	}
	
	private void printArray(TypeNode[] nodes)
	{
		printArray(", ", nodes);
	}
	
	private void printArray(String joiner, TypeNode[] nodes)
	{
		switch (nodes.length)
		{
		case 1 : print(nodes[0]);
		case 0 : break;
		default:
			print(nodes[0]);
			for (int i = 1; i < nodes.length; ++i)
			{
				System.out.print(joiner);
				print(nodes[i]);
			}
			break;
		}
	}
	
	@Override
	public void visitExprNode(ExprNode node)
	{
		print(node);
	}
	
	private void print(ExprNode node)
	{
		if (node instanceof IdentNode)
		{
			print((IdentNode) node);
		}
		else if (node instanceof IdentToNode)
		{
			print(((IdentToNode) node).getParent());
			System.out.print('.');
			print(((IdentToNode) node).getChild());
		}
		else if (node instanceof FullExprNode)
		{
			printArray(((FullExprNode) node).children());
		}
		else if (node instanceof NestExprNode)
		{
			System.out.print(((NestExprNode) node).isLeft() ? '[' : '(');
			print(((NestExprNode) node).getChild());
			System.out.print(((NestExprNode) node).isRight() ? ']' : ')');
		}
		else if (node instanceof MethodExprNode)
		{
			print(((MethodExprNode) node).getParent());
			print(((MethodExprNode) node).getParameters());
		}
		else if (node instanceof OpBinaryNode)
		{
			print(((OpBinaryNode) node).getLeft());
			System.out.print(' ');
			print(((OpBinaryNode) node).getOperator());
			System.out.print(' ');
			print(((OpBinaryNode) node).getRight());
		}
		else if (node instanceof OpUnaryNode)
		{
			print(((OpUnaryNode) node).getOperator());
			print(((OpUnaryNode) node).getRight());
		}
		else if (node instanceof ConstNode)
		{
			System.out.print(((ConstNode) node).getConstant());
		}
	}
	
	private void print(IdentNode node)
	{
		System.out.print(node.getName());
		if (node.getParameters() != null)
		{
			System.out.print("<|");
			printArray(node.getParameters());
			System.out.print("|>");
		}
	}
	
	private void printArray(List<? extends ExprNode> nodes)
	{
		switch (nodes.size())
		{
		case 1 : print(nodes.get(0));
		case 0 : break;
		default:
			Iterator<? extends ExprNode> itr = nodes.iterator();
			print(itr.next());
			while (itr.hasNext())
			{
				System.out.print(", ");
				print(itr.next());
			}
			break;
		}
	}
}
