package org.codehaus.graphprocessor.bidi;

import java.util.Map;
import java.util.Set;

import org.codehaus.graphprocessor.ContextCreatedListener;
import org.codehaus.graphprocessor.GraphNode;


public interface BidiGraphConfig
{
	BidiGraphConfig getTargetConfig();

	/**
	 * Returns an immutable (unmodifiable) view of all configured node types with their appropriate {@link BidiNodeConfig}.
	 * 
	 * @return Mapping between a node type and {@link BidiNodeConfig}
	 */
	Map<Class<?>, BidiNodeConfig> getNodes();

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
	 * Lookup for a {@link BidiNodeConfig} for a given node type.
	 * <p/>
	 * Returns only a {@link BidiNodeConfig} when such one is registered for requested type. Result is same like using
	 * {@link #getNodes()}
	 * 
	 * @param node
	 *           type of node for which a {@link BidiNodeConfig} is requested
	 * @return {@link BidiNodeConfig} or null
	 */
	// TODO: convenience method necessary?
	BidiNodeConfig getNodeConfig(Class node);

	/**
	 * Lookup for a {@link BidiNodeConfig} for given node type.
	 * <p/>
	 * Returns a {@link BidiNodeConfig} when either such one is registered for requested type or when requested type is assignable
	 * to one of the registered types. Assignable means that either a super-class or an implemented interfaces of requested type is
	 * registered at this graph.
	 * 
	 * @param node
	 *           type of node for which a {@link BidiNodeConfig} is requested
	 * @return {@link BidiNodeConfig} or null
	 */
	BidiNodeConfig getAssignableNodeConfig(Class node);

	// TODO: debug method
	Set<String> getDebugNodes();

	ContextCreatedListener getContextListener();

	/**
	 * The default {@link NodeProcessor} which shall be used when processing a node of specified type.
	 * 
	 * @param nodeType
	 *           the type of node
	 * @return {@link NodeProcessor}
	 */
	NodeProcessor getDefaultNodeProcessor(Class nodeType);

	/**
	 * The default PropertyProcessor which shall be used when processing a property of specified type.
	 * 
	 * @param propertyType
	 *           the type of property
	 * @return {@link PropertyProcessor}
	 */
	PropertyProcessor getDefaultPropertyProcessor(Class propertyType);

}
