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
package org.codehaus.graphprocessor.transform;

import org.codehaus.graphprocessor.bidi.BidiGraphContext;


/**
 * Transforms a graph or a subgraph from a source into a target representation. For each single transformation a
 * {@link BidiGraphContext} is used. If no {@link BidiGraphContext} is passed, an appropriate one gets created automatically.
 * <p/>
 * A {@link BidiGraphContext} can be created manually with {@link #createGraphContext(Class)}. Such context can be used to customize
 * transformation behavior for exactly one transformation process. After that context can still be used to fetch some statistic
 * values but not for a second transformation process again.
 */
public interface GraphTransformer
{
	/**
	 * Calls {@link #transform(BidiGraphContext, Object, Object)} with no specific context and no given target.
	 * @param <T>
	 * @param source source graph
	 * @return target graph
	 */
	<T extends Object> T transform(final Object source);

	/**
	 * Calls {@link #transform(BidiGraphContext, Object, Object)} with no specific context.
	 * @param <T>
	 * @param source source graph
	 * @param target target graph or null
	 * @return target graph
	 */
	<T extends Object> T transform(final Object source, final T target);


	/**
	 * Calls {@link #transform(BidiGraphContext, Object, Object)} without a specific target.
	 * @param <T>
	 * @param ctx {@link BidiGraphContext}
	 * @param source source graph
	 * @return target graph
	 */
	<T extends Object> T transform(final BidiGraphContext ctx, final Object source);



	/**
	 * Transforms a source object into a target one. Source is the graph (starting from root or any child node) which shall be
	 * transformed. If no context is passed, a default one will be created. If no target is passed, an appropriate one will be
	 * created. Return value is always 'target' regardless whether it was already passed or internally created.
	 * @param <T>
	 * @param source source graph
	 * @param target target graph or null
	 * @return target graph
	 */
	<T extends Object> T transform(final BidiGraphContext ctx, final Object source, T target);

	/**
	 * Creates a {@link BidiGraphContext}. Such context can be used to customize transformation behavior or fetch some statistics after
	 * transformation.
	 * @return {@link BidiGraphContext}.
	 */
	//GraphContext createGraphContext();

	BidiGraphContext createGraphContext(Class node);

}
