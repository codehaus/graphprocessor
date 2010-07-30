package org.codehaus.graphprocessor.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeProcessingUnit;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.PropertyProcessingUnit;


public class NodeProcessingUnitImpl implements NodeProcessingUnit
{
	private final NodeProcessor processor;
	private final NodeConfig node;

	protected boolean isInitialized = false;
	protected Map<String, PropertyProcessingUnit> childPropertyUnits;


	public NodeProcessingUnitImpl(NodeProcessor processor, NodeConfig node)
	{
		this.processor = processor;
		this.node = node;
		childPropertyUnits = new TreeMap<String, PropertyProcessingUnit>();
	}

	@Override
	public NodeConfig getNodeConfig()
	{
		return node;
	}

	@Override
	public NodeProcessor getProcessor()
	{
		return processor;
	}

	public Collection<PropertyProcessingUnit> getChildPropertyProcessingUnits()
	{
		return this.childPropertyUnits.values();
	}

	public PropertyProcessingUnit getPropertyProcessingUnit(String id)
	{
		return this.childPropertyUnits.get(id);
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


}
