package org.codehaus.graphprocessor;


/**
 * Listener for graph specific events.
 * 
 * @param <T>
 *           type of graph context
 */
public interface GraphListener<T>
{
	/**
	 * Gets invoked whenever a new context object for a graph is created.
	 * 
	 * @param graphContext
	 *           newly created context object
	 */
	void graphContextCreated(T graphContext);
}
