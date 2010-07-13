package org.codehaus.graphprocessor;

import org.codehaus.graphprocessor.bidi.BidiGraphContext;

public interface GraphProcessor
{
	<T extends Object> T process(final BidiGraphContext graphCtx, final Object source, T target);
}
