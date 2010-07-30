package org.codehaus.graphprocessor.impl;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.PropertyConfig;



public abstract class AbstractNodeConfig implements NodeConfig
{

	protected boolean isNodeInitialized = false;
	protected boolean isPropertiesInitialized = false;

	private GraphConfig graphConfig = null;

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
	public GraphConfig getParentGraph()
	{
		return this.graphConfig;
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
				PropertyConfig propCfg = this.getPropertyConfig(uidPropnames[i]);

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
	public Map<String, PropertyConfig> getProperties()
	{
		return this.properties;
	}

	@Override
	public PropertyConfig getPropertyConfig(final String propertyName)
	{
		return this.properties.get(propertyName);
	}

	public void addPropertyConfig(final PropertyConfig propCfg)
	{
		getProperties().put(propCfg.getName(), propCfg);
		this.isPropertiesInitialized = false;
	}

	@Override
	public PropertyConfig removePropertyConfig(final String propertyId)
	{
		// TODO: reseting is initialized is not enough when removing a property includes removing a node
		return getProperties().remove(propertyId);
	}

	@Override
	public Map<String, PropertyConfig> removeAllProperties()
	{
		final Map<String, PropertyConfig> result = new HashMap<String, PropertyConfig>(this.properties);
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
				result.put(propCfg.getName(), propCfg);
			}
		}
		return result;
	}


	protected abstract PropertyConfig createPropertyConfig(final String propertyName);

}
