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
package org.codehaus.graphprocessor.bidi.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.codehaus.graphprocessor.GraphConfig;
import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.impl.DefaultNodeConfig;



public class BidiCollectionNodeConfig extends DefaultNodeConfig
{
	// private static final AbstractBidiNodeProcessor DEFAULT_NODE_PROCESSOR = new BidiCollectionNodeProcessor();

	public BidiCollectionNodeConfig(final GraphConfig graphConfig)
	{
		super(graphConfig);
		setType(Collection.class);
		// setProcessor(DEFAULT_NODE_PROCESSOR);

		setVirtual(true);
	}

	@Override
	public Map<String, PropertyConfig> getProperties()
	{
		return Collections.EMPTY_MAP;
	}

}
