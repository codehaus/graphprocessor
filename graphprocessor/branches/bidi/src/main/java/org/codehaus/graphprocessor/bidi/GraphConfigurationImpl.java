/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2010 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 * 
 *  
 */
package org.codehaus.graphprocessor.bidi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.GraphException;



/**
 * Manages runtime access to various {@link BidiNodeConfig} instances. {@link BidiGraphConfig} already provides access to
 * {@link BidiNodeConfig} instances but processing runtime adds dynamic behavior like different {@link BidiNodeConfig} instances
 * for same node types based on depth of current processing (distance) or current processed property.
 */
public class GraphConfigurationImpl implements GraphConfiguration
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(GraphConfigurationImpl.class);

	// configures custom NodeConfig instances which are used dependently on graph processing depth
	private List<CachedClassLookupMap<BidiNodeConfig>> nodeConfigByDepth = null;

	// base NodeConfig configuration
	// private CachedClassLookupMap<NodeConfig> baseConfig = null;
	private Map<Class<?>, BidiNodeConfig> baseConfig = null;


	/**
	 * Constructor. Creates a new configuration and uses passed Map of {@link BidiNodeConfig} instances as root configuration.
	 * 
	 * @param baseConfiguration
	 */
	// public GraphConfigurationImpl(final CachedClassLookupMap<NodeConfig> baseConfiguration)
	public GraphConfigurationImpl(final Map<Class<?>, BidiNodeConfig> baseConfiguration)
	{
		this.baseConfig = baseConfiguration;
		this.nodeConfigByDepth = new ArrayList<CachedClassLookupMap<BidiNodeConfig>>();
	}

	public CachedClassLookupMap<BidiNodeConfig> getAllNodeConfigs(final int distance)
	{
		// only distances above zero are valid
		if (distance < 0)
		{
			throw new GraphException("Can't manage a node configuration for a distance below zero (" + distance + ")");
		}

		// initially create lookup for "NodeConfig by processing depth" when not done
		if (this.nodeConfigByDepth.isEmpty())
		{
			this.nodeConfigByDepth.add(new CachedClassLookupMap<BidiNodeConfig>(baseConfig));
		}

		// increase size of "NodeConfig by processing depth" stack when requested distance is greater
		while (this.nodeConfigByDepth.size() < distance + 1)
		{
			this.nodeConfigByDepth.add(new CachedClassLookupMap<BidiNodeConfig>());
		}

		// retrieve lookup Map for requested distance
		final CachedClassLookupMap<BidiNodeConfig> result = this.nodeConfigByDepth.get(distance);
		return result;
	}



	@Override
	public void addNodeConfig(final int distance, final BidiNodeConfig nodeConfig)
	{
		this.getAllNodeConfigs(distance).put(nodeConfig.getType(), nodeConfig);
	}

	@Override
	public void addNodeConfig(final int distance, final Collection<BidiNodeConfig> nodeMappingList)
	{
		final CachedClassLookupMap<BidiNodeConfig> map = this.getAllNodeConfigs(distance);
		for (final BidiNodeConfig nodeConfig : nodeMappingList)
		{
			map.put(nodeConfig.getType(), nodeConfig);
		}
	}

	@Override
	public BidiNodeConfig getNodeConfig(final Class<?> type)
	{
		return getNodeConfig(0, type);
	}

	@Override
	public BidiNodeConfig getNodeConfig(final int distance, final Class<?> type)
	{
		final BidiNodeConfig result = this.getAllNodeConfigs(distance).get(type);
		return result;
	}



}
