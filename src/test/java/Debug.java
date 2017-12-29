/*
 * copyright© 2017 ueyudiud
 */

import org.objectweb.asm.Opcodes;

import equ.compiler.IScanner;
import equ.compiler.IScannerFactory;
import equ.compiler.ScannerFactories;

/**
 * @author ueyudiud
 */
public class Debug implements Opcodes
{
	public static void main(String[] args) throws Throwable
	{
		IScannerFactory factory = ScannerFactories.fileFactory("D:\\Program Files\\EquJ\\Equ\\test");
		IScanner scanner = factory.openSource(null, "A.fls");
		label: for (;;)
		{
			scanner.scan();
			switch (scanner.type())
			{
			case END :
				break label;
			case IDENTIFIER :
			case OPERATOR :
				System.out.println(scanner.ident().name);
				break;
			case COMMENT_BLOCK :
			case COMMENT_DOC :
			case COMMENT_LINE :
				System.out.println(scanner.com().comments);
				break;
			case LITERAL :
				System.out.println(scanner.lit().value);
				break;
			default:
				System.out.println(scanner.type());
				break;
			}
		}
		factory.closeScanner(scanner);
		//		FileReader reader = new FileReader("D:\\Program Files\\EquJ\\Equ\\test\\A.fls");
		//		char[] buf = IOUtil.readFully(reader);
		//		ReaderPreoperator preoperator = new ReaderPreoperator(new FileReader("D:\\Program Files\\EquJ\\Equ\\test\\A.fls"));
		//		StreamLexer lexer = new StreamLexer(preoperator.preoperate());
		//		lexer.lexeralize().forEach(System.out::println);
		System.exit(0);
		//		SClassLoader loader = STypeLoaders.simple(true);
		//		SClass class1 = loader.loadClass("java.util.List");
		//		System.out.println(Arrays.toString(
		//		GS.transform(STypes.INT.getFunctions(), SFunc::toString, String.class)));
	}
}

final class DebugClassLoader extends ClassLoader
{
	static final DebugClassLoader INSTANCE = new DebugClassLoader();
	
	static Class<?> parseClass(byte[] codes, String name)
	{
		return INSTANCE.defineClass(name, codes, 0, codes.length);
	}
}