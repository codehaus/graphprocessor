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
package org.codehaus.graphprocessor.basic;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.PropertyContext;
import org.codehaus.graphprocessor.PropertyFilter;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;



public class ModifiedPropertyFilter implements PropertyFilter
{
	private static final Logger log = Logger.getLogger(ModifiedPropertyFilter.class);

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.proto.PropertyFilter#isFiltered(de.hybris.platform.
	 * webservices .objectgraphtransformer.ObjectGraphContext, java.lang.Object,
	 * de.hybris.platform.webservices.util.objectgraphtransformer.proto.PropertyProcessor)
	 */
	@Override
	public boolean isFiltered(final PropertyContext ctx, final Object value)
	{
		boolean isFiltered = false;
		final Object node = ctx.getParentContext().getSourceNodeValue();
		if (node instanceof ModifiedProperties)
		{
			final BidiPropertyConfig prop = ctx.getPropertyConfig();
			isFiltered = !((ModifiedProperties) node).getModifiedProperties().contains(prop.getName());
			if (log.isDebugEnabled())
			{
				log.debug("'" + prop.getName() + "' matched filter:" + isFiltered);
			}
		}
		return isFiltered;
	}


}
