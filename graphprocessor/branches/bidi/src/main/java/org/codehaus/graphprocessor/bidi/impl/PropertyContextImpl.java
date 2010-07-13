package org.codehaus.graphprocessor.bidi.impl;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.CachedClassLookupMap;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.BidiGraphContext;
import org.codehaus.graphprocessor.bidi.BidiNodeContext;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;



/**
 * See specification of {@link BidiPropertyContext}
 */
public class PropertyContextImpl implements BidiPropertyContext
{
	private static final Logger log = Logger.getLogger(PropertyContextImpl.class);

	private BidiPropertyConfig propertyConfig = null;
	private GraphContextImpl graphCtx = null;
	private NodeContextImpl parentNodeCtx = null;
	private CachedClassLookupMap<BidiNodeConfig> childNodeLookup = null;


	protected PropertyContextImpl(final GraphContextImpl graphCtx, final NodeContextImpl nodeCtx,
			final BidiPropertyConfig propertyMapping, final CachedClassLookupMap<BidiNodeConfig> nodeLookup)
	{
		this.propertyConfig = propertyMapping;
		this.graphCtx = graphCtx;
		this.parentNodeCtx = nodeCtx;
		this.childNodeLookup = nodeLookup;
	}


	@Override
	public BidiGraphContext getGraphContext()
	{
		return this.graphCtx;
	}



	@Override
	public BidiPropertyConfig getPropertyConfig()
	{
		return this.propertyConfig;
	}

	@Override
	public BidiNodeContext getParentContext()
	{
		return this.parentNodeCtx;
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyContext#getNodeMappingsMap()
	 */
	protected CachedClassLookupMap<BidiNodeConfig> getChildNodeLookup()
	{
		return this.childNodeLookup;
	}


	protected NodeContextImpl createChildNodeContext(final AbstractBidiNodeConfig nodeConfig, final Object source)
	{
		final int distance = this.parentNodeCtx.getRealDistance() + 1;
		final int virtualDist = nodeConfig.isVirtual() ? this.parentNodeCtx.getDistance() : this.parentNodeCtx.getDistance() + 1;

		// if needed: update highest found transformation distance
		if (this.graphCtx.getMaxDistance() <= virtualDist)
		{
			this.graphCtx.setMaxDistance(distance);
		}

		// TODO: use childNode
		CachedClassLookupMap<BidiNodeConfig> nodeLookup = this.graphCtx.getRuntimeNodeMappings(distance);

		if (nodeLookup == null)
		{
			// ...nodeLookup from this property
			final CachedClassLookupMap<BidiNodeConfig> base = this.getChildNodeLookup();
			final CachedClassLookupMap<BidiNodeConfig> merge = this.graphCtx.graphConfigImpl.getAllNodeConfigs(distance);

			// ...build
			nodeLookup = this.graphCtx.buildChildNodeLookup(base, merge);

			this.graphCtx.setRuntimeNodeMappings(distance, nodeLookup);

			log.debug("Added distance based runtime node lookup: " + distance + ":" + nodeLookup.hashCode());
		}


		// create result context
		final NodeContextImpl result = new NodeContextImpl(this.graphCtx, this, nodeConfig, nodeLookup, distance, virtualDist,
				source);

		this.graphCtx.getGraphConfig().getContextListener().nodeContextCreated(result);

		return result;
	}

	protected String createSourcePathString()
	{
		String result = this.parentNodeCtx.createSourcePathString();
		if (getPropertyConfig() != null)
		{
			result = result + ">" + getPropertyConfig().getName();
		}
		return result;
	}

	protected String createTargetPathString()
	{
		String result = this.parentNodeCtx.createTargetPathString();
		if (getPropertyConfig() != null)
		{
			result = result + ">" + getPropertyConfig().getName();
		}
		return result;
	}

}
