package org.codehaus.graphprocessor;


/**
 * Listener for node specific events.
 * 
 * @param <T>
 *           type of context object
 */
public interface NodeListener<T>
{
	/**
	 * Gets invoked whenever a new context object for node operations is created.
	 * 
	 * @param nodeContext
	 *           newly created context object
	 */
	void nodeContextCreated(T nodeContext);

	/**
	 * Gets invoked whenever a new node is created
	 * 
	 * @param nodeContext
	 *           context object
	 * @param node
	 *           newly created node
	 */
	void nodeCreated(T nodeContext, Object node);

}
