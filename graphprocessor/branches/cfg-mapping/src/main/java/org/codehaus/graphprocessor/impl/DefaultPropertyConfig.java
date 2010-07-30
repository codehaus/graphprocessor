package org.codehaus.graphprocessor.impl;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.PropertyConfig;



public class DefaultPropertyConfig extends AbstractPropertyConfig implements PropertyConfig
{
	private static final Logger log = Logger.getLogger(DefaultPropertyConfig.class);


	public DefaultPropertyConfig(final NodeConfig node, final String sourceProperty)
	{
		super(node, sourceProperty);
	}



}
