package org.codehaus.graphprocessor.single;

import java.util.Map;

import org.codehaus.graphprocessor.AbstractNodeConfig;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.PropertyConfig;



public class DefaultNodeConfig extends AbstractNodeConfig
{
	private Class<?> type = null;
	private final String[] uidPropnames = null;
	private final PropertyConfig[] uidProperties = null;
	private final Map<String, PropertyConfig> properties = null;
	private final boolean isVirtualNode = false;

	protected DefaultNodeConfig(final GraphConfig graphConfig)
	{
		super(graphConfig);
	}

	public DefaultNodeConfig(final GraphConfig graphConfig, final Class<?> type)
	{
		this(graphConfig);
		this.type = type;
	}

	@Override
	protected boolean initializeNode()
	{
		return true;
	}

	@Override
	protected DefaultPropertyConfig createPropertyConfig(final String propertyName)
	{
		return new DefaultPropertyConfig(this, propertyName, propertyName);
	}



}
