package org.codehaus.graphprocessor;

public interface ContextCreatedListener
{

	void graphContextCreated(GraphContext graphContext);

	void nodeContextCreated(NodeContext nodeContext);

	void propertyContextCreated(PropertyContext propertyContext);

	void nodeCreated(NodeContext nodeContext, Object node);
}
