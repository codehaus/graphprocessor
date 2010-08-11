package org.codehaus.graphprocessor;

import java.util.Map;
import java.util.Set;


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
	 * return type is a {@link GraphNode} or if return type is a typed collection whose type is a {@link GraphNode} then this type
	 * is added to this graph too. Each child node gets inspected for new child nodes.
	 * 
	 * @param rootNode
	 *           the node which is added as root node
	 */
	void addNodes(Class<?> rootNode);

	/**
	 * Lookup for a {@link NodeConfig} for a given node type.
	 * <p/>
	 * Returns only a {@link NodeConfig} when such one is registered for requested type. Result is same like using
	 * {@link #getNodes()}
	 * 
	 * @param node
	 *           type of node for which a {@link NodeConfig} is requested
	 * @return {@link NodeConfig} or null
	 */
	// TODO: convenience method necessary?
	NodeConfig getNodeConfig(Class node);

	/**
	 * Lookup for a {@link NodeConfig} for given node type.
	 * <p/>
	 * Returns a {@link NodeConfig} when either such one is registered for requested type or when requested type is assignable to
	 * one of the registered types. Assignable means that either a super-class or an implemented interfaces of requested type is
	 * registered at this graph.
	 * 
	 * @param node
	 *           type of node for which a {@link NodeConfig} is requested
	 * @return {@link NodeConfig} or null
	 */
	NodeConfig getAssignableNodeConfig(Class node);

	// TODO: debug method
	Set<String> getDebugNodes();

	/**
	 * The default {@link NodeProcessor} which shall be used when processing a node of specified type.
	 * 
	 * @param nodeType
	 *           the type of node
	 * @return {@link NodeProcessor}
	 */
	NodeProcessor getNodeProcessor(Class nodeType);

	/**
	 * The default PropertyProcessor which shall be used when processing a property of specified type.
	 * 
	 * @param propertyType
	 *           the type of property
	 * @return {@link PropertyProcessor}
	 */
	PropertyProcessor getPropertyProcessor(Class propertyType);

	GraphListener<GraphContext> getGraphListener();

	NodeListener<NodeContext> getNodeListener();

	PropertyListener<PropertyContext> getPropertyListener();

	NodeConfigFactory getNodeConfigFactory();

}
