/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import equ.compiler.util.ILiteralFactory;
import equ.compiler.util.Literal;
import equ.util.IOUtil;

/**
 * @author ueyudiud
 */
public abstract class FileBasedParsingFactory implements IParsingFactory, ILiteralFactory
{
	private final File root;
	private final Map<IScanner, File> map = new HashMap<>();
	
	public FileBasedParsingFactory(File root)
	{
		this.root = root;
	}
	
	@Override
	public IScanner openSource(IScanner source, boolean lib, String path) throws IOException
	{
		File location = this.map.getOrDefault(source, this.root);
		char[] data = IOUtil.readFully(new FileReader(new File(location.getParentFile(), path + ".fhs")));
		Scanner scanner = new Scanner(data, this);
		this.map.put(scanner, location);
		return scanner;
	}
	
	@Override
	public void cycledScanner(IScanner scanner)
	{
		this.map.remove(scanner);
	}
	
	@Override
	public Literal parseHexLiteral(String number, String suffix)
	{
		return null;
	}
	
	@Override
	public Literal parseOctLiteral(String number, String suffix)
	{
		return null;
	}
	
	@Override
	public Literal parseDecLiteral(String number, String suffix)
	{
		return null;
	}
	
	@Override
	public Literal parseCustomLiteral(String value)
	{
		return null;
	}
}
