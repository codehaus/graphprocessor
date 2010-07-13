package org.codehaus.graphprocessor.bidi;


public interface GraphProcessor
{
	<T extends Object> T process(final BidiGraphContext graphCtx, final Object source, T target);
}
