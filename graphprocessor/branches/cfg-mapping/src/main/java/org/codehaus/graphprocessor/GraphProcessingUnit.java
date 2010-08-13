package org.codehaus.graphprocessor;

import java.util.Collection;


public interface GraphProcessingUnit
{
	GraphConfig getGraphConfig();

	GraphProcessor getProcessor();

	Collection<NodeProcessingUnit> getChildProcessingUnits();

}
