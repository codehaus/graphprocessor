package org.codehaus.graphprocessor;

import java.util.Collection;


public interface NodeProcessingUnit
{

	NodeConfig getNodeConfig();

	NodeProcessor getProcessor();

	Collection<PropertyProcessingUnit> getChildProcessingUnits();
}
