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

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.AbstractPropertyConfig;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphProperty;
import org.codehaus.graphprocessor.GraphPropertyInterceptor;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyInterceptor;



/**
 * Default {@link PropertyConfig} implementation.
 */
public class DefaultPropertyConfig extends AbstractPropertyConfig
{
	private static final Logger log = Logger.getLogger(DefaultPropertyConfig.class);

	// this is the name of the method defined by PropertyInterceptor interface
	private static final String INTERCEPT_METHOD_NAME;
	static
	{
		// should result in "intercept" (fetch method name in a refactoring safe manner)
		INTERCEPT_METHOD_NAME = PropertyInterceptor.class.getDeclaredMethods()[0].getName();
	}

	private String id = null;
	private String name = null;
	private Method readMethod = null;
	private Method writeMethod = null;
	private PropertyInterceptor<?, ?> readInterceptor = null;
	private PropertyInterceptor<?, ?> writeInterceptor = null;
	private Class<?> readType = null;
	private Class<?> writeType = null;

	private boolean readTypeCheckEnabled = true;
	private boolean writeTypeCheckEnabled = true;

	private boolean virtualRead = false;
	private boolean virtualWrite = false;

	//	public DefaultPropertyConfig(final NodeConfig node, final String id)
	//	{
	//		this(node, id, id);
	//	}

	public DefaultPropertyConfig(final NodeConfig node, final String id, String name)
	{
		super(node);
		this.id = id;
		this.name = name;

		final Map<String, Method[]> allProps = getPropertiesFor(node.getType());
		final Method[] prop = allProps.get(name);

		if (prop != null)
		{
			if (prop[0] != null)
			{
				setReadMethod(prop[0]);
			}
			if (prop[1] != null)
			{
				setWriteMethod(prop[1]);
			}
		}
		else
		{
			log.debug("Property " + node.getType() + "." + id + " not available");
		}
	}


	@Override
	public String getId()
	{
		return this.id;
	}


	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}

	/**
	 * @return the readMethod
	 */
	public Method getReadMethod()
	{
		return readMethod;
	}

	/**
	 * @param readMethod the readMethod to set
	 */
	protected void setReadMethod(final Method readMethod)
	{
		this.readMethod = readMethod;

		if (this.readMethod != null)
		{
			if (this.readInterceptor == null)
			{
				this.readType = this.readMethod.getReturnType();
			}

			this.evaluateReadMethodAnnotation();
		}
	}

	/**
	 * @return the writeMethod
	 */
	public Method getWriteMethod()
	{
		return writeMethod;
	}

	/**
	 * @param writeMethod the writeMethod to set
	 */
	protected void setWriteMethod(final Method writeMethod)
	{
		this.writeMethod = writeMethod;
		if (writeMethod != null)
		{
			if (this.writeInterceptor == null)
			{
				this.writeType = this.writeMethod.getParameterTypes()[0];
			}
			this.evaluateWriteMethodAnnotation();

		}

	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyConfig#getReadInterceptor()
	 */
	public PropertyInterceptor getReadInterceptor()
	{
		return readInterceptor;
	}

	/**
	 * Sets a {@link PropertyInterceptor} which gets assigned to read-method of this property.
	 * @param interceptor the interceptor to set
	 */
	public void setReadInterceptor(final PropertyInterceptor interceptor)
	{
		this.readInterceptor = interceptor;
		if (this.readInterceptor != null)
		{
			// TODO: interceptor parameter check against read-method return type
			this.readType = this.getInterceptMethod(readInterceptor).getReturnType();
		}
		else
		{
			this.readType = this.readMethod != null ? this.readMethod.getReturnType() : null;
		}
	}


	@Override
	public PropertyInterceptor getWriteInterceptor()
	{
		return this.writeInterceptor;
	}

	/**
	 * Sets a {@link PropertyInterceptor} which gets assigned to write-method of this property.
	 * @param interceptor
	 */
	public void setWriteInterceptor(final PropertyInterceptor interceptor)
	{
		this.writeInterceptor = interceptor;
		if (this.writeInterceptor != null)
		{
			this.writeType = this.getInterceptMethod(writeInterceptor).getParameterTypes()[1];
		}
		else
		{
			this.writeType = this.writeMethod != null ? this.writeMethod.getParameterTypes()[0] : null;
		}
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyConfig#isTypeCheckEnabled()
	 */
	@Override
	public boolean isReadTypeCheckEnabled()
	{
		return this.readTypeCheckEnabled;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyConfig#isWriteTypeCheckEnabled()
	 */
	@Override
	public boolean isWriteTypeCheckEnabled()
	{
		return this.writeTypeCheckEnabled;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyConfig#getReadType()
	 */
	@Override
	public Class getReadType()
	{
		return this.readType;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.PropertyConfig#getWriteType()
	 */
	@Override
	public Class getWriteType()
	{
		return this.writeType;
	}



	private void evaluateReadMethodAnnotation()
	{
		final GraphProperty readAnno = readMethod.isAnnotationPresent(GraphProperty.class) ? readMethod
				.getAnnotation(GraphProperty.class) : null;

		if (readAnno != null)
		{
			this.readTypeCheckEnabled = readAnno.typecheck();
			this.virtualRead = readAnno.virtual();

			if (this.readInterceptor == null)
			{
				final Class interceptorClass = readAnno.interceptor();
				final PropertyInterceptor interceptor = this.createInterceptor(interceptorClass);
				this.setReadInterceptor(interceptor);

				if (interceptorClass.isAnnotationPresent(GraphPropertyInterceptor.class))
				{
					final GraphPropertyInterceptor interceptorAnno = (GraphPropertyInterceptor) interceptorClass
							.getAnnotation(GraphPropertyInterceptor.class);
					this.readTypeCheckEnabled = interceptorAnno.typecheck();
				}
			}
		}
	}

	private void evaluateWriteMethodAnnotation()
	{
		final GraphProperty writeAnno = writeMethod.isAnnotationPresent(GraphProperty.class) ? writeMethod
				.getAnnotation(GraphProperty.class) : null;

		if (writeAnno != null)
		{
			this.writeTypeCheckEnabled = writeAnno.typecheck();
			this.virtualWrite = writeAnno.virtual();
			if (writeInterceptor == null)
			{
				final Class interceptorClass = writeAnno.interceptor();
				final PropertyInterceptor interceptor = this.createInterceptor(interceptorClass);
				this.setWriteInterceptor(interceptor);

				if (interceptorClass.isAnnotationPresent(GraphPropertyInterceptor.class))
				{
					final GraphPropertyInterceptor interceptorAnno = (GraphPropertyInterceptor) interceptorClass
							.getAnnotation(GraphPropertyInterceptor.class);
					this.writeTypeCheckEnabled = interceptorAnno.typecheck();
				}
			}
		}

	}

	private PropertyInterceptor createInterceptor(final Class interceptor)
	{
		PropertyInterceptor result = null;
		if (interceptor != PropertyInterceptor.class)
		{
			try
			{
				result = (PropertyInterceptor) interceptor.newInstance();
			}
			catch (final Exception e)
			{
				throw new GraphException("Error creating " + PropertyInterceptor.class.getSimpleName() + " for property", e);
			}
		}
		return result;
	}

	/**
	 * Find the declared (non-bridged) 'intercept' method.
	 * @param interceptor
	 * @return {@link Method}
	 */
	protected Method getInterceptMethod(final PropertyInterceptor interceptor)
	{
		Method result = null;
		final Method[] declaredMethods = interceptor.getClass().getDeclaredMethods();
		for (final Method m : declaredMethods)
		{
			// synthetic (compiler-generated), bridge (compiler-generated to support generic interfaces).
			if (m.getName().equals(INTERCEPT_METHOD_NAME) && !m.isBridge())
			{
				result = m;
				break;
			}
		}
		return result;
	}




	//	protected void mergeWith(final PropertyConfig pCfg)
	//	{
	//		if (pCfg != null)
	//		{
	//			if (this.readMethod == null)
	//			{
	//				this.setReadMethod(pCfg.getReadMethod());
	//			}
	//
	//			if (this.writeMethod == null)
	//			{
	//				this.setWriteMethod(pCfg.getWriteMethod());
	//			}
	//
	//			if (this.readInterceptor == null)
	//			{
	//				this.setReadInterceptor(pCfg.getReadInterceptor());
	//			}
	//
	//			if (this.writeInterceptor == null)
	//			{
	//				this.setWriteInterceptor(pCfg.getWriteInterceptor());
	//			}
	//		}
	//	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.AbstractPropertyConfig#initialize(int)
	 */
	@Override
	public boolean initialize(final int complianceLevel)
	{
		return true;
	}


	@Override
	public boolean isVirtualRead()
	{
		return this.virtualRead;
	}

	public void setVirtualRead(boolean isVirtualRead)
	{
		this.virtualRead = isVirtualRead;
	}


	@Override
	public boolean isVirtualWrite()
	{
		return this.virtualWrite;
	}

	public void setVirtualWrite(boolean isVirtualWrite)
	{
		this.virtualWrite = isVirtualWrite;
	}


	@Override
	public String toString()
	{
		return this.nodeConfig.getType().getSimpleName() + "#" + this.getName();
	}
}
