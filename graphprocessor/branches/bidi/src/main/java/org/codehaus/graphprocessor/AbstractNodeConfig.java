package org.codehaus.graphprocessor;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;



/**
 * Abstract base implementation for {@link NodeConfig}
 */
public abstract class AbstractNodeConfig implements NodeConfig, Initializable
{

	protected boolean isNodeInitialized = false;
	protected boolean isPropertiesInitialized = false;

	private GraphConfig graphConfig = null;
	private NodeProcessor nodeProcessor = null;

	private Class<?> type = null;
	private String[] uidPropnames = null;
	private PropertyConfig[] uidProperties = null;
	private Map<String, PropertyConfig> properties = null;
	private boolean isVirtualNode = false;

	public AbstractNodeConfig(final GraphConfig graphConfig)
	{
		this.graphConfig = graphConfig;
	}

	public AbstractNodeConfig(final GraphConfig graphConfig, final Class<?> type)
	{
		this.graphConfig = graphConfig;
		this.type = type;
	}

	@Override
	public GraphConfig getGraphConfig()
	{
		return this.graphConfig;
	}


	/**
	 * Initializes this node.
	 * <p/>
	 */
	public boolean initialize(int complianceLevel)
	{
		// step1: initialize node
		if (!this.isNodeInitialized)
		{
			this.isNodeInitialized = this.initializeNode();
		}

		if (!this.isPropertiesInitialized)
		{
			// step2: initialize properties; filter out invalid ones
			this.isPropertiesInitialized = initializeProperties();
		}
		return true;
	}

	protected abstract boolean initializeNode();

	/**
	 * Initializes each {@link PropertyConfig} of this node.
	 * <p/>
	 * If {@link PropertyConfig} is already initialized, it gets skipped.<br/>
	 * If {@link PropertyConfig} is not initialized, their initializer method gets called. If initialization fails, property gets
	 * removed from that node.
	 * 
	 * @return true when initialization succeeds
	 */
	protected boolean initializeProperties()
	{
		// initialize/refresh properties (when not already done)
		final Map<String, PropertyConfig> properties = this.getProperties();
		for (final Iterator<PropertyConfig> iter = properties.values().iterator(); iter.hasNext();)
		{
			final PropertyConfig pCfg = iter.next();
			if (pCfg instanceof AbstractPropertyConfig)
			{
				final AbstractPropertyConfig aPropCfg = (AbstractPropertyConfig) pCfg;
				if (!aPropCfg.isInitialized())
				{
					final boolean valid = aPropCfg.initialize(AbstractPropertyConfig.COMPLIANCE_LEVEL_LOW);
					if (!valid)
					{
						iter.remove();
					}
				}
			}
			else
			{
				throw new GraphException("Need an instance of " + AbstractPropertyConfig.class.getSimpleName());
			}
		}
		return true;
	}

	public boolean isInitialized()
	{
		return this.isNodeInitialized && this.isPropertiesInitialized;
	}

	protected void setInitialized(final boolean initialized)
	{
		this.isNodeInitialized = initialized;
		this.isPropertiesInitialized = initialized;
	}


	/**
	 * @return the nodeProcessor
	 */
	public NodeProcessor getProcessor()
	{
		return nodeProcessor;
	}

	/**
	 * @param nodeProcessor
	 *           the nodeProcessor to set
	 */
	public void setProcessor(NodeProcessor nodeProcessor)
	{
		this.nodeProcessor = nodeProcessor;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.NodeConfig#getPropertyProcessor(java.lang.Class)
	 */
	@Override
	public PropertyProcessor getPropertyProcessor(Class propertyType)
	{
		return getGraphConfig().getDefaultPropertyProcessor(propertyType);
	}




	public boolean isVirtual()
	{
		return this.isVirtualNode;
	}

	public void setVirtual(final boolean virtual)
	{
		this.isVirtualNode = virtual;
	}



	@Override
	public Class getType()
	{
		return this.type;
	}

	protected void setType(final Class<?> type)
	{
		this.type = type;
	}


	public void setUidPropertyNames(final String propNames)
	{
		this.uidPropnames = null;
		this.uidProperties = null;

		// split and remove whitespaces
		if (propNames != null && propNames.trim().length() > 0)
		{
			this.uidPropnames = propNames.split("\\s*,\\s*");
		}
	}

	public String[] getUidPropertyNames()
	{
		return this.uidPropnames;
	}


	/**
	 * @param uidProperties
	 *           the uidProperties to set
	 */
	public void setUidProperties(final PropertyConfig[] uidProperties)
	{
		this.uidProperties = uidProperties;
	}


	@Override
	public PropertyConfig[] getUidProperties()
	{
		if (this.uidProperties == null && uidPropnames != null && uidPropnames.length > 0)
		{
			this.uidProperties = new PropertyConfig[uidPropnames.length];
			// this.uidProperties = new PropertyConfig[uidPropnames.length];
			// final Map<String, PropertyConfig> cfgMap = this.getProperties();
			for (int i = 0; i < uidPropnames.length; i++)
			{
				// uidProperties[i] = cfgMap.get(uidPropnames[i]);
				uidProperties[i] = this.getPropertyConfigByName(uidPropnames[i]);
			}
		}
		return this.uidProperties;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.NodeConfig#getProperties()
	 */
	@Override
	public Map<String, PropertyConfig> getProperties()
	{
		return this.properties;
	}

	@Override
	public PropertyConfig getPropertyConfigByName(String propertyName)
	{
		return this.properties.get(propertyName);
	}

	public void addPropertyConfig(PropertyConfig propCfg)
	{
		getProperties().put(propCfg.getId(), propCfg);
		this.isPropertiesInitialized = false;
	}

	@Override
	public PropertyConfig removePropertyConfigByName(String propertyId)
	{
		// TODO: reseting is initialized is not enough when removing a property includes removing a node
		return getProperties().remove(propertyId);
	}

	@Override
	public Map<String, PropertyConfig> removeAllProperties()
	{
		Map<String, PropertyConfig> result = new HashMap<String, PropertyConfig>(this.properties);
		this.properties.clear();
		return result;
	}


	protected void setDefaults()
	{
		// default propertyconfig lookup
		this.properties = this.createDefaultProperties();

		// default UID property lookup
		if (type.isAnnotationPresent(GraphNode.class))
		{
			final GraphNode cfg = type.getAnnotation(GraphNode.class);
			if (cfg.uidProperties().trim().length() > 0)
			{
				setUidPropertyNames(cfg.uidProperties());
			}
		}

	}

	private Map<String, PropertyConfig> createDefaultProperties()
	{
		final Map<String, PropertyConfig> result = new LinkedHashMap<String, PropertyConfig>();
		final Map<String, Method[]> props = AbstractPropertyConfig.getPropertiesFor(getType());
		for (final String propertyName : props.keySet())
		{
			final PropertyConfig propCfg = this.createPropertyConfig(propertyName);
			if (propCfg.getReadMethod() != null || propCfg.getWriteMethod() != null)
			{
				result.put(propCfg.getId(), propCfg);
			}
		}
		return result;
	}


	protected abstract PropertyConfig createPropertyConfig(final String propertyName);

}
