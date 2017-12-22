/*
 * copyright© 2017 ueyudiud
 */

import org.objectweb.asm.Opcodes;

import equ.compiler.coder.CodingFormats;
import equ.compiler.parser.Parser;
import equ.compiler.parser.Scanner;

/**
 * @author ueyudiud
 */
public class Debug implements Opcodes
{
	public static void main(String[] args) throws Throwable
	{
		//		FileReader reader = new FileReader("D:\\Program Files\\EquJ\\Equ\\test\\A.fls");
		//		char[] buf = IOUtil.readFully(reader);
		Scanner scanner = new Scanner("String[1|3~]".toCharArray(), null);
		Parser parser = new Parser(scanner);
		System.out.println(CodingFormats.toString(parser.type()));
		//		ReaderPreoperator preoperator = new ReaderPreoperator(new FileReader("D:\\Program Files\\EquJ\\Equ\\test\\A.fls"));
		//		StreamLexer lexer = new StreamLexer(preoperator.preoperate());
		//		lexer.lexeralize().forEach(System.out::println);
		//		System.exit(0);
		//		SClassLoader loader = STypeLoaders.simple(true);
		//		SClass class1 = loader.loadClass("java.util.List");
		//		System.out.println(Arrays.toString(
		//				GS.transform(STypes.INT.getFunctions(), SFunc::toString, String.class)));
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