package org.codehaus.graphprocessor;

import java.util.Map;


/**
 * Describes a single node from a graph.
 */
public interface NodeConfig<T extends PropertyConfig, G extends GraphConfig>
{

	/**
	 * Returns the type of this node.
	 * 
	 * @return type of node
	 */
	Class<?> getType();

	/**
	 * Returns all Properties which are marked 'unique'.
	 * <p/>
	 * For more information see {@link GraphNode#uidProperties()}
	 * 
	 * @return List of {@link PropertyConfig}
	 */
	T[] getUidProperties();

	T getPropertyConfigByName(String propertyName);

	T removePropertyConfigByName(String propertyName);

	Map<String, T> removeAllProperties();

	/**
	 * Returns a mapping of all {@link PropertyConfig} for this node.
	 * 
	 * @return {@link PropertyConfig} mapping
	 */
	Map<String, T> getProperties();

	G getGraphConfig();

	boolean isVirtual();

	/**
	 * Returns the {@link NodeProcessor} which is used for processing this node.
	 * 
	 * @return {@link NodeProcessor}
	 */
	NodeProcessor getProcessor();

	PropertyProcessor getPropertyProcessor(Class propertyType);

}
