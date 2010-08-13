/*
 *	Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *                
 */
package org.codehaus.graphprocessor.transform;

import org.codehaus.graphprocessor.GraphContext;


/**
 * Transforms a graph or a subgraph from a source into a target representation. For each single transformation a
 * {@link GraphContext} is used. If no {@link GraphContext} is passed, an appropriate one gets created automatically.
 * <p/>
 * A {@link GraphContext} can be created manually with {@link #createGraphContext(Class)}. Such context can be used to customize
 * transformation behavior for exactly one transformation process. After that context can still be used to fetch some statistic
 * values but not for a second transformation process again.
 * 
 * @author denny.strietzbaum
 */
public interface GraphTransformer
{
	/**
	 * Calls {@link #transform(GraphContext, Object, Object)} with no specific context and no given target.
	 * 
	 * @param <T>
	 * @param source
	 *           source graph
	 * @return target graph
	 */
	<T extends Object> T transform(final Object source);

	/**
	 * Calls {@link #transform(GraphContext, Object, Object)} with no specific context.
	 * 
	 * @param <T>
	 * @param source
	 *           source graph
	 * @param target
	 *           target graph or null
	 * @return target graph
	 */
	<T extends Object> T transform(final Object source, final T target);


	/**
	 * Calls {@link #transform(GraphContext, Object, Object)} without a specific target.
	 * 
	 * @param <T>
	 * @param ctx
	 *           {@link GraphContext}
	 * @param source
	 *           source graph
	 * @return target graph
	 */
	<T extends Object> T transform(final GraphContext ctx, final Object source);



	/**
	 * Transforms a source object into a target one. Source is the graph (starting from root or any child node) which shall be
	 * transformed. If no context is passed, a default one will be created. If no target is passed, an appropriate one will be
	 * created. Return value is always 'target' regardless whether it was already passed or internally created.
	 * 
	 * @param <T>
	 * @param source
	 *           source graph
	 * @param target
	 *           target graph or null
	 * @return target graph
	 */
	<T extends Object> T transform(final GraphContext ctx, final Object source, T target);

	/**
	 * Creates a {@link GraphContext}. Such context can be used to customize transformation behavior or fetch some statistics after
	 * transformation.
	 * 
	 * @return {@link GraphContext}.
	 */
	// GraphContext createGraphContext();

	GraphContext createGraphContext(Class node);

}
