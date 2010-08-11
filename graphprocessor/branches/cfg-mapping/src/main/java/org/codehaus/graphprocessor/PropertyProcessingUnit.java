package org.codehaus.graphprocessor;

public interface PropertyProcessingUnit
{
	PropertyConfig getPropertyConfig();

	PropertyProcessor getProcessor();

	boolean isNode();


}
