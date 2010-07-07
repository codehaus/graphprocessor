package org.codehaus.graphprocessor.single;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractGraphConfig;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.PropertyProcessor;
import org.codehaus.graphprocessor.impl.CachedClassLookupMap;
import org.codehaus.graphprocessor.impl.CollectionNodeProcessor;



public class DefaultGraphConfig extends AbstractGraphConfig
{
	private static final Logger log = Logger.getLogger(DefaultGraphConfig.class);
	
	protected final CachedClassLookupMap<NodeProcessor> nodeProcessorMap;
	protected final CachedClassLookupMap<PropertyProcessor> propertyProcessorMap;
	
	public DefaultGraphConfig() {
		this.nodeProcessorMap = new CachedClassLookupMap<NodeProcessor>();
		this.propertyProcessorMap = new CachedClassLookupMap<PropertyProcessor>();
		
		nodeProcessorMap.put(Collection.class, new CollectionNodeProcessor());
		nodeProcessorMap.put(Object.class, null);
	}


	@Override
	protected NodeConfig createNodeConfig(final Class node)
	{
		return new DefaultNodeConfig(this, node);
	}


	/* (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphConfig#getNodeProcessor(java.lang.Class)
	 */
	@Override
	public NodeProcessor getDefaultNodeProcessor(Class nodeType) {
		return nodeProcessorMap.get(nodeType);
	}


	/* (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.GraphConfig#getPropertyProcessor(java.lang.Class)
	 */
	@Override
	public PropertyProcessor getDefaultPropertyProcessor(Class propertyType) {
		return propertyProcessorMap.get(propertyType);
	}
	
	
 

}
