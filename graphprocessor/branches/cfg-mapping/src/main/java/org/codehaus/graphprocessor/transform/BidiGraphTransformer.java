package org.codehaus.graphprocessor.transform;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.bidi.BidiGraphProcessor;
import org.codehaus.graphprocessor.bidi.BidiNodeProcessor;
import org.codehaus.graphprocessor.bidi.impl.BidiCollectionNodeConfig;
import org.codehaus.graphprocessor.impl.AbstractGraphConfig;
import org.codehaus.graphprocessor.impl.BidiGraphProcessingUnit;
import org.codehaus.graphprocessor.impl.GraphConfigurationImpl;
import org.codehaus.graphprocessor.impl.GraphContextImpl;



public class BidiGraphTransformer extends AbstractGraphConfig implements GraphConfig, GraphTransformer
{

	private static final Logger LOG = Logger.getLogger(BidiGraphTransformer.class);

	private final BidiGraphProcessor processor = new BidiGraphProcessor();
	private BidiGraphProcessingUnit processingUnit;

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
		return this.processor.process(ctx, source, target);
		// a source graph must be specified
		if (source == null)
		{
			throw new GraphException("No source graph to transform [null]", new NullPointerException());
		}

		if (processingUnit instanceof Initial !this.isInitialized())
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



	public BidiGraphContext createGraphContext()
	{
		return this.createSourceContext();
	}

	public BidiGraphContext createGraphContext(Class node)
	{
		if (node == null)
		{
			throw new GraphException("Can't create " + BidiGraphContext.class.getSimpleName() + " (no node type available)",
					new NullPointerException());
		}
		BidiGraphContext result = null;

		if (!this.isInitialized())
		{
			this.initialize(0);
		}

		BidiNodeConfig nodeConfig = getAssignableNodeConfig(node);

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
			LOG.debug("Successfully created " + BidiGraphContext.class.getSimpleName() + " for " + node.getSimpleName());
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
