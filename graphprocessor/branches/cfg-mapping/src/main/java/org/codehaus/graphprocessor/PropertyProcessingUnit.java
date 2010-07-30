package org.codehaus.graphprocessor;

public interface PropertyProcessingUnit
{
	int COMPLIANCE_LEVEL_LOW = 1;
	int COMPLIANCE_LEVEL_MEDIUM = 2;
	int COMPLIANCE_LEVEL_HIGH = 3;


	PropertyConfig getPropertyConfig();

	PropertyProcessor getProcessor();

	boolean isNode();

	boolean isTypeCheckEnabled();


}
