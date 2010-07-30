package org.codehaus.graphprocessor.bidi;

import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphProcessor;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.impl.AbstractNodeConfig;
import org.codehaus.graphprocessor.impl.GraphConfigurationImpl;
import org.codehaus.graphprocessor.impl.GraphContextImpl;


public class BidiGraphProcessor implements GraphProcessor
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

	public <T> T process(final GraphContextImpl graphCtx, final Object source, final T target)
	{
		// TODO: typecheck?
		GraphConfig graphCfg = graphCtx.getGraphConfig();
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
			throw new GraphException("Can't use an instance of " + GraphContext.class.getSimpleName() + " twice");
		}

		// create nodeLookup to lookup root node
		final CachedClassLookupMap<NodeConfig> nodeLookup = ((GraphConfigurationImpl) graphCtx.getConfiguration())
				.getAllNodeConfigs(0);
		final NodeConfig nodeMapping = nodeLookup.get(source.getClass());

		if (nodeMapping == null)
		{
			throw new GraphException("Can't find a " + NodeConfig.class.getSimpleName() + " for " + source.getClass());
		}

		// create nodeLookup used for root nodes childs
		final NodeContext nodeCtx = graphCtx.createRootNodeContext(nodeLookup, (AbstractNodeConfig) nodeMapping, source);
		NodeProcessor processor = nodeCtx.getProcessingUnit().getProcessor();

		final T result = processor.process(nodeCtx, source, target);
		graphCtx.setReleased(true);

		return result;


	}

}
