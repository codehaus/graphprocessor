/*
 * [y] hybris Platform
 *
 * Copyright (c) 2000-2010 hybris AG
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of hybris
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with hybris.
 * 
 *  
 */
package org.codehaus.graphprocessor.single;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractPropertyConfig;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.PropertyConfig;



/**
 * Default {@link PropertyConfig} implementation.
 */
public class DefaultPropertyConfig extends AbstractPropertyConfig
{
	private static final Logger log = Logger.getLogger(DefaultPropertyConfig.class);


	public DefaultPropertyConfig(final NodeConfig node, final String id, String name)
	{
		super(node, id, name);
	}


	@Override
	public boolean initialize(int complianceLevel)
	{
		return true;
	}


}
