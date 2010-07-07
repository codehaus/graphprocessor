package org.codehaus.graphprocessor;

import java.util.Map;
import java.util.Set;


/**
 * Configuration settings for a graph.
 * 
 * @author denny.strietzbaum
 */
public interface GraphConfig
{
	/**
	 * Returns an immutable (unmodifiable) view of all configured node types with their appropriate {@link NodeConfig}.
	 * 
	 * @return Mapping between a node type and {@link NodeConfig}
	 */
	Map<Class<?>, NodeConfig> getNodes();

	/**
	 * Adds passed node including all child nodes which can be found to this graph.
	 * <p/>
	 * Passed node must be annotated with {@link GraphNode}. With reflection all getters are evaluated by their return type. If
	 * return type is a {@link GraphNode} if return type is a typed collection whose type is a {@link GraphNode} then this type is
	 * treated as child node.
	 * 
	 * @param rootNode
	 *           the node which is added as root node
	 */
	void addNodes(Class rootNode);

	NodeConfig getNodeConfig(Class node);

	NodeConfig findNodeConfig(Class node);

	Set<String> getDebugNodes();

	boolean isInitialized();

	ContextCreatedListener getContextListener();

	/**
	 * The default {@link NodeProcessor} which is used for processing nodes of specified type.
	 * 
	 * @param nodeType
	 * @return {@link NodeProcessor}
	 */
	NodeProcessor getNodeProcessor(Class nodeType);

	/**
	 * The default PropertyProcessor which is used for processing properties of specified type.
	 * 
	 * @param propertyType
	 * @return {@link PropertyProcessor}
	 */
	PropertyProcessor getPropertyProcessor(Class propertyType);

}
