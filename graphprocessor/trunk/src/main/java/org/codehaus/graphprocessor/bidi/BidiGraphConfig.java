package org.codehaus.graphprocessor.bidi;

import org.codehaus.graphprocessor.GraphConfig;


public interface BidiGraphConfig extends GraphConfig
{
	public BidiGraphConfig getTargetConfig();

	public BidiNodeConfig getNodeConfig(final Class node);

	public BidiNodeConfig findNodeConfig(Class node);

}
