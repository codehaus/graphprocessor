package org.codehaus.graphprocessor.bidi;

import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeFactory;
import org.codehaus.graphprocessor.impl.BidiNodeProcessor;
import org.codehaus.graphprocessor.single.DefaultNodeConfig;
import org.codehaus.graphprocessor.single.DefaultPropertyConfig;



public class DefaultBidiNodeConfig extends DefaultNodeConfig implements BidiNodeConfig
{
	private static final Logger log = Logger.getLogger(DefaultBidiNodeConfig.class);

	private static final BidiNodeProcessor DEFAULT_NODE_PROCESSOR = new BidiNodeProcessor();

	private DefaultBidiNodeConfig targetNode = null;

	/**
	 * Create a bidirectional {@link NodeConfig} which is bound to a {@link BidiGraphConfig} and a node type which is taken as
	 * source node.
	 * @param graphConfig
	 * @param type
	 */
	public DefaultBidiNodeConfig(final BidiGraphConfig graphConfig, final Class<?> type)
	{
		super(graphConfig, type);

		if (type.isAnnotationPresent(GraphNode.class))
		{
			final GraphNode nodeAnno = type.getAnnotation(GraphNode.class);

			if (nodeAnno.target() != null)
			{
				final Class<?> targetNode = nodeAnno.target();
				this.targetNode = new DefaultBidiNodeConfig(graphConfig.getTargetConfig(), targetNode, this);
			}
		}
		else
		{
			this.targetNode = new DefaultBidiNodeConfig(graphConfig.getTargetConfig(), type, this);
		}

		this.setDefaults();
		this.initMetaData(type);
	}

	public DefaultBidiNodeConfig(final BidiGraphConfig graphConfig, final Class<?> sourceNode, Class<?> targetNode)
	{
		super(graphConfig, sourceNode);

		if (targetNode == null)
		{

			if (sourceNode.isAnnotationPresent(GraphNode.class))
			{
				final GraphNode nodeAnno = sourceNode.getAnnotation(GraphNode.class);

				if (nodeAnno.target() != null)
				{
					targetNode = nodeAnno.target();
					// this.targetNode = new DefaultBidiNodeConfig(graphConfig.getTargetConfig(), targetNode, this);
				}
			}
			else
			{
				// target is of same type like source node type
				// this.targetNode = new DefaultBidiNodeConfig(graphConfig.getTargetConfig(), sourceNode, this);
				targetNode = sourceNode;
			}
		}

		this.targetNode = new DefaultBidiNodeConfig(graphConfig.getTargetConfig(), targetNode, this);

		this.setDefaults();
		this.initMetaData(targetNode);
	}


	protected DefaultBidiNodeConfig(final BidiGraphConfig graphConfig)
	{
		super(graphConfig);
	}

	/**
	 * Private Constructor.
	 * <p/>
	 * Used to create a circular reference between two NodeConfig instances.
	 * @param graphConfig
	 * @param type
	 * @param target
	 */
	private DefaultBidiNodeConfig(final BidiGraphConfig graphConfig, final Class<?> type, final DefaultBidiNodeConfig target)
	{
		super(graphConfig, type);
		this.targetNode = target;

		if (type.isAnnotationPresent(GraphNode.class))
		{
			final GraphNode nodeAnno = type.getAnnotation(GraphNode.class);

			if (nodeAnno.target() != null)
			{
				final Class<?> targetNode = nodeAnno.target();
				if (!targetNode.equals(target.getType()))
				{
					log.warn("Node mapping mismatch: " + target.getType().getSimpleName() + " is annotated with "
							+ type.getSimpleName() + "; but " + type.getSimpleName() + " is annotated with "
							+ nodeAnno.target().getSimpleName() + " as target node");

				}
			}
		}
		this.setDefaults();
	}

	@Override
	protected void setDefaults()
	{
		super.setDefaults();
		setProcessor(DEFAULT_NODE_PROCESSOR);
	}

	public BidiNodeConfig getTargetNodeConfig()
	{
		return this.targetNode;
	}


	@Override
	public BidiGraphConfig getGraphConfig()
	{
		return (BidiGraphConfig) super.getGraphConfig();
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.DefaultNodeConfig#createPropertyConfig(java.lang
	 * .String)
	 */
	@Override
	protected DefaultPropertyConfig createPropertyConfig(final String id)
	{
		final DefaultBidiPropertyConfig result = new DefaultBidiPropertyConfig(this, id);
		if (result.getTargetProperty().getReadMethod() == null && result.getTargetProperty().getWriteMethod() == null)
		{
			result.setTargetProperty(null);
		}
		return result;
	}



	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.AbstractNodeConfig#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled()
	{
		final Set<String> debugNodes = getGraphConfig().getDebugNodes();
		final boolean enabled = debugNodes.isEmpty() || debugNodes.contains(this.getType().getSimpleName())
				|| debugNodes.contains(this.targetNode.getType().getSimpleName());
		return enabled;
	}


	private NodeFactory nodeFactory = null;

	/**
	 * @return the nodeFactory
	 */
	public NodeFactory getNodeFactory()
	{
		return nodeFactory;
	}

	/**
	 * @param nodeFactory
	 *           the nodeFactory to set
	 */
	public void setNodeFactory(NodeFactory nodeFactory)
	{
		this.nodeFactory = nodeFactory;
	}

	@Override
	public BidiPropertyConfig getPropertyConfigByName(String source)
	{
		return getPropertyConfigByName(source, source);
	}

	@Override
	public BidiPropertyConfig getPropertyConfigByName(String source, String target)
	{
		return (BidiPropertyConfig) getProperties().get(source + "-" + target);
	}

	@Override
	public BidiPropertyConfig removePropertyConfigByName(String propertyId)
	{
		return removePropertyConfigByName(propertyId, propertyId);
	}

	@Override
	public BidiPropertyConfig removePropertyConfigByName(String propertyName, String targetPropName)
	{
		return (BidiPropertyConfig) super.removePropertyConfigByName(propertyName + "-" + targetPropName);
	}

	private void initMetaData(Class type)
	{
		if (type.isAnnotationPresent(GraphNode.class))
		{
			final GraphNode nodeAnno;
			try
			{
				nodeAnno = (GraphNode) type.getAnnotation(GraphNode.class);

				if (!nodeAnno.factory().equals(NodeFactory.class))
				{
					NodeFactory factory = this.getInstance(nodeAnno.factory());
					setNodeFactory(factory);
				}

			}
			catch (Exception e)
			{
				log.error("Error getting " + GraphNode.class.getSimpleName() + " annotation", e);
			}
		}
	}

	private <T> T getInstance(Class clazz)
	{

		T result = null;
		try
		{
			result = (T) clazz.newInstance();
		}
		catch (Exception e)
		{
			log.error(e.getMessage());

		}
		return result;
	}

}
