package org.codehaus.graphprocessor.bidi;

import org.codehaus.graphprocessor.PropertyConfig;


public interface BidiPropertyConfig extends PropertyConfig
{
	BidiPropertyConfig getTargetProperty();

	BidiNodeConfig getNodeConfig();

	boolean isNode();

	String toExtString();



}
