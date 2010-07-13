package org.codehaus.graphprocessor.bidi;


public interface BidiGraphProcessor
{
	<T extends Object> T process(final BidiGraphContext graphCtx, final Object source, T target);
}
