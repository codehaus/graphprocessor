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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractNodeConfig;
import org.codehaus.graphprocessor.GraphConfiguration;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.PropertyFilter;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;



/**
 * Context which is used for and during the process of transforming one object graph into another one.
 */
public class GraphContextImpl implements GraphContext
{
	private static final Logger log = Logger.getLogger(GraphContextImpl.class);

	private List<PropertyFilter> filterList = Collections.EMPTY_LIST;

	private int maxFoundDistance = -1;


	protected GraphConfigurationImpl graphConfigImpl = null;

	private BidiGraphConfig graphConfig = null;

	// TODO; currently used as unsafe node cache in (ID->node)
	protected Map attributes = null;

	protected Map srcNodeValueMap = null;
	protected Map srcNodeIdMap = null;

	private boolean released = false;

	private final List<PropertyFilter> propFilters = new ArrayList();
	private final List<PropertyFilter> nodeFilters = new ArrayList();


	/**
	 * Constructor.
	 * 
	 * @param objGraph
	 *           {@link BidiGraphConfig}
	 */
	public GraphContextImpl(final BidiGraphConfig objGraph)
	{

		this.graphConfig = objGraph;
		if (filterList != null)
		{
			this.filterList = new ArrayList<PropertyFilter>(filterList);
		}
		this.srcNodeValueMap = new HashMap();
		this.srcNodeIdMap = new HashMap();
		this.attributes = new HashMap();
		// this.graphConfigImpl = new GraphConfigurationImpl(graphTransformer.getNodeMappingsMap());
		this.graphConfigImpl = new GraphConfigurationImpl(graphConfig.getNodes());

	}




	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphContext#getConfiguration()
	 */
	public GraphConfiguration getConfiguration()
	{
		return this.graphConfigImpl;
	}

	@Override
	public List<PropertyFilter> getNodeFilterList()
	{
		return this.nodeFilters;
	}

	@Override
	public List<PropertyFilter> getPropertyFilterList()
	{
		return this.propFilters;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphContext#isReleased()
	 */
	public boolean isReleased()
	{
		return released;
	}

	/**
	 * @param isReleased
	 */
	public void setReleased(final boolean isReleased)
	{
		this.released = isReleased;
	}

	public Map getAttributes()
	{
		return this.attributes;
	}


	/**
	 * Returns the most far away distance which was detected from start node. If graph processing is finished, it is the highest
	 * distance for that graph at all.
	 * 
	 * @return highest distance (snapshot)
	 */
	public int getMaxDistance()
	{
		return this.maxFoundDistance;
	}


	public void setMaxDistance(final int distance)
	{
		this.maxFoundDistance = distance;
	}


	/**
	 * Returns the {@link BidiGraphConfig} which this context belongs to.
	 * 
	 * @return {@link BidiGraphConfig}
	 */
	public BidiGraphConfig getGraphConfig()
	{
		return this.graphConfig;
	}



	/**
	 * Returns a Map of already processed nodes.
	 * 
	 * @return Map
	 */
	public Map<Object, Object> getProcessedNodes()
	{
		return this.srcNodeValueMap;
	}

	public Map<Object, Object> getProcessedNodesId()
	{
		return this.srcNodeIdMap;
	}


	/**
	 * Creates an initial {@link NodeContext} (root node context)
	 * 
	 * @param rootNodeLookup
	 * @param nodeMapping
	 * @param source
	 * @return {@link NodeContext}
	 */
	public NodeContext createRootNodeContext(final CachedClassLookupMap<BidiNodeConfig> rootNodeLookup,
			final AbstractNodeConfig nodeMapping, final Object source)
	{
		NodeContext result = null;

		// configured nodeLookup for distance 1
		// final CachedClassLookupMap<NodeMapping> add = this.graphConfigImpl.getAllNodeMappings(1);
		final CachedClassLookupMap<BidiNodeConfig> add = this.graphConfigImpl.getAllNodeConfigs(1);

		// child nodes lookup is a merged result of current used node lookup and configured node lookup for next processing distance
		final CachedClassLookupMap<BidiNodeConfig> childNodesLookup = this.buildChildNodeLookup(rootNodeLookup, add);

		this.setRuntimeNodeMappings(0, rootNodeLookup);
		this.setRuntimeNodeMappings(1, childNodesLookup);

		if (log.isDebugEnabled())
		{
			log.debug("Added distance based runtime node lookup: " + 0 + ":" + rootNodeLookup.hashCode());
			log.debug("Added distance based runtime node lookup: " + 1 + ":" + childNodesLookup.hashCode());
		}


		// create result context
		result = new NodeContextImpl(this, null, nodeMapping, childNodesLookup, 0, 0, source);

		this.graphConfig.getContextListener().nodeContextCreated(result);

		return result;
	}



	private final Map<Integer, CachedClassLookupMap<BidiNodeConfig>> runtimeNodeMappings = new HashMap<Integer, CachedClassLookupMap<BidiNodeConfig>>();

	/**
	 * Returns an already calculated {@link CachedClassLookupMap}.
	 * 
	 * @param distance
	 * @return {@link CachedClassLookupMap}
	 */
	protected CachedClassLookupMap<BidiNodeConfig> getRuntimeNodeMappings(final int distance)
	{
		return this.runtimeNodeMappings.get(Integer.valueOf(distance));
	}

	protected void setRuntimeNodeMappings(final int distance, final CachedClassLookupMap<BidiNodeConfig> nodeLookup)
	{
		this.runtimeNodeMappings.put(Integer.valueOf(distance), nodeLookup);
	}




	protected CachedClassLookupMap<BidiNodeConfig> buildChildNodeLookup(final CachedClassLookupMap<BidiNodeConfig> base,
			final CachedClassLookupMap<BidiNodeConfig> add)
	{
		// by default result is same instance as base
		CachedClassLookupMap<BidiNodeConfig> result = base;

		// merge only when 'add' is not same as 'base'
		if (base != add)
		{
			// take static configuration
			final Collection<BidiNodeConfig> _add = add.getStaticMap().values();
			// and merge with 'base'
			result = buildChildNodeLookup(base, _add);
		}
		return result;
	}



	protected CachedClassLookupMap<BidiNodeConfig> buildChildNodeLookup(final CachedClassLookupMap<BidiNodeConfig> base,
			final Collection<BidiNodeConfig> add)
	{
		// by default result is same instance as base
		CachedClassLookupMap<BidiNodeConfig> result = base;

		// merge only when collection is not empty...
		if (add != null && !add.isEmpty())
		{
			// ... create a new result instance
			result = new CachedClassLookupMap<BidiNodeConfig>();
			// ... all static elements (no elements which were dynamically found) of base
			result.putAll(base.getStaticMap());
			// ... all passed NodeConfig elements
			for (final BidiNodeConfig nodeCfg : add)
			{
				result.put(nodeCfg.getType(), nodeCfg);
			}
		}
		return result;
	}




}
