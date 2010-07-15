package org.codehaus.graphprocessor;

public interface Initializable
{
	int COMPLIANCE_LEVEL_LOW = 0;
	int COMPLIANCE_LEVEL_MEDIUM = 1;
	int COMPLIANCE_LEVEL_HIGH = 2;

	boolean isInitialized();

	boolean initialize(int complianceLevel);

}
