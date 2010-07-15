package org.codehaus.graphprocessor;


/**
 * Listener for property specific events
 * 
 * @param <T>
 *           type of context object
 */
public interface PropertyListener<T>
{
	/**
	 * Gets invoked whenever a new context object for property operations is created.
	 * 
	 * @param propertyContext
	 *           newly created context object
	 */
	void propertyContextCreated(T propertyContext);
}
