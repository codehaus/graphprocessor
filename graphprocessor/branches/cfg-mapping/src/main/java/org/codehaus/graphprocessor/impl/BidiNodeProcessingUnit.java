package org.codehaus.graphprocessor.impl;

import java.util.Map;

import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.PropertyConfig;


public class BidiNodeProcessingUnit extends NodeProcessingUnitImpl
{
	private final NodeConfig targetNode;

	public BidiNodeProcessingUnit(NodeProcessor processor, NodeConfig sourceNode, NodeConfig targetNode)
	{
		super(processor, sourceNode);
		this.targetNode = targetNode;
	}

	public NodeConfig getTargetNode()
	{
		return targetNode;
	}

	@Override
	public BidiPropertyProcessingUnit getPropertyProcessingUnit(String mapping)
	{
		return getPropertyProcessingUnit(mapping, mapping);
	}

	public BidiPropertyProcessingUnit getPropertyProcessingUnit(String sourceName, String targetName)
	{
		return (BidiPropertyProcessingUnit) this.childPropertyUnits.get(sourceName + "-" + targetName);
	}

	@Override
	public boolean initialize(int complianceLevel)
	{
		// initialize/refresh properties (when not already done)
		final Map<String, PropertyConfig> properties = getNodeConfig().getProperties();

		boolean success = true;
		for (PropertyConfig propCfg : properties.values())
		{
			PropertyConfig targetProperty = targetNode.getPropertyConfig(propCfg.getName());
			BidiPropertyProcessingUnit unit = new BidiPropertyProcessingUnit(null, propCfg, targetProperty);
			boolean _success = unit.initialize(complianceLevel);
			if (_success)
			{
				this.childPropertyUnits.put(unit.getId(), unit);
			}
			success = success & _success;
		}

		return success;
	}

}
