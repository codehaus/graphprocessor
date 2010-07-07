package org.codehaus.graphprocessor.transform;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractNodeConfig;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphProcessor;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.bidi.BidiCollectionNodeConfig;
import org.codehaus.graphprocessor.bidi.DefaultBidiGraphConfig;
import org.codehaus.graphprocessor.impl.CachedClassLookupMap;
import org.codehaus.graphprocessor.impl.GraphConfigurationImpl;
import org.codehaus.graphprocessor.impl.GraphContextImpl;



public class BidiGraphTransformer extends DefaultBidiGraphConfig implements GraphProcessor, GraphTransformer
{

	private static final Logger LOG = Logger.getLogger(BidiGraphTransformer.class);

	public BidiGraphTransformer()
	{
		super();
	}

	public BidiGraphTransformer(Class rootNode)
	{
		super(rootNode);
		initialize(0);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.bidi.BidiGraphConfig#initialize()
	 */
	@Override
	public boolean initialize(int complianceLevel)
	{
		// add custom nodes
		addNode(new BidiCollectionNodeConfig(this));

		// initialize nodes
		return super.initialize(complianceLevel);
	}

	@Override
	public <T> T transform(final GraphContext ctx, final Object source)
	{
		return (T) transform(ctx, source, null);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphTransformer#transform(java.lang.Object)
	 */
	@Override
	public <T extends Object> T transform(final Object source)
	{
		return (T) transform(null, source, null);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphTransformer#transform(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public <T extends Object> T transform(final Object source, final T target)
	{
		return transform(null, source, target);
	}


	@Override
	public <T> T transform(GraphContext ctx, final Object source, final T target)
	{
		// a source graph must be specified
		if (source == null)
		{
			throw new GraphException("No source graph to transform [null]", new NullPointerException());
		}

		if (!this.isInitialized())
		{
			this.initialize(0);
		}

		// if no context is passed, a default one gets created
		if (ctx == null)
		{
			ctx = this.createGraphContext(source.getClass());

			if (ctx == null)
			{
				throw new GraphException("Can't create a GraphContext");
			}
		}

		final T result = this.process(ctx, source, target);

		return result;
	}


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
		GraphConfig graphConfig = graphCtx.getGraphConfig();
		if (graphConfig instanceof Initializable)
		{
			Initializable init = (Initializable) graphConfig;
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
		GraphConfigurationImpl graphConfigImpl = (GraphConfigurationImpl) graphCtx.getConfiguration();
		final CachedClassLookupMap<NodeConfig> nodeLookup = graphConfigImpl.getAllNodeConfigs(0);
		final NodeConfig nodeConfig = nodeLookup.get(source.getClass());

		if (nodeConfig == null)
		{
			throw new GraphException("Can't find a " + NodeConfig.class.getSimpleName() + " for " + source.getClass());
		}

		// create nodeLookup used for root nodes childs
		final NodeContext nodeCtx = graphCtx.createRootNodeContext(nodeLookup, (AbstractNodeConfig) nodeConfig, source);

		NodeProcessor nodeProcessor = nodeConfig.getProcessor();
		final T result = nodeProcessor.process(nodeCtx, source, target);
		graphCtx.setReleased(true);

		return result;
	}

	public GraphContext createGraphContext()
	{
		return this.createSourceContext();
	}

	public GraphContext createGraphContext(Class node)
	{
		if (node == null)
		{
			throw new GraphException("Can't create " + GraphContext.class.getSimpleName() + " (no node type available)",
					new NullPointerException());
		}
		GraphContext result = null;

		if (!this.isInitialized())
		{
			this.initialize(0);
		}

		NodeConfig nodeConfig = getAssignableNodeConfig(node);

		if (nodeConfig != null)
		{
			result = this.createSourceContext();
		}
		else
		{
			if (getTargetConfig().getAssignableNodeConfig(node) != null)
			{
				result = this.createTargetContext();
			}
		}

		if (result == null)
		{
			LOG.debug("Successfully created " + GraphContext.class.getSimpleName() + " for " + node.getSimpleName());
		}

		return result;

	}


	public GraphContext createSourceContext()
	{
		return new GraphContextImpl(this);
	}

	public GraphContext createTargetContext()
	{
		return new GraphContextImpl(this.getTargetConfig());
	}



}
