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
package org.codehaus.graphprocessor;

import org.codehaus.graphprocessor.bidi.BidiPropertyContext;

/**
 * Intercepts processing of a property. A PropertyInterceptor can be assigned to a read- as well as a write property. In
 * case of a read-property, interceptor gets invoked immediately, after property was read (and before any other
 * operation like {@link PropertyFilter})
 * <p/>
 * In case of a write-property, interceptor gets invoked after every other operation is finished (like filters) but as
 * last step before property value gets written.
 * <p/>
 * With {@link BidiPropertyContext} an Interceptor is given full access to each graph-element which is and will be
 * processed. Therefore an Interceptor can be used as Converter, as Factory or just to do some other, implicit
 * processing stuff somewhere within the graph.
 * <p/>
 * 
 * @param <IN>
 *           the type of the unprocessed source value
 * @param <OUT>
 *           the type of the result, the processed source value
 */
public interface PropertyInterceptor<IN, OUT>
{
	/**
	 * Intercepts current property processing.
	 * 
	 * @param propertyCtx
	 *           {@link BidiPropertyContext}
	 * @param propertyValue
	 *           the unprocessed property value
	 * @return the processed property value
	 */
	OUT intercept(final BidiPropertyContext propertyCtx, final IN propertyValue);


}
