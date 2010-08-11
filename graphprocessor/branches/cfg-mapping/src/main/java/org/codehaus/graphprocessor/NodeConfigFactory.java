package org.codehaus.graphprocessor;

public interface NodeConfigFactory
{
	NodeConfig createNodeConfig(GraphConfig graphConfig, Class type);

	NodeConfig getNodeConfig(Class type);
}
