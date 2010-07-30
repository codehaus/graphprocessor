package org.codehaus.graphprocessor.bidi.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeProcessor;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyProcessor;



/**
 * Abstract base implementation for {@link BidiNodeConfig}
 */
public abstract class AbstractBidiNodeConfig implements BidiNodeConfig, Initializable
{

	protected boolean isNodeInitialized = false;
	protected boolean isPropertiesInitialized = false;

	private BidiGraphConfig graphConfig = null;
	private BidiNodeProcessor nodeProcessor = null;

	private Class<?> type = null;
	private String[] uidPropnames = null;
	private BidiPropertyConfig[] uidProperties = null;
	private Map<String, BidiPropertyConfig> properties = null;
	private boolean isVirtualNode = false;

	public AbstractBidiNodeConfig(final BidiGraphConfig graphConfig)
	{
		this.graphConfig = graphConfig;
	}

	public AbstractBidiNodeConfig(final BidiGraphConfig graphConfig, final Class<?> type)
	{
		this.graphConfig = graphConfig;
		this.type = type;
	}

	@Override
	public BidiGraphConfig getGraphConfig()
	{
		return this.graphConfig;
	}


	/**
	 * Initializes this node.
	 * <p/>
	 */
	public boolean initialize(final int complianceLevel)
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
	 * Initializes each {@link BidiPropertyConfig} of this node.
	 * <p/>
	 * If {@link BidiPropertyConfig} is already initialized, it gets skipped.<br/>
	 * If {@link BidiPropertyConfig} is not initialized, their initializer method gets called. If initialization fails, property
	 * gets removed from that node.
	 * 
	 * @return true when initialization succeeds
	 */
	protected boolean initializeProperties()
	{
		// initialize/refresh properties (when not already done)
		final Map<String, BidiPropertyConfig> properties = this.getProperties();
		for (final Iterator<BidiPropertyConfig> iter = properties.values().iterator(); iter.hasNext();)
		{
			final BidiPropertyConfig pCfg = iter.next();

			if (pCfg instanceof Initializable)
			{
				final Initializable aPropCfg = (Initializable) pCfg;
				if (!aPropCfg.isInitialized())
				{
					final boolean valid = aPropCfg.initialize(AbstractBidiPropertyConfig.COMPLIANCE_LEVEL_LOW);
					if (!valid)
					{
						iter.remove();
					}
				}
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
	public BidiNodeProcessor getProcessor()
	{
		if (this.nodeProcessor == null)
		{
			this.nodeProcessor = this.graphConfig.getDefaultNodeProcessor(this.type);
		}
		return nodeProcessor;
	}

	/**
	 * @param nodeProcessor
	 *           the nodeProcessor to set
	 */
	public void setProcessor(final BidiNodeProcessor nodeProcessor)
	{
		this.nodeProcessor = nodeProcessor;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.NodeConfig#getPropertyProcessor(java.lang.Class)
	 */
	@Override
	public BidiPropertyProcessor getPropertyProcessor(final Class propertyType)
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
	public void setUidProperties(final BidiPropertyConfig[] uidProperties)
	{
		this.uidProperties = uidProperties;
	}


	@Override
	public BidiPropertyConfig[] getUidProperties()
	{
		if (this.uidProperties == null && uidPropnames != null && uidPropnames.length > 0)
		{
			this.uidProperties = new BidiPropertyConfig[uidPropnames.length];
			// this.uidProperties = new PropertyConfig[uidPropnames.length];
			// final Map<String, PropertyConfig> cfgMap = this.getProperties();
			for (int i = 0; i < uidPropnames.length; i++)
			{
				BidiPropertyConfig propCfg = this.getPropertyConfigByName(uidPropnames[i]);

				if (propCfg == null)
				{
					propCfg = getTargetNodeConfig().getPropertyConfigByName(uidPropnames[i]).getTargetProperty();
				}

				if (propCfg == null)
				{
					throw new GraphException("Can't find  declared unique property '" + uidPropnames[i] + " on type "
							+ this.type.getSimpleName());
				}

				uidProperties[i] = propCfg;
			}
		}
		return this.uidProperties;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.NodeConfig#getProperties()
	 */
	@Override
	public Map<String, BidiPropertyConfig> getProperties()
	{
		return this.properties;
	}

	@Override
	public BidiPropertyConfig getPropertyConfigByName(final String propertyName)
	{
		return this.properties.get(propertyName);
	}

	public void addPropertyConfig(final BidiPropertyConfig propCfg)
	{
		getProperties().put(propCfg.getId(), propCfg);
		this.isPropertiesInitialized = false;
	}

	@Override
	public BidiPropertyConfig removePropertyConfigByName(final String propertyId)
	{
		// TODO: reseting is initialized is not enough when removing a property includes removing a node
		return getProperties().remove(propertyId);
	}

	@Override
	public Map<String, BidiPropertyConfig> removeAllProperties()
	{
		final Map<String, BidiPropertyConfig> result = new HashMap<String, BidiPropertyConfig>(this.properties);
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

	private Map<String, BidiPropertyConfig> createDefaultProperties()
	{
		final Map<String, BidiPropertyConfig> result = new LinkedHashMap<String, BidiPropertyConfig>();
		final Map<String, Method[]> props = AbstractBidiPropertyConfig.getPropertiesFor(getType());
		for (final String propertyName : props.keySet())
		{
			final BidiPropertyConfig propCfg = this.createPropertyConfig(propertyName);
			if (propCfg.getReadMethod() != null || propCfg.getWriteMethod() != null)
			{
				result.put(propCfg.getId(), propCfg);
			}
		}
		return result;
	}


	protected abstract BidiPropertyConfig createPropertyConfig(final String propertyName);

}
