package org.codehaus.graphprocessor.impl;

import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.PropertyConfig;


public class BidiNodeProcessingUnit extends NodeProcessingUnitImpl
{
	private static final Logger log = Logger.getLogger(BidiNodeProcessingUnit.class);

	private BidiNodeProcessingUnit targetUnit;


	/**
	 * Internal constructor.
	 * 
	 * @param processor
	 * @param sourceNodeCfg
	 */
	private BidiNodeProcessingUnit(NodeProcessor processor, NodeConfig sourceNodeCfg)
	{
		super(processor, sourceNodeCfg);
	}

	public BidiNodeProcessingUnit(NodeProcessor processor, NodeConfig sourceNode, NodeConfig targetNode)
	{
		super(processor, sourceNode);
		this.targetUnit = new BidiNodeProcessingUnit(processor, targetNode);
	}

	public BidiNodeProcessingUnit getTargetNode()
	{
		return targetUnit;
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
		createOrUpdateChildProcessingUnits();
		getTargetNode().createOrUpdateChildProcessingUnits();

		boolean success1 = initializeChilds(complianceLevel);
		boolean success2 = getTargetNode().initializeChilds(complianceLevel);

		boolean isInitialized = success1 && success2;
		setInitialized(isInitialized);

		return isInitialized;
	}

	private void createOrUpdateChildProcessingUnits()
	{
		// initialize/refresh properties (when not already done)
		final Map<String, PropertyConfig> properties = getNodeConfig().getProperties();

		for (PropertyConfig propCfg : properties.values())
		{
			BidiPropertyProcessingUnit childProceesingUnit = createChildProcessingUnit(propCfg);

			if (childProceesingUnit != null)
			{
				this.childPropertyUnits.put(childProceesingUnit.getId(), childProceesingUnit);
			}
		}
	}


	private BidiPropertyProcessingUnit createChildProcessingUnit(PropertyConfig propCfg)
	{
		String propName = propCfg.getName();
		PropertyConfig targetProperty = getTargetNode().getNodeConfig().getPropertyConfig(propName);
		BidiPropertyProcessingUnit result = new BidiPropertyProcessingUnit(null, propCfg, targetProperty);
		boolean isInitialized = result.initialize(COMPLIANCE_LEVEL_LOW);

		if (!isInitialized)
		{
			result = null;
		}

		return result;
	}


}
