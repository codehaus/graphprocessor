package org.codehaus.graphprocessor;

public interface GraphProcessor
{
	<T extends Object> T process(final GraphContext graphCtx, final Object source, T target);
}
