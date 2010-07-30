package org.codehaus.graphprocessor.impl;

import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphProcessingUnit;
import org.codehaus.graphprocessor.GraphProcessor;


public class GraphProcessingUnitImpl implements GraphProcessingUnit
{
	private final GraphProcessor processor;
	private final GraphConfig config;

	protected boolean isInitialized = false;

	public GraphProcessingUnitImpl(GraphProcessor processor, GraphConfig config)
	{
		this.config = config;
		this.processor = processor;
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
