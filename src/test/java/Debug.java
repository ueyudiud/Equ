/*
 * copyright© 2018 ueyudiud
 */

import java.io.File;

import equ.compiler.FileCompileFactory;

/**
 * @author ueyudiud
 */
public class Debug
{
	public static void main(String[] args) throws Throwable
	{
		FileCompileFactory factory = new FileCompileFactory();
		File file = new File("D:\\Program Files\\EquJ\\Equ\\test");
		factory.compile(new File(file, "include"), new File(file, "lib"));
		System.exit(0);
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