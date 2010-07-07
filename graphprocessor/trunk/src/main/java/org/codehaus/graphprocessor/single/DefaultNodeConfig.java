package org.codehaus.graphprocessor.single;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.graphprocessor.AbstractNodeConfig;
import org.codehaus.graphprocessor.AbstractPropertyConfig;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.PropertyConfig;



public class DefaultNodeConfig extends AbstractNodeConfig
{
	private Class<?> type = null;
	private String[] uidPropnames = null;
	private PropertyConfig[] uidProperties = null;
	private Map<String, PropertyConfig> properties = null;
	private boolean isVirtualNode = false;

	protected DefaultNodeConfig(final GraphConfig graphConfig)
	{
		super(graphConfig);
	}

	public DefaultNodeConfig(final GraphConfig graphConfig, final Class<?> type)
	{
		this(graphConfig);
		this.type = type;
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
	public boolean initializeNode()
	{
		return true;
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
		super.isPropertiesInitialized = false;
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

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.AbstractNodeConfig#isDebugEnabled()
	 */
	@Override
	public boolean isDebugEnabled()
	{
		final Set<String> debugNodes = getGraphConfig().getDebugNodes();
		final boolean enabled = debugNodes.isEmpty() || debugNodes.contains(this.getType().getSimpleName());
		return enabled;
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


	protected DefaultPropertyConfig createPropertyConfig(final String propertyName)
	{
		return new DefaultPropertyConfig(this, propertyName, propertyName);
	}



}
