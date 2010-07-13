package org.codehaus.graphprocessor;

import java.util.Map;

import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;


/**
 * Describes a single node from a graph.
 */
public interface NodeConfig
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
	PropertyConfig[] getUidProperties();

	/**
	 * Returns a {@link PropertyConfig} for a mapping whose source and target property name are matching passed name.
	 * 
	 * @param propertyName
	 *           name of source and target property
	 * @return {@link BidiPropertyConfig}
	 */
	PropertyConfig getPropertyConfigByName(String propertyName);

	PropertyConfig removePropertyConfigByName(String propertyName);

	Map<String, PropertyConfig> removeAllProperties();

	/**
	 * Returns a mapping of all {@link PropertyConfig} for this node.
	 * 
	 * @return {@link PropertyConfig} mapping
	 */
	Map<String, PropertyConfig> getProperties();

	GraphConfig getGraphConfig();

	boolean isVirtual();

	/**
	 * Returns the {@link NodeProcessor} which is used for processing this node.
	 * 
	 * @return {@link NodeProcessor}
	 */
	NodeProcessor getProcessor();

	PropertyProcessor getPropertyProcessor(Class propertyType);

}
