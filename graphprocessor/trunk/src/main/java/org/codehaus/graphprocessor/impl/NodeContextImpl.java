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
import java.util.Collections;
import java.util.List;

import org.codehaus.graphprocessor.AbstractNodeConfig;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.PropertyConfig;




/**
 * See specification of {@link NodeContext}.
 */
public class NodeContextImpl implements NodeContext
{
	private AbstractNodeConfig nodeMapping = null;
	private Object sourceNodeValue = null;
	private Object targetNodeValue = null;

	private PropertyContextImpl parentPropertyCtx = null;
	private GraphContextImpl graphCtx = null;

	private int distance = 0;
	private int virtualDistance = 0;
	private CachedClassLookupMap<NodeConfig> childNodeLookup = null;


	/**
	 * @param graphCtx
	 * @param propertyCtx
	 * @param nodeMapping
	 * @param nodeMappingsMap
	 * @param distance
	 * @param virtualDistance
	 * @param source
	 */
	protected NodeContextImpl(final GraphContextImpl graphCtx, final PropertyContextImpl propertyCtx,
			final AbstractNodeConfig nodeMapping, final CachedClassLookupMap<NodeConfig> nodeMappingsMap, final int distance,
			final int virtualDistance, final Object source)
	{
		super();
		this.graphCtx = graphCtx;
		this.parentPropertyCtx = propertyCtx;
		if (this.parentPropertyCtx != null)
		{
			if (propertyCtx.getGraphContext() != this.graphCtx)
			{
				throw new GraphException(GraphContext.class.getSimpleName() + " of passed property is not same as of this node");
			}
		}
		this.nodeMapping = nodeMapping;

		this.sourceNodeValue = source;
		this.childNodeLookup = nodeMappingsMap;
		this.distance = distance;
		this.virtualDistance = virtualDistance;
	}


	/**
	 * @return the distance
	 */
	public int getRealDistance()
	{
		return distance;
	}

	/**
	 * @return the virtualDistance
	 */
	public int getDistance()
	{
		return virtualDistance;
	}



	/**
	 * @return the nodeConfig
	 */
	public AbstractNodeConfig getNodeConfig()
	{
		return nodeMapping;
	}


	/**
	 * @return the sourceNode
	 */
	public Object getSourceNodeValue()
	{
		return sourceNodeValue;
	}

	/**
	 * @param sourceNode
	 *           the sourceNode to set
	 */
	protected void setSourceNodeValue(final Object sourceNode)
	{
		this.sourceNodeValue = sourceNode;
	}


	/**
	 * @return the targetNode
	 */
	public Object getTargetNodeValue()
	{
		return targetNodeValue;
	}

	/**
	 * @param targetNode
	 *           the targetNode to set
	 */
	protected void setTargetNodeValue(final Object targetNode)
	{
		this.targetNodeValue = targetNode;
	}



	/**
	 * {@inheritDoc}
	 * <p/>
	 * This implementation of {@link NodeContext} returns always a {@link PropertyContextImpl} type.
	 */
	@Override
	public PropertyContextImpl getParentContext()
	{
		return this.parentPropertyCtx;
	}

	/**
	 * {@inheritDoc}
	 * <p/>
	 * This implementation of {@link NodeContext} returns always a {@link GraphContextImpl} type.
	 */
	@Override
	public GraphContextImpl getGraphContext()
	{
		return this.graphCtx;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.NodeContext#getPath()
	 */
	@Override
	public List<NodeContext> getProcessingPath()
	{
		final List<NodeContext> path = new ArrayList<NodeContext>();

		NodeContext nodeCtx = this;

		while (nodeCtx != null)
		{
			path.add(nodeCtx);
			if (nodeCtx.getParentContext() != null)
			{
				nodeCtx = nodeCtx.getParentContext().getParentContext();
			}
			else
			{
				nodeCtx = null;
			}
		}

		Collections.reverse(path);
		return path;
	}

	protected CachedClassLookupMap<NodeConfig> getChildNodeLookup()
	{
		return this.childNodeLookup;
	}


	protected PropertyContextImpl createChildPropertyContext(final PropertyConfig propertyConfig)
	{
		CachedClassLookupMap<NodeConfig> nodeConfig = this.childNodeLookup;
		if (propertyConfig != null)
		{
			// create node lookup for property childs based on:
			final List<NodeConfig> merge = propertyConfig.getNewNodeConfigs();
			nodeConfig = this.graphCtx.buildChildNodeLookup(nodeConfig, merge);
		}

		final PropertyContextImpl result = new PropertyContextImpl(this.graphCtx, this, propertyConfig, nodeConfig);

		this.graphCtx.getGraphConfig().getContextListener().propertyContextCreated(result);

		return result;
	}

	protected String createTargetPathString()
	{
		String result = "[" + (getTargetNodeValue() != null ? getTargetNodeValue().getClass().getSimpleName() : "null") + "]";
		if (this.parentPropertyCtx != null)
		{
			result = this.parentPropertyCtx.createTargetPathString() + ">" + result;
		}
		return result;
	}

	protected String createSourcePathString()
	{
		String result = "[" + (getSourceNodeValue() != null ? getSourceNodeValue().getClass().getSimpleName() : "null") + "]";
		if (this.parentPropertyCtx != null)
		{
			result = this.parentPropertyCtx.createSourcePathString() + ">" + result;
		}
		return result;
	}

}
