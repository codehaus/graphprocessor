package org.codehaus.graphprocessor.impl;

import org.codehaus.graphprocessor.PropertyContext;
import org.codehaus.graphprocessor.PropertyProcessor;


public abstract class AbstractPropertyProcessor implements PropertyProcessor
{
	@Override
	public void process(PropertyContext pCtx, Object source, Object target)
	{
		// instance check
		if (!(pCtx instanceof PropertyContextImpl))
		{
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " needs an instance of "
					+ PropertyContextImpl.class.getName() + " to work properly");
		}

		// cast
		final PropertyContextImpl pCtxImpl = (PropertyContextImpl) pCtx;

		process(pCtxImpl, source, target);
	}

	protected abstract void process(PropertyContextImpl pCtx, Object source, Object target);

}
