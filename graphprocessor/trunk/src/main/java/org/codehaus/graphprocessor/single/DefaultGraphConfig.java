package org.codehaus.graphprocessor.single;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractGraphConfig;



public class DefaultGraphConfig extends AbstractGraphConfig<DefaultNodeConfig>
{
	private static final Logger log = Logger.getLogger(DefaultGraphConfig.class);

	public DefaultGraphConfig()
	{
		super();
	}


	@Override
	protected DefaultNodeConfig createNodeConfig(final Class node)
	{
		return new DefaultNodeConfig(this, node);
	}



}
