package org.codehaus.graphprocessor;

import org.codehaus.graphprocessor.bidi.BidiGraphContext;
import org.codehaus.graphprocessor.bidi.BidiNodeContext;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;

public interface ContextCreatedListener
{

	void graphContextCreated(BidiGraphContext graphContext);

	void nodeContextCreated(BidiNodeContext nodeContext);

	void propertyContextCreated(BidiPropertyContext propertyContext);

	void nodeCreated(BidiNodeContext nodeContext, Object node);
}
