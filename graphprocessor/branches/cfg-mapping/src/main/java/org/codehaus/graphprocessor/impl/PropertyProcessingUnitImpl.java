package org.codehaus.graphprocessor.impl;

import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyProcessingUnit;
import org.codehaus.graphprocessor.PropertyProcessor;


public class PropertyProcessingUnitImpl implements PropertyProcessingUnit
{
	private final PropertyConfig property;
	private PropertyProcessor processor;
	protected boolean isInitialized = false;
	protected boolean isNode;
	protected boolean isTypeCheckEnabled = false;

	public PropertyProcessingUnitImpl(PropertyProcessor processor, PropertyConfig property)
	{
		this.property = property;
		this.processor = processor;
	}

	@Override
	public PropertyConfig getPropertyConfig()
	{
		return property;
	}

	public PropertyProcessor getProcessor()
	{
		if (this.processor == null)
		{
			Class type = property.getReadType();
			if (type == null)
			{
				type = property.getWriteType();
			}
			this.processor = property.getParentNode().getParentGraph().getPropertyProcessor(type);
		}
		return processor;
	}

	@Override
	public boolean isNode()
	{
		return isNode;
	}

	@Override
	public boolean isTypeCheckEnabled()
	{
		return isTypeCheckEnabled;
	}

	public boolean initialize(final int complianceLevel)
	{
		isInitialized = true;
		return true;
	}

	public boolean isInitialized()
	{
		return isInitialized;
	}

	public String getId()
	{
		return property.getName();
	}


}
