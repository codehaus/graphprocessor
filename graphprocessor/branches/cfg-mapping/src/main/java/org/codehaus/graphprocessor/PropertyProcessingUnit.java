package org.codehaus.graphprocessor;

import java.util.List;


public interface PropertyProcessingUnit
{
	PropertyConfig getPropertyConfig();

	PropertyProcessor getProcessor();

	boolean isNode();

	List<NodeProcessingUnit> getChildProcessingUnits();


}
