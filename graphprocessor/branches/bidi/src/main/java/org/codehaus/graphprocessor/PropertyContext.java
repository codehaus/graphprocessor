package org.codehaus.graphprocessor;

import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;



/**
 * Provides context (runtime) information about current processed property.
 */
public interface PropertyContext
{
	/**
	 * Returns the {@link NodeContext} which was used to create this {@link PropertyContext}. Returned {@link NodeContext} is bound
	 * to a runtime value, which is a valid GraphNode (a {@link BidiNodeConfig} is available). It is the parent element of current
	 * processed runtime value which itself is of meta-type GraphProperty and represented by this {@link PropertyContext} instance.
	 * 
	 * @return {@link NodeContext}
	 */
	NodeContext getParentContext();

	/**
	 * Returns the {@link GraphContext}
	 * 
	 * @return {@link GraphContext}
	 */
	GraphContext getGraphContext();

	/**
	 * Returns the {@link BidiPropertyConfig} which is used for current property processing.
	 * 
	 * @return {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig getPropertyConfig();


}
