package org.codehaus.graphprocessor.impl;

import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.NodeConfig;


public class DefaultGraphConfig extends AbstractGraphConfig implements GraphConfig
{
	public DefaultGraphConfig()
	{
		// nodeProcessorMap.put(Object.class, new BidiNodeProcessorImpl());
	}

	public DefaultGraphConfig(final Class rootNode)
	{
		this();

		if (rootNode == null)
		{
			throw new GraphException("Error creating " + this.getClass().getSimpleName() + "; no root node was passed");
		}

		addNodes(rootNode);
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.DefaultGraphConfig#createNodeConfig(java.lang. Class)
	 */
	@Override
	protected NodeConfig createNodeConfig(final Class node)
	{
		return new DefaultNodeConfig(this, node);
	}

	// /*
	// * (non-Javadoc)
	// * @see de.hybris.platform.webservices.util.objectgraphtransformer.impl.DefaultGraphConfig#initialize()
	// */
	// @Override
	// public boolean initialize(int complianceLevel)
	// {
	// boolean isInitialized = true;
	//
	// for (final BidiNodeConfig nodeCfg : getNodes().values())
	// {
	// if (nodeCfg instanceof Initializable)
	// {
	// // binary AND; no short-circuit
	// isInitialized = isInitialized & ((Initializable) nodeCfg).initialize(0);
	// }
	//
	// BidiNodeConfig targetNode = (nodeCfg).getTargetNodeConfig();
	// if (targetNode instanceof Initializable)
	// {
	// // binary AND; no short-circuit
	// isInitialized = isInitialized & ((Initializable) targetNode).initialize(0);
	// }
	// }
	// setInitialized(isInitialized);
	// return true;
	// }
}
