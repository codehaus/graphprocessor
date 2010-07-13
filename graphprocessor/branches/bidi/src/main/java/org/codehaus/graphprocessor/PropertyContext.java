package org.codehaus.graphprocessor;



/**
 * Provides context (runtime) information about current processed property.
 */
public interface PropertyContext
{
	/**
	 * Returns the {@link NodeContext} which was used to create this {@link PropertyContext}. Returned {@link NodeContext} is bound
	 * to a runtime value, which is a valid GraphNode (a {@link NodeConfig} is available). It is the parent element of current
	 * processed runtime value which itself is of meta-type GraphProperty and represented by this {@link PropertyContext} instance.
	 * @return {@link NodeContext}
	 */
	NodeContext getParentContext();

	/**
	 * Returns the {@link GraphContext}
	 * @return {@link GraphContext}
	 */
	GraphContext getGraphContext();

	/**
	 * Returns the {@link PropertyConfig} which is used for current property processing.
	 * @return {@link PropertyConfig}
	 */
	PropertyConfig getPropertyConfig();


}
