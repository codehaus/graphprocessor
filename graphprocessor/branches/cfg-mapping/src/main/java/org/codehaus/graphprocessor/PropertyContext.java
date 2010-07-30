package org.codehaus.graphprocessor;




/**
 * Provides context (runtime) information about current processed property.
 */
public interface PropertyContext
{
	NodeContext getParentContext();

	GraphContext getGraphContext();

	PropertyProcessingUnit getProcessingUnit();


}
