package org.codehaus.graphprocessor.bidi;

import org.codehaus.graphprocessor.GraphConfig;


public interface BidiGraphConfig extends GraphConfig
{
	BidiGraphConfig getTargetConfig();

	BidiNodeConfig getNodeConfig(Class type);

	BidiNodeConfig getAssignableNodeConfig(Class type);

}
