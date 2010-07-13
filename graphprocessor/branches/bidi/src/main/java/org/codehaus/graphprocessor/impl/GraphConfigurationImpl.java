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
package org.codehaus.graphprocessor.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphConfiguration;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.NodeConfig;



/**
 * Manages runtime access to various {@link NodeConfig} instances. {@link GraphConfig} already provides access to
 * {@link NodeConfig} instances but processing runtime adds dynamic behavior like different {@link NodeConfig} instances for same
 * node types based on depth of current processing (distance) or current processed property.
 */
public class GraphConfigurationImpl implements GraphConfiguration
{
	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(GraphConfigurationImpl.class);

	// configures custom NodeConfig instances which are used dependently on graph processing depth
	private List<CachedClassLookupMap<NodeConfig>> nodeConfigByDepth = null;

	// base NodeConfig configuration
	// private CachedClassLookupMap<NodeConfig> baseConfig = null;
	private Map<Class<?>, NodeConfig> baseConfig = null;


	/**
	 * Constructor. Creates a new configuration and uses passed Map of {@link NodeConfig} instances as root configuration.
	 * @param baseConfiguration
	 */
	// public GraphConfigurationImpl(final CachedClassLookupMap<NodeConfig> baseConfiguration)
	public GraphConfigurationImpl(final Map<Class<?>, NodeConfig> baseConfiguration)
	{
		this.baseConfig = baseConfiguration;
		this.nodeConfigByDepth = new ArrayList<CachedClassLookupMap<NodeConfig>>();
	}

	public CachedClassLookupMap<NodeConfig> getAllNodeConfigs(final int distance)
	{
		// only distances above zero are valid
		if (distance < 0)
		{
			throw new GraphException("Can't manage a node configuration for a distance below zero (" + distance + ")");
		}

		// initially create lookup for "NodeConfig by processing depth" when not done
		if (this.nodeConfigByDepth.isEmpty())
		{
			this.nodeConfigByDepth.add(new CachedClassLookupMap<NodeConfig>(baseConfig));
		}

		// increase size of "NodeConfig by processing depth" stack when requested distance is greater
		while (this.nodeConfigByDepth.size() < distance + 1)
		{
			this.nodeConfigByDepth.add(new CachedClassLookupMap<NodeConfig>());
		}

		// retrieve lookup Map for requested distance
		final CachedClassLookupMap<NodeConfig> result = this.nodeConfigByDepth.get(distance);
		return result;
	}



	@Override
	public void addNodeConfig(final int distance, final NodeConfig nodeConfig)
	{
		this.getAllNodeConfigs(distance).put(nodeConfig.getType(), nodeConfig);
	}

	@Override
	public void addNodeConfig(final int distance, final Collection<NodeConfig> nodeMappingList)
	{
		final CachedClassLookupMap<NodeConfig> map = this.getAllNodeConfigs(distance);
		for (final NodeConfig nodeConfig : nodeMappingList)
		{
			map.put(nodeConfig.getType(), nodeConfig);
		}
	}

	@Override
	public NodeConfig getNodeConfig(final Class<?> type)
	{
		return getNodeConfig(0, type);
	}

	@Override
	public NodeConfig getNodeConfig(final int distance, final Class<?> type)
	{
		final NodeConfig result = this.getAllNodeConfigs(distance).get(type);
		return result;
	}



}
