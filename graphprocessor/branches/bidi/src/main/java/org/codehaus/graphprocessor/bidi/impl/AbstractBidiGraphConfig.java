package org.codehaus.graphprocessor.bidi.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.ContextCreatedListener;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiGraphContext;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeContext;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;
import org.codehaus.graphprocessor.bidi.NodeProcessor;
import org.codehaus.graphprocessor.bidi.PropertyProcessor;
import org.codehaus.graphprocessor.impl.CachedClassLookupMap;




public abstract class AbstractBidiGraphConfig implements BidiGraphConfig, Initializable, ContextCreatedListener
{
	private static final Logger log = Logger.getLogger(AbstractBidiGraphConfig.class);

	private boolean _isInitialized = false;
	private ContextCreatedListener listener = null;

	protected final CachedClassLookupMap<BidiNodeConfig> nodeLookupMap;
	private final Map<Class<?>, BidiNodeConfig> inmutableNodeLookup;

	protected final CachedClassLookupMap<NodeProcessor> nodeProcessorMap;
	protected final CachedClassLookupMap<PropertyProcessor> propertyProcessorMap;


	public AbstractBidiGraphConfig()
	{
		this.listener = this;
		this.nodeLookupMap = new CachedClassLookupMap<BidiNodeConfig>();
		this.inmutableNodeLookup = Collections.unmodifiableMap(this.nodeLookupMap.getStaticMap());

		this.nodeProcessorMap = new CachedClassLookupMap<NodeProcessor>();
		this.propertyProcessorMap = new CachedClassLookupMap<PropertyProcessor>();

		nodeProcessorMap.put(Collection.class, new CollectionNodeProcessor());
		nodeProcessorMap.put(Object.class, null);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphConfig#getNodeProcessor(java.lang.Class)
	 */
	@Override
	public NodeProcessor getDefaultNodeProcessor(Class nodeType)
	{
		return nodeProcessorMap.get(nodeType);
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphConfig#getPropertyProcessor(java.lang.Class)
	 */
	@Override
	public PropertyProcessor getDefaultPropertyProcessor(Class propertyType)
	{
		return propertyProcessorMap.get(propertyType);
	}

	public boolean isInitialized()
	{
		return this._isInitialized;
	}

	protected void setInitialized(boolean isInitialized)
	{
		this._isInitialized = isInitialized;
	}

	public boolean initialize(int complianceLevel)
	{
		this.initializeAllNodes();
		return true;
	}

	public void initializeAllNodes()
	{
		for (final BidiNodeConfig nodeCfg : getNodes().values())
		{
			if (nodeCfg instanceof Initializable)
			{
				((Initializable) nodeCfg).initialize(0);
			}

		}
	}


	public void setContextListener(ContextCreatedListener listener)
	{
		this.listener = listener;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphConfig#getNodes()
	 */
	@Override
	public Map<Class<?>, BidiNodeConfig> getNodes()
	{
		// return (Map) this.nodeLookupMap.getStaticMap();
		return this.inmutableNodeLookup;
	}

	@Override
	public BidiNodeConfig getNodeConfig(final Class node)
	{
		return getNodes().get(node);
	}

	@Override
	public BidiNodeConfig getAssignableNodeConfig(Class nodeType)
	{
		return this.nodeLookupMap.get(nodeType);
	}

	public void addNode(final BidiNodeConfig nodeConfig)
	{
		this.nodeLookupMap.put(nodeConfig.getType(), nodeConfig);
	}

	public BidiNodeConfig addNode(final Class node)
	{
		final BidiNodeConfig result = this.createNodeConfig(node);
		this.addNode(result);
		return result;
	}


	@Override
	public void addNodes(final Class nodeClass)
	{

		// which is annotated as graph
		if (!nodeClass.isAnnotationPresent(GraphNode.class))
		{
			throw new GraphException(nodeClass.getName() + " is not annotated with " + GraphNode.class.getSimpleName());
		}

		final Map<Class, BidiNodeConfig> result = new LinkedHashMap<Class, BidiNodeConfig>();

		log.debug("Start introspecting (sub)graph " + nodeClass + "...");

		final int size = result.size();
		this.findAndAddChildNodes(nodeClass, result);

		if (log.isDebugEnabled())
		{
			final int newNodes = result.size() - size;
			log.debug("... finished introspecting (sub)graph " + nodeClass + "; detected " + newNodes + " new nodes");
		}

	}

	public void addNodes(final Collection<Class> nodes)
	{
		for (final Class nodeClass : nodes)
		{
			this.addNodes(nodeClass);
		}
	}


	/**
	 * @param node
	 *           start node
	 */
	private void findAndAddChildNodes(final Class node, final Map<Class, BidiNodeConfig> newNodes)
	{
		// only process node type if not already done
		if (!this.getNodes().containsKey(node))
		{
			// lookup for graph annotation
			if (node.isAnnotationPresent(GraphNode.class))
			{
				// get annotation with node configurations
				final GraphNode graphNode = (GraphNode) node.getAnnotation(GraphNode.class);

				// create a NodeConfig
				final BidiNodeConfig cfg = this.addNode(node);
				newNodes.put(node, cfg);

				// Recursive processing of child nodes
				// child nodes are detected in different strategies ...

				// strategy 1: child nodes are taken from annotation GraphNnode#addNodes
				// default value is "void" which means: nothing set, no child nodes
				if (!graphNode.addNodes()[0].equals(void.class))
				{
					for (final Class<?> childNode : graphNode.addNodes())
					{
						// recursive call with current child node candidate
						this.findAndAddChildNodes(childNode, newNodes);
					}
				}

				// strategy 2: child nodes are detected from available property types (getters)
				// (with support for typed collections)
				final Collection<BidiPropertyConfig> pCfgList = cfg.getProperties().values();
				for (final BidiPropertyConfig pCfg : pCfgList)
				{
					Class<?> childNodeCandidate = pCfg.getReadType();

					// if type is collection:
					// try to find out the collection type and update child node candiate
					if (Collection.class.isAssignableFrom(childNodeCandidate))
					{
						// works only if collection is typed
						final Type type = pCfg.getReadMethod().getGenericReturnType();
						if (type instanceof ParameterizedType)
						{
							final Type[] types = ((ParameterizedType) type).getActualTypeArguments();
							// don't know whether any other value than '1' can ever occur
							if (types.length == 1)
							{
								Type _type = types[0];

								// extract class-type from wildcard-type
								if (types[0] instanceof WildcardType)
								{
									final WildcardType wType = ((WildcardType) types[0]);

									// currently only upper-bounds are supported (? extends type)
									if (wType.getUpperBounds().length == 1)
									{
										_type = wType.getUpperBounds()[0];
									}
								}

								// any other type than 'class' gets skipped
								if (_type instanceof Class)
								{
									childNodeCandidate = (Class) _type;
								}
							}
						}
					}

					// recursive call with current child node candidate
					findAndAddChildNodes(childNodeCandidate, newNodes);
				}
			}
		}
	}

	@Override
	public Set<String> getDebugNodes()
	{
		return Collections.EMPTY_SET;
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphConfig#getContextListener()
	 */
	@Override
	public ContextCreatedListener getContextListener()
	{
		return this.listener;
	}

	@Override
	public void graphContextCreated(BidiGraphContext graphContext)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void nodeContextCreated(BidiNodeContext nodeContext)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void propertyContextCreated(BidiPropertyContext propertyContext)
	{
		// NOP
	}

	@Override
	public void nodeCreated(BidiNodeContext nodeContext, Object node)
	{
		// TODO Auto-generated method stub

	}

	protected abstract BidiNodeConfig createNodeConfig(Class node);


}