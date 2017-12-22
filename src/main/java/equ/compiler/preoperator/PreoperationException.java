/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import equ.compiler.util.ISourceInfo;
import equ.compiler.util.ISourceInfoProvider;

/**
 * @author ueyudiud
 */
public class PreoperationException extends Exception
{
	private static final long serialVersionUID = -7774032460059228810L;
	
	private static String provideSourceMsg(ISourceInfoProvider provider)
	{
		if (provider == null)
			return "\r(unknown resource)";
		ISourceInfo info = provider.applyInfo();
		return info == null ? "\\r(illegal info)" : info.toSourceInfo();
	}
	
	public PreoperationException(String msg, @Nullable ISourceInfoProvider provider)
	{
		super(msg + '\r' + provideSourceMsg(provider));
	}
	
	public PreoperationException(String msg, @Nonnull ISourceInfo info)
	{
		super(msg + '\r' + info.toSourceInfo());
	}
}
