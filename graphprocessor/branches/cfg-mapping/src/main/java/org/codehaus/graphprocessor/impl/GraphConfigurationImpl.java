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
import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.GraphConfiguration;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeProcessingUnit;



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
	private List<CachedClassLookupMap<NodeProcessingUnit>> nodeConfigByDepth = null;

	// base NodeConfig configuration
	// private CachedClassLookupMap<NodeConfig> baseConfig = null;
	private Map<Class<?>, NodeProcessingUnit> baseConfig = null;


	/**
	 * Constructor. Creates a new configuration and uses passed Map of {@link NodeConfig} instances as root configuration.
	 * 
	 * @param baseConfiguration
	 */
	// public GraphConfigurationImpl(final CachedClassLookupMap<NodeConfig> baseConfiguration)
	public GraphConfigurationImpl(final Map<Class<?>, NodeProcessingUnit> baseConfiguration)
	{
		this.baseConfig = baseConfiguration;
		this.nodeConfigByDepth = new ArrayList<CachedClassLookupMap<NodeProcessingUnit>>();
	}

	public CachedClassLookupMap<NodeProcessingUnit> getAllNodeConfigs(final int distance)
	{
		// only distances above zero are valid
		if (distance < 0)
		{
			throw new GraphException("Can't manage a node configuration for a distance below zero (" + distance + ")");
		}

		// initially create lookup for "NodeConfig by processing depth" when not done
		if (this.nodeConfigByDepth.isEmpty())
		{
			this.nodeConfigByDepth.add(new CachedClassLookupMap<NodeProcessingUnit>(baseConfig));
		}

		// increase size of "NodeConfig by processing depth" stack when requested distance is greater
		while (this.nodeConfigByDepth.size() < distance + 1)
		{
			this.nodeConfigByDepth.add(new CachedClassLookupMap<NodeProcessingUnit>());
		}

		// retrieve lookup Map for requested distance
		final CachedClassLookupMap<NodeProcessingUnit> result = this.nodeConfigByDepth.get(distance);
		return result;
	}



	@Override
	public void addNodeConfig(final int distance, final NodeProcessingUnit nodeConfig)
	{
		this.getAllNodeConfigs(distance).put(nodeConfig.getNodeConfig().getType(), nodeConfig);
	}

	@Override
	public void addNodeConfig(final int distance, final Collection<NodeProcessingUnit> nodeMappingList)
	{
		final CachedClassLookupMap<NodeProcessingUnit> map = this.getAllNodeConfigs(distance);
		for (final NodeProcessingUnit nodeConfig : nodeMappingList)
		{
			map.put(nodeConfig.getNodeConfig().getType(), nodeConfig);
		}
	}

	@Override
	public NodeProcessingUnit getNodeConfig(final Class<?> type)
	{
		return getNodeConfig(0, type);
	}

	@Override
	public NodeProcessingUnit getNodeConfig(final int distance, final Class<?> type)
	{
		final NodeProcessingUnit result = this.getAllNodeConfigs(distance).get(type);
		return result;
	}



}
