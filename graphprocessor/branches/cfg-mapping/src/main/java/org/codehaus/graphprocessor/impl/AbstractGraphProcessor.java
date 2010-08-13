package org.codehaus.graphprocessor.impl;

import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.GraphProcessor;


public abstract class AbstractGraphProcessor implements GraphProcessor
{

	@Override
	public <T> T process(final GraphContext graphCtx, final Object source, final T target)
	{
		if (!(graphCtx instanceof GraphContextImpl))
		{
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " needs an instance of "
					+ GraphContextImpl.class.getName() + " to work properly");

		}

		final GraphContextImpl graphCtxImpl = (GraphContextImpl) graphCtx;
		return this.process(graphCtxImpl, source, target);
	}

	protected abstract <T> T process(final GraphContextImpl graphCtx, final Object source, final T target);

}
