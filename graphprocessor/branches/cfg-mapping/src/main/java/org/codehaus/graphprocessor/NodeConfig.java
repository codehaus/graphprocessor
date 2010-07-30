package org.codehaus.graphprocessor;

import java.util.Map;


public interface NodeConfig
{

	/**
	 * Returns a {@link PropertyConfig} for a mapping whose source and target property name are matching passed name.
	 * 
	 * @param propertyName
	 *           name of source and target property
	 * @return {@link PropertyConfig}
	 */
	PropertyConfig getPropertyConfig(String propertyName);

	PropertyConfig removePropertyConfig(String propertyName);

	GraphConfig getParentGraph();

	/**
	 * @return the nodeFactory
	 */
	NodeFactory getNodeFactory();

	boolean isDebugEnabled();



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

	Map<String, PropertyConfig> removeAllProperties();

	/**
	 * Returns a mapping of all {@link PropertyConfig} for this node.
	 * 
	 * @return {@link PropertyConfig} mapping
	 */
	Map<String, PropertyConfig> getProperties();

	boolean isVirtual();


}
