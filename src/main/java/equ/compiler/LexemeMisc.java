/*
 * copyright© 2018 ueyudiud
 */
package equ.compiler;

/**
 * @author ueyudiud
 */
public class LexemeMisc extends LexemeAbstract
{
	public static ILexeme leftPanc(SourceTrace trace) { return new LexemeMisc(EnumLexType.LPA, "(", trace); }
	public static ILexeme leftBrace(SourceTrace trace) { return new LexemeMisc(EnumLexType.LBR, "[", trace); }
	public static ILexeme leftBracket(SourceTrace trace) { return new LexemeMisc(EnumLexType.LBK, "{", trace); }
	public static ILexeme leftNaming(SourceTrace trace) { return new LexemeMisc(EnumLexType.LNA, "<|", trace); }
	public static ILexeme leftTemplate(SourceTrace trace) { return new LexemeMisc(EnumLexType.LTM, "<[", trace); }
	public static ILexeme rightPanc(SourceTrace trace) { return new LexemeMisc(EnumLexType.RPA, ")", trace); }
	public static ILexeme rightBrace(SourceTrace trace) { return new LexemeMisc(EnumLexType.RBR, "]", trace); }
	public static ILexeme rightBracket(SourceTrace trace) { return new LexemeMisc(EnumLexType.RBK, "}", trace); }
	public static ILexeme rightNaming(SourceTrace trace) { return new LexemeMisc(EnumLexType.RNA, "|>", trace); }
	public static ILexeme rightTemplate(SourceTrace trace) { return new LexemeMisc(EnumLexType.RTM, "]>", trace); }
	public static ILexeme at(SourceTrace trace) { return new LexemeMisc(EnumLexType.AT, "@", trace); }
	public static ILexeme dot(SourceTrace trace) { return new LexemeMisc(EnumLexType.DOT, ".", trace); }
	public static ILexeme semi(SourceTrace trace) { return new LexemeMisc(EnumLexType.SEM, ";", trace); }
	public static ILexeme comma(SourceTrace trace) { return new LexemeMisc(EnumLexType.COM, ",", trace); }
	public static ILexeme newLine(SourceTrace trace) { return new LexemeMisc(EnumLexType.NL, "\r\n", trace); }
	public static ILexeme end(SourceTrace trace) { return new LexemeMisc(EnumLexType.END, "", trace); }
	public static ILexeme macroEnding(SourceTrace trace) { return new LexemeMisc(EnumLexType.MAE, "", trace); }
	public static ILexeme literalNull(SourceTrace trace) { return new LexemeMisc(EnumLexType.LIN, "null", trace); }
	
	private final EnumLexType type;
	private final String name;
	
	private LexemeMisc(EnumLexType type, String name, SourceTrace trace)
	{
		super(trace);
		this.type = type;
		this.name = name;
	}
	
	@Override
	public EnumLexType type()
	{
		return type;
	}
	
	@Override
	public String display()
	{
		return name;
	}
}
