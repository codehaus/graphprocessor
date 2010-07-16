package org.codehaus.graphprocessor.bidi.impl;

import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;


public class DefaultBidiGraphConfig extends AbstractBidiGraphConfig implements BidiGraphConfig
{
	private DefaultBidiGraphConfig targetGraphCfg = null;

	public DefaultBidiGraphConfig()
	{
		// circular reference
		this.targetGraphCfg = new DefaultBidiGraphConfig(this);
		nodeProcessorMap.put(Object.class, new BidiNodeProcessorImpl());
	}

	public DefaultBidiGraphConfig(final Class rootNode)
	{
		this();

		if (rootNode == null)
		{
			throw new GraphException("Error creating " + this.getClass().getSimpleName() + "; no root node was passed");
		}

		addNodes(rootNode);
	}


	private DefaultBidiGraphConfig(final DefaultBidiGraphConfig targetConfig)
	{
		this.targetGraphCfg = targetConfig;
	}

	public DefaultBidiGraphConfig getTargetConfig()
	{
		return this.targetGraphCfg;
	}

	@Override
	public BidiNodeConfig getNodeConfig(final Class node)
	{
		return super.getNodeConfig(node);
	}

	@Override
	public BidiNodeConfig getAssignableNodeConfig(Class nodeType)
	{
		return super.getAssignableNodeConfig(nodeType);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.DefaultGraphConfig#addNode(de.hybris.platform.
	 * webservices.util.objectgraphtransformer.NodeConfig)
	 */
	@Override
	public void addNode(final BidiNodeConfig nodeConfig)
	{
		if (nodeConfig instanceof BidiNodeConfig)
		{
			super.addNode(nodeConfig);
			final BidiNodeConfig targetNode = (nodeConfig).getTargetNodeConfig();

			// this.targetGraphCfg.nodesMap.put(targetNode.getType(), targetNode);
			// need direct access to member to prevent an infinite cycle
			this.targetGraphCfg.nodeLookupMap.put(targetNode.getType(), targetNode);
		}
		else
		{
			throw new GraphException("Need an instance of " + BidiNodeConfig.class.getSimpleName() + " but bot "
					+ nodeConfig.getClass().getName());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.DefaultGraphConfig#createNodeConfig(java.lang. Class)
	 */
	@Override
	protected BidiNodeConfig createNodeConfig(final Class node)
	{
		return new DefaultBidiNodeConfig(this, node);
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.DefaultGraphConfig#initialize()
	 */
	@Override
	public boolean initialize(int complianceLevel)
	{
		boolean isInitialized = true;

		for (final BidiNodeConfig nodeCfg : getNodes().values())
		{
			if (nodeCfg instanceof Initializable)
			{
				// binary AND; no short-circuit
				isInitialized = isInitialized & ((Initializable) nodeCfg).initialize(0);
			}

			BidiNodeConfig targetNode = (nodeCfg).getTargetNodeConfig();
			if (targetNode instanceof Initializable)
			{
				// binary AND; no short-circuit
				isInitialized = isInitialized & ((Initializable) targetNode).initialize(0);
			}
		}
		setInitialized(isInitialized);
		return true;
	}
}
