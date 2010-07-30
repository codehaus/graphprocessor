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
package org.codehaus.graphprocessor.bidi;

import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;


public class ToUppercaseConverter implements PropertyInterceptor<String, String>
{

	@Override
	public String intercept(final BidiPropertyContext ctx, final String source)
	{
		return source.toUpperCase();
	}
}
