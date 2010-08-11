package org.codehaus.graphprocessor.impl;

import java.util.Collection;
import java.util.Collections;

import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyProcessingUnit;
import org.codehaus.graphprocessor.PropertyProcessor;


public class PropertyProcessingUnitImpl extends AbstractProcessingUnit implements PropertyProcessingUnit
{
	private final PropertyConfig property;
	private PropertyProcessor processor;
	private boolean isNodeProcessing;

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
		return isNodeProcessing;
	}

	public void setNode(boolean value)
	{
		isNodeProcessing = value;
	}


	@Override
	public Collection<? extends AbstractProcessingUnit> getChildProcessingUnits()
	{
		return Collections.EMPTY_LIST;
	}

	public String getId()
	{
		return property.getName();
	}


}
