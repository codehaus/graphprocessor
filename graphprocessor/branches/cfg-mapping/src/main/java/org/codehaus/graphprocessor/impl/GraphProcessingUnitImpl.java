package org.codehaus.graphprocessor.impl;

import java.util.Collection;

import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphProcessingUnit;
import org.codehaus.graphprocessor.GraphProcessor;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.NodeProcessingUnit;


public class GraphProcessingUnitImpl extends AbstractProcessingUnit implements GraphProcessingUnit, Initializable
{
	private final GraphProcessor processor;
	private final GraphConfig config;
	private final CachedClassLookupMap<NodeProcessingUnit> nodeProcessingUnits;

	public GraphProcessingUnitImpl(GraphProcessor processor, GraphConfig config)
	{
		this.config = config;
		this.processor = processor;
		this.nodeProcessingUnits = new CachedClassLookupMap<NodeProcessingUnit>();
	}

	@Override
	public GraphConfig getGraph()
	{
		return config;
	}

	public GraphProcessor getProcessor()
	{
		return processor;
	}

	protected void addNodeProcessingUnit(NodeProcessingUnit unit)
	{
		nodeProcessingUnits.put(unit.getNodeConfig().getType(), unit);
	}

	protected NodeProcessingUnit findNodeProcessingUnit(Class type)
	{
		return this.nodeProcessingUnits.get(type);
	}

	@Override
	public Collection<? extends AbstractProcessingUnit> getChildProcessingUnits()
	{
		return nodeProcessingUnits.values();
	}


}
