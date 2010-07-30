package org.codehaus.graphprocessor.bidi;




/**
 * Provides context (runtime) information about current processed property.
 */
public interface BidiPropertyContext
{
	/**
	 * Returns the {@link BidiNodeContext} which was used to create this {@link BidiPropertyContext}. Returned {@link BidiNodeContext} is bound
	 * to a runtime value, which is a valid GraphNode (a {@link BidiNodeConfig} is available). It is the parent element of current
	 * processed runtime value which itself is of meta-type GraphProperty and represented by this {@link BidiPropertyContext} instance.
	 * 
	 * @return {@link BidiNodeContext}
	 */
	BidiNodeContext getParentContext();

	/**
	 * Returns the {@link BidiGraphContext}
	 * 
	 * @return {@link BidiGraphContext}
	 */
	BidiGraphContext getGraphContext();

	/**
	 * Returns the {@link BidiPropertyConfig} which is used for current property processing.
	 * 
	 * @return {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig getPropertyConfig();


}
