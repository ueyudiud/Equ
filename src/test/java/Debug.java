/*
 * copyright© 2017 ueyudiud
 */

import org.objectweb.asm.Opcodes;

/**
 * @author ueyudiud
 */
public class Debug implements Opcodes
{
	public static void main(String[] args) throws Throwable
	{
		final int[] map = {0, 1, 4, 4, 5};
		for (int idx = 0; idx < 10; ++idx)
		{
			int low = 0;
			int high = map.length - 1;
			while (low < high)
			{
				int mid = ((low + high + 1) >>> 1);
				int midVal = map[mid];
				if (midVal > idx)
				{
					if (low == mid - 1)
						break;
					high = mid - 1;
				}
				else if (midVal < idx)
					low = mid;
				else
				{
					low = mid;
					break;
				}
			}
			System.out.println(map[low]);
		}
		//		FileReader reader = new FileReader("D:\\Program Files\\EquJ\\Equ\\test\\A.fls");
		//		char[] buf = IOUtil.readFully(reader);
		//		ReaderPreoperator preoperator = new ReaderPreoperator(new FileReader("D:\\Program Files\\EquJ\\Equ\\test\\A.fls"));
		//		StreamLexer lexer = new StreamLexer(preoperator.preoperate());
		//		lexer.lexeralize().forEach(System.out::println);
		//		System.exit(0);
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