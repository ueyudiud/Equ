/*
 * copyright© 2017 ueyudiud
 */
package equ.compiler.preoperator;

import java.io.IOException;
import java.util.stream.Stream;

import equ.compiler.util.ISourceInfoProvider;

/**
 * @author ueyudiud
 */
public interface Preoperator extends ISourceInfoProvider
{
	Stream<?> preoperate() throws IOException, PreoperationException;
	
	void appendTransformed(Object value);
	
	void addReplacement(String key, StringReplacement replacement) throws PreoperationException;
	
	void removeReplacement(String key) throws PreoperationException;
	
	boolean isLineReading();
}
