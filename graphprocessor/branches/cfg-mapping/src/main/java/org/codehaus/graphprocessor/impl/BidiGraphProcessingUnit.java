package org.codehaus.graphprocessor.impl;

import java.util.Collection;

import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.GraphProcessor;
import org.codehaus.graphprocessor.NodeConfig;


public class BidiGraphProcessingUnit extends GraphProcessingUnitImpl
{
	private BidiGraphProcessingUnit targetUnit;

	/**
	 * Internal constructor.
	 * 
	 * @param processor
	 * @param config
	 */
	private BidiGraphProcessingUnit(GraphProcessor processor, GraphConfig config)
	{
		super(processor, config);
	}

	public BidiGraphProcessingUnit(GraphProcessor processor, GraphConfig source, GraphConfig target)
	{
		super(processor, source);
		targetUnit = new BidiGraphProcessingUnit(processor, target);
	}


	public BidiGraphProcessingUnit getTargetGraph()
	{
		return targetUnit;
	}


	@Override
	public boolean initialize(int complianceLevel)
	{
		boolean isInitialized = true;

		// all nodes of configured source graph
		Collection<NodeConfig> nodeConfigs = getGraphConfig().getNodes().values();

		// create a NodeProcessingUnit for source and target graph and for each NodeConfig
		for (final NodeConfig nodeCfg : nodeConfigs)
		{
			// create a target NodeConfig
			NodeConfig targetNodeCfg = findTargetNodeConfig(nodeCfg);

			if (targetNodeCfg != null)
			{

				// create/add a NodeProcessingUnit for this graph
				BidiNodeProcessingUnit sourceUnit = new BidiNodeProcessingUnit(null, nodeCfg, targetNodeCfg);
				addNodeProcessingUnit(sourceUnit);

				// create/ add a NodeProcessingUnit for target graph
				BidiNodeProcessingUnit targetUnit = new BidiNodeProcessingUnit(null, targetNodeCfg, nodeCfg);
				getTargetGraph().addNodeProcessingUnit(targetUnit);
			}
			else
			{
				throw new GraphException("Can't find a target node for " + nodeCfg.getType());
			}
		}

		// initialize NodeProcessingUnits for source ...
		isInitialized = initializeChilds(complianceLevel);

		// ... and target
		isInitialized = isInitialized & targetUnit.initializeChilds(complianceLevel);

		setInitialized(isInitialized);

		return isInitialized;
	}

	/**
	 * Finds a node's target type for a given source type.
	 * 
	 * @param nodeConfig
	 * @return target NodeConfig
	 */
	private NodeConfig findTargetNodeConfig(NodeConfig nodeConfig)
	{
		NodeConfig result = null;
		Class<?> srcType = nodeConfig.getType();
		if (srcType.isAnnotationPresent(GraphNode.class))
		{
			GraphNode node = srcType.getAnnotation(GraphNode.class);
			Class<?> targetType = node.target();
			result = getGraphConfig().getNodeConfigFactory().getNodeConfig(targetType);
		}
		return result;
	}
}
