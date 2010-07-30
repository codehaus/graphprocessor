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
package org.codehaus.graphprocessor;

import java.util.Collection;




/**
 * Graph specific configuration settings.
 */
public interface GraphConfiguration
{

	/**
	 * Adds a {@link NodeConfig} under a specific distance. Distance specifies the level of processing depth when this mapping
	 * shall become active.
	 * 
	 * @param distance
	 *           processing distance when {@link NodeConfig} shall become activated
	 * @param nodeMapping
	 *           {@link NodeConfig} to add
	 */
	public void addNodeConfig(int distance, NodeConfig nodeMapping);

	/**
	 * Adds a Collection of {@link NodeConfig} under a specific distance. Distance specifies the level of processing depth when
	 * this mapping shall become active.
	 * 
	 * @param distance
	 *           processing distance when {@link NodeConfig} shall become activated
	 * @param nodeMapping
	 *           {@link NodeConfig} to add
	 */
	public void addNodeConfig(int distance, Collection<NodeConfig> nodeMapping);


	/**
	 * Returns the {@link NodeConfig} which was configured on root-level base.
	 * <p/>
	 * This call equals {@link #getNodeConfig(int, Class)} with a distance level of zero.
	 * 
	 * @param type
	 *           type of node
	 * @return {@link NodeConfig}
	 */
	public NodeConfig getNodeConfig(Class<?> type);

	/**
	 * Returns the {@link NodeConfig} for a requested node type and requested distance when that mapping shall be used. A
	 * {@link NodeConfig} which was added under e.g. distance 2 is not returned here when requesting it under distance level 3
	 * (this only works at runtime during graph processing)
	 * 
	 * @param distance
	 * @param type
	 * @return NodeConfig
	 */
	public NodeConfig getNodeConfig(int distance, Class<?> type);


}
