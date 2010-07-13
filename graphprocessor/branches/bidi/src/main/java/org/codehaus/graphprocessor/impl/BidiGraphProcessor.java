package org.codehaus.graphprocessor.impl;

import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphProcessor;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.bidi.AbstractBidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiGraphContext;
import org.codehaus.graphprocessor.bidi.BidiNodeContext;


public class BidiGraphProcessor implements GraphProcessor
{

	@Override
	public <T> T process(final BidiGraphContext graphCtx, final Object source, final T target)
	{
		if (!(graphCtx instanceof GraphContextImpl))
		{
			throw new UnsupportedOperationException(this.getClass().getSimpleName() + " needs an instance of "
					+ GraphContextImpl.class.getName() + " to work properly");

		}

		final GraphContextImpl graphCtxImpl = (GraphContextImpl) graphCtx;

		return this.process(graphCtxImpl, source, target);
	}

	public <T> T process(final GraphContextImpl graphCtx, final Object source, final T target)
	{
		// TODO: typecheck?
		BidiGraphConfig graphCfg = graphCtx.getGraphConfig();
		if (graphCfg instanceof Initializable)
		{
			Initializable init = (Initializable) graphCfg;
			if (!init.isInitialized())
			{
				init.initialize(0);
			}
		}

		if (graphCtx.isReleased())
		{
			throw new GraphException("Can't use an instance of " + BidiGraphContext.class.getSimpleName() + " twice");
		}

		// create nodeLookup to lookup root node
		final CachedClassLookupMap<BidiNodeConfig> nodeLookup = ((GraphConfigurationImpl) graphCtx.getConfiguration())
				.getAllNodeConfigs(0);
		final BidiNodeConfig nodeMapping = nodeLookup.get(source.getClass());

		if (nodeMapping == null)
		{
			throw new GraphException("Can't find a " + BidiNodeConfig.class.getSimpleName() + " for " + source.getClass());
		}

		// create nodeLookup used for root nodes childs
		final BidiNodeContext nodeCtx = graphCtx.createRootNodeContext(nodeLookup, (AbstractBidiNodeConfig) nodeMapping, source);

		final T result = nodeMapping.getProcessor().process(nodeCtx, source, target);
		graphCtx.setReleased(true);

		return result;


	}

}
