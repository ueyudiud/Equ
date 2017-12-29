/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author ueyudiud
 */
public interface IScannerFactory
{
	@Nonnull IScanner openSource(@Nullable IScanner scanner, @Nonnull String path) throws IOException;
	
	void closeScanner(@Nonnull IScanner scanner);
	
	void warn(@Nonnull IScanner scanner, @Nonnull String cause, Object... formats);
	
	void error(@Nonnull IScanner scanner, @Nonnull String cause, Object... formats);
}
