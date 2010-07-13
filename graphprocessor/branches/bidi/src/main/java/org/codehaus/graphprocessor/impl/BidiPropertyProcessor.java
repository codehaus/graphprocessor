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
package org.codehaus.graphprocessor.impl;




import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractNodeConfig;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.NodeProcessor;
import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyContext;
import org.codehaus.graphprocessor.PropertyFilter;
import org.codehaus.graphprocessor.PropertyProcessor;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.DefaultBidiNodeConfig;
import org.codehaus.graphprocessor.single.DefaultPropertyConfig;



// BidiPropertyProcessor
public class BidiPropertyProcessor implements PropertyProcessor
{
	private static final Logger log = Logger.getLogger(BidiPropertyProcessor.class);

	@Override
	public void process(final PropertyContext pCtx, final Object source, final Object target)
	{
		// TODO: type safety?
		final PropertyContextImpl pCtxImpl = (PropertyContextImpl) pCtx;
		final BidiPropertyConfig propertyConfig = (BidiPropertyConfig) pCtx.getPropertyConfig();


		final GraphContext graphCtx = pCtx.getGraphContext();

		if (propertyConfig instanceof Initializable)
		{
			Initializable init = (Initializable) propertyConfig;

			// lazy compile when necessary
			if (!init.isInitialized())
			{
				init.initialize(DefaultPropertyConfig.COMPLIANCE_LEVEL_HIGH);
			}

		}


		// DEBUG output
		// if (log.isDebugEnabled() && ((AbstractNodeMapping) pCtxImpl.getPropertyConfig().getParentMapping()).isDebugEnabled())
		if (log.isDebugEnabled() && ((DefaultBidiNodeConfig) pCtxImpl.getPropertyConfig().getNodeConfig()).isDebugEnabled())
		{
			final String logMsg = propertyConfig.toExtString();
			final String pre = "[" + pCtx.getParentContext().getRealDistance() + ":" + propertyConfig.getName() + "] config: ";
			log.debug(pre + logMsg);
		}

		// read property value from source node
		Object value = null;
		boolean isFiltered = false;

		if (propertyConfig.getReadMethod() != null)
		{
			value = this.readValueFromSource(pCtxImpl, source);

			// check filters
			// ... use global configured filters from GraphContext
			final List<PropertyFilter> globalFilters = graphCtx.getPropertyFilterList();
			// ... and local configured ones from current PropertyMapping
			final List<PropertyFilter> localFilters = propertyConfig.getPropertyFilters();
			// ... and apply
			isFiltered = this.isFilterd(pCtx, value, globalFilters, localFilters);

			// node transformation when necessary
			if (!isFiltered && value != null)
			{
				if (propertyConfig.isNode())
				{
					final AbstractNodeConfig nodeMapping = (AbstractNodeConfig) pCtxImpl.getChildNodeLookup().get(value.getClass());

					if (nodeMapping != null)
					{
						// check node filters
						isFiltered = this.isFilterd(pCtx, value, graphCtx.getNodeFilterList(), Collections.EMPTY_LIST);
						if (!isFiltered)
						{
							final NodeProcessor trans = nodeMapping.getProcessor();
							final NodeContext nodeCtx = pCtxImpl.createChildNodeContext(nodeMapping, value);
							value = trans.process(nodeCtx, value, null);
						}
					}
					else
					{
						// this should never happen
						// if yes an exception is not really necessary (log.error should do the job as well)
						throw new GraphException("Illegal graph config: need to transform a property but can't find a transformer");
					}
				}
			}
		}
		else
		{
			if (!propertyConfig.getTargetProperty().isVirtualWrite())
			{
				// IllegalState; should never happens
				throw new GraphException(propertyConfig.getName() + " has no read method and target write property is virtual");
			}
		}

		if (log.isDebugEnabled())
		{
			try
			{
				final String pre = "[" + pCtx.getParentContext().getRealDistance() + ":" + propertyConfig.getName() + "] actual: ";
				String read = "[virtual]";
				if (propertyConfig.getReadMethod() != null)
				{
					final Method readMethod = propertyConfig.getReadMethod();
					read = source.getClass().getSimpleName() + "#" + readMethod.getName() + "():"
							+ source.getClass().getMethod(readMethod.getName()).getReturnType().getSimpleName();
				}
				final String write = target.getClass().getSimpleName() + "#???";
				log.debug(pre + read + " -> " + write + "; (value:" + ((value != null) ? value.getClass().getSimpleName() : "null")
						+ ")");
			}
			catch (final Exception e)
			{
				e.printStackTrace();
			}
		}

		// can be filtered either by a PropertyFilter or, when node, additionally by a NodeFilter
		if (!isFiltered)
		{
			// target value is 'null' when any read-error occurs or conversion has failed
			// in any other case it's a converted source value or source value itself
			this.writeValueToTarget(pCtxImpl, target, value);
		}

	}

	protected boolean isFilterd(final PropertyContext pCtx, final Object value, final List<PropertyFilter> globalFilters,
			final List<PropertyFilter> localFilters)
	{
		boolean isFiltered = false;

		PropertyFilter filter = null;

		// check first list of filters
		for (final Iterator<PropertyFilter> iter = globalFilters.iterator(); iter.hasNext() && !isFiltered;)
		{
			filter = iter.next();
			isFiltered = filter.isFiltered(pCtx, value);
		}

		// check second list of filters
		for (final Iterator<PropertyFilter> iter = localFilters.iterator(); iter.hasNext() && !isFiltered;)
		{
			filter = iter.next();
			isFiltered = filter.isFiltered(pCtx, value);
		}

		if (log.isDebugEnabled() && isFiltered)
		{
			log.debug("Filter matched: " + filter.getClass().getSimpleName());
		}

		return isFiltered;
	}


	/**
	 * Reads a property value from source node.
	 * 
	 * @param pCtx
	 *           {@link PropertyContext}
	 * @param source
	 *           source node
	 * @return value
	 */
	private Object readValueFromSource(final PropertyContextImpl pCtx, final Object source)
	{
		final PropertyConfig pMap = pCtx.getPropertyConfig();

		Object value = null;
		// read and optionally convert value from source graph
		try
		{
			// read from source graph
			final Method readMethod = pMap.getReadMethod();
			value = readMethod.invoke(source, (Object[]) null);

			// convert (take interceptor from source graph property)
			if (pMap.getReadInterceptor() != null)
			{
				value = pMap.getReadInterceptor().intercept(pCtx, value);
			}
		}
		// only catch exceptions whose cause is method invocation
		catch (final InvocationTargetException e)
		{
			throw new GraphException("Error reading " + pCtx.createSourcePathString(), e);
		}
		catch (final IllegalAccessException e)
		{
			throw new GraphException("Error reading " + pCtx.createSourcePathString(), e);
		}
		return value;
	}


	// private void writeValueToTarget(final PropertyContextImpl pCtx, final Object target, Object value)
	// {
	// final PropertyConfig pMap = pCtx.getPropertyConfig();
	//
	// final Method writeMethod = pMap.getTargetConfig().getWriteMethod();
	//
	// // write target value
	// // target value is 'null' when any read-error occurs or conversion has failed
	// // in any other case it's a converted source value or source value itself
	//
	// // invoke interceptor (if available)
	// if (pMap.getTargetConfig().getWriteInterceptor() != null)
	// {
	// try
	// {
	// // when propertymapping is of type 'node' we have to do an additional compatibility check here because
	// // it may be, that a node was converted into an another type
	// // if (pCtx.getPropertyMapping().isNode())
	// // {
	// // final PropertyMapping propMap = pCtx.getPropertyMapping();
	// // final Class writeType = propMap.getTargetPropertyConfig().getWriteType();
	// // if (!writeType.isAssignableFrom(value.getClass()))
	// // {
	// // throw new GraphException("Property '" + propMap.getId() + "' was processed as node and transformed into "
	// // + value.getClass().getSimpleName() + " but interceptor needs " + writeType.getSimpleName());
	// // }
	// //
	// // }
	// value = pMap.getTargetConfig().getWriteInterceptor().intercept(pCtx, value);
	// }
	// // any kind of exception gets caught, useful log output gets produced
	// // property setter gets not invoked (method is left)
	// catch (final Exception e)
	// {
	// final String name = pMap.getTargetConfig().getWriteInterceptor().getClass().getSimpleName();
	// log.error("Error while processing write property interceptor '" + name + "' at property "
	// + pCtx.createTargetPathString(), e);
	// return;
	// }
	// }
	//
	// try
	// {
	// writeMethod.invoke(target, value);
	// }
	// catch (final Exception e)
	// {
	// log.error("Error writing " + pCtx.createTargetPathString());
	// if (writeMethod.getDeclaringClass().isAssignableFrom(target.getClass()))
	// {
	// final String actualType = (value != null) ? value.getClass().getName() : "null";
	// final String expectedType = writeMethod.getParameterTypes()[0].getName();
	// log.error("Error invoking method (used type '" + actualType + "' as parameter for '" + expectedType + "')", e);
	// }
	// else
	// {
	// log.error("Error invoking method '" + writeMethod.toString() + "' at class " + target.getClass().getSimpleName(), e);
	// }
	// }
	// }

	private void writeValueToTarget(final PropertyContextImpl pCtx, final Object target, Object value)
	{
		final BidiPropertyConfig pMap = (BidiPropertyConfig) pCtx.getPropertyConfig();

		final Method writeMethod = pMap.getTargetProperty().getWriteMethod();

		// write target value
		// target value is 'null' when any read-error occurs or conversion has failed
		// in any other case it's a converted source value or source value itself

		// invoke interceptor (if available)
		if (pMap.getTargetProperty().getWriteInterceptor() != null)
		{
			try
			{
				// when propertymapping is of type 'node' we have to do an additional compatibility check here because
				// it may be, that a node was converted into an another type
				// if (pCtx.getPropertyMapping().isNode())
				// {
				// final PropertyMapping propMap = pCtx.getPropertyMapping();
				// final Class writeType = propMap.getTargetPropertyConfig().getWriteType();
				// if (!writeType.isAssignableFrom(value.getClass()))
				// {
				// throw new GraphException("Property '" + propMap.getId() + "' was processed as node and transformed into "
				// + value.getClass().getSimpleName() + " but interceptor needs " + writeType.getSimpleName());
				// }
				//
				// }
				value = pMap.getTargetProperty().getWriteInterceptor().intercept(pCtx, value);
			}
			// any kind of exception gets caught, useful log output gets produced
			// property setter gets not invoked (method is left)
			catch (final Exception e)
			{
				final String name = pMap.getTargetProperty().getWriteInterceptor().getClass().getSimpleName();
				log.error(
						"Error while processing write property interceptor '" + name + "' at property " + pCtx.createTargetPathString(),
						e);
				return;
			}
		}

		try
		{
			writeMethod.invoke(target, value);
		}
		catch (final Exception e)
		{
			log.error("Error writing " + pCtx.createTargetPathString());
			if (writeMethod.getDeclaringClass().isAssignableFrom(target.getClass()))
			{
				final String actualType = (value != null) ? value.getClass().getName() : "null";
				final String expectedType = writeMethod.getParameterTypes()[0].getName();
				log.error("Error invoking method (used type '" + actualType + "' as parameter for '" + expectedType + "')", e);
			}
			else
			{
				log.error("Error invoking method '" + writeMethod.toString() + "' at class " + target.getClass().getSimpleName(), e);
			}
		}
	}


}
