package org.codehaus.graphprocessor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphProcessor;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.NodeConfig;


public class BidiGraphProcessingUnit extends GraphProcessingUnitImpl
{
	private final GraphConfig target;
	private final List<BidiNodeProcessingUnit> childNodeProcessings;

	public BidiGraphProcessingUnit(GraphProcessor processor, GraphConfig config, GraphConfig target)
	{
		super(processor, config);
		this.target = target;
		this.childNodeProcessings = new ArrayList<BidiNodeProcessingUnit>();
	}

	public GraphConfig getTargetGraph()
	{
		return target;
	}

	public List<BidiNodeProcessingUnit> getChildNodeProcessingUnits()
	{
		return this.childNodeProcessings;
	}


	@Override
	public boolean initialize(int complianceLevel)
	{
		boolean isInitialized = true;

		Collection<NodeConfig> nodes = getGraph().getNodes().values();
		for (final NodeConfig nodeCfg : nodes)
		{
			BidiNodeProcessingUnit unit = new BidiNodeProcessingUnit(null, nodeCfg sourceNode, targetNode);
			if (nodeCfg instanceof Initializable)
			{
				// binary AND; no short-circuit
				isInitialized = isInitialized & ((Initializable) nodeCfg).initialize(0);
			}

			final NodeConfig targetNode = (nodeCfg).getTargetNodeConfig();
			if (targetNode instanceof Initializable)
			{
				// binary AND; no short-circuit
				isInitialized = isInitialized & ((Initializable) targetNode).initialize(0);
			}
		}
		return isInitialized;
	}
}
