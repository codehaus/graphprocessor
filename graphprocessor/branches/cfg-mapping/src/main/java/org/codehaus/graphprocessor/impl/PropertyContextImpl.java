package org.codehaus.graphprocessor.impl;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.NodeListener;
import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyContext;
import org.codehaus.graphprocessor.PropertyProcessingUnit;



public class PropertyContextImpl implements PropertyContext
{
	private static final Logger log = Logger.getLogger(PropertyContextImpl.class);

	private PropertyProcessingUnit processorUnit = null;
	private GraphContextImpl graphCtx = null;
	private NodeContextImpl parentNodeCtx = null;
	private CachedClassLookupMap<NodeConfig> childNodeLookup = null;


	protected PropertyContextImpl(final GraphContextImpl graphCtx, final NodeContextImpl nodeCtx,
			final PropertyProcessingUnit proccessorUnit, final CachedClassLookupMap<NodeConfig> nodeLookup)
	{
		this.processorUnit = proccessorUnit;
		this.graphCtx = graphCtx;
		this.parentNodeCtx = nodeCtx;
		this.childNodeLookup = nodeLookup;
	}


	@Override
	public GraphContext getGraphContext()
	{
		return this.graphCtx;
	}

	@Override
	public PropertyProcessingUnit getProcessingUnit()
	{
		return this.processorUnit;
	}



	@Override
	public NodeContext getParentContext()
	{
		return this.parentNodeCtx;
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyContext#getNodeMappingsMap()
	 */
	public CachedClassLookupMap<NodeConfig> getChildNodeLookup()
	{
		return this.childNodeLookup;
	}


	public NodeContextImpl createChildNodeContext(final AbstractNodeConfig nodeConfig, final Object source)
	{
		final int distance = this.parentNodeCtx.getRealDistance() + 1;
		final int virtualDist = nodeConfig.isVirtual() ? this.parentNodeCtx.getDistance() : this.parentNodeCtx.getDistance() + 1;

		// if needed: update highest found transformation distance
		if (this.graphCtx.getMaxDistance() <= virtualDist)
		{
			this.graphCtx.setMaxDistance(distance);
		}

		// TODO: use childNode
		CachedClassLookupMap<NodeConfig> nodeLookup = this.graphCtx.getRuntimeNodeMappings(distance);

		if (nodeLookup == null)
		{
			// ...nodeLookup from this property
			final CachedClassLookupMap<NodeConfig> base = this.getChildNodeLookup();
			final CachedClassLookupMap<NodeConfig> merge = this.graphCtx.graphConfigImpl.getAllNodeConfigs(distance);

			// ...build
			nodeLookup = this.graphCtx.buildChildNodeLookup(base, merge);

			this.graphCtx.setRuntimeNodeMappings(distance, nodeLookup);

			log.debug("Added distance based runtime node lookup: " + distance + ":" + nodeLookup.hashCode());
		}


		// create result context
		final NodeContextImpl result = new NodeContextImpl(this.graphCtx, this, nodeConfig, nodeLookup, distance, virtualDist,
				source);

		NodeListener<NodeContext> listener = graphCtx.getGraphConfig().getNodeListener();
		if (listener != null)
		{
			listener.nodeContextCreated(result);
		}


		return result;
	}

	public String createSourcePathString()
	{
		String result = this.parentNodeCtx.createSourcePathString();
		PropertyConfig cfg = getProcessingUnit().getPropertyConfig();
		if (cfg != null)
		{
			result = result + ">" + cfg.getName();
		}
		return result;
	}

	public String createTargetPathString()
	{
		String result = this.parentNodeCtx.createTargetPathString();
		PropertyConfig cfg = getProcessingUnit().getPropertyConfig();
		if (cfg != null)
		{
			result = result + ">" + cfg.getName();
		}
		return result;
	}

}
