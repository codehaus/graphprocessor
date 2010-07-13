package org.codehaus.graphprocessor.bidi;

import java.util.Map;

import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.NodeFactory;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.PropertyProcessor;


public interface BidiNodeConfig
{

	BidiNodeConfig getTargetNodeConfig();

	/**
	 * Returns a {@link BidiPropertyConfig} for a mapping whose source and target property name are matching passed name.
	 * 
	 * @param propertyName
	 *           name of source and target property
	 * @return {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig getPropertyConfigByName(String propertyName);

	/**
	 * Returns a {@link BidiPropertyConfig} for passed property mappings.
	 * <p/>
	 * Property mapping is given as source property name and target property name. If target property name is 'null' it's assumed
	 * that it's name equals source property name.
	 * 
	 * @param sourcePropName
	 *           name of source property
	 * @param targetPropName
	 *           name of target property
	 * @return {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig getPropertyConfigByName(String sourcePropName, String targetPropName);

	BidiPropertyConfig removePropertyConfigByName(String propertyName);

	BidiPropertyConfig removePropertyConfigByName(String propertyName, String targetPropName);


	BidiGraphConfig getGraphConfig();

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
	 * @return List of {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig[] getUidProperties();

	Map<String, BidiPropertyConfig> removeAllProperties();

	/**
	 * Returns a mapping of all {@link BidiPropertyConfig} for this node.
	 * 
	 * @return {@link BidiPropertyConfig} mapping
	 */
	Map<String, BidiPropertyConfig> getProperties();

	boolean isVirtual();

	/**
	 * Returns the {@link NodeProcessor} which is used for processing this node.
	 * 
	 * @return {@link NodeProcessor}
	 */
	NodeProcessor getProcessor();

	PropertyProcessor getPropertyProcessor(Class propertyType);

}
