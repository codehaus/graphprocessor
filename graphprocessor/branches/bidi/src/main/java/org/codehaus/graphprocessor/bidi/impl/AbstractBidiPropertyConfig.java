package org.codehaus.graphprocessor.bidi.impl;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.GraphProperty;
import org.codehaus.graphprocessor.GraphPropertyInterceptor;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.PropertyFilter;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyProcessor;



public abstract class AbstractBidiPropertyConfig implements BidiPropertyConfig, Initializable
{

	// this is the name of the method defined by PropertyInterceptor interface
	private static final String INTERCEPT_METHOD_NAME;
	static
	{
		// should result in "intercept" (fetch method name in a refactoring safe manner)
		INTERCEPT_METHOD_NAME = PropertyInterceptor.class.getDeclaredMethods()[0].getName();
	}

	private static final Logger log = Logger.getLogger(AbstractBidiPropertyConfig.class);

	// PropertyConfig creation
	// - uses a global (static) lookup cache (yes, static is intended here)
	// - does a factory bring advantages?
	// (there were several issues with PropertyDescriptor read/write method detection so maybe that algorithm should be
	// replaceable)
	private static Map<Class, Map<String, Method[]>> beanPropertyMap = new HashMap<Class, Map<String, Method[]>>();

	private static Pattern BEAN_GETTER = Pattern.compile("get(.*)");
	private static Pattern BEAN_BOOLEAN_GETTER = Pattern.compile("is(.*)");
	private static Pattern BEAN_SETTER = Pattern.compile("set(.*)");


	protected boolean isInitialized = false;
	protected BidiNodeConfig nodeConfig = null;
	private List<PropertyFilter> propertyFilters = Collections.EMPTY_LIST;
	private BidiPropertyProcessor propertyProcessor = null;
	private List<BidiNodeConfig> nodeMappingList = Collections.EMPTY_LIST;


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


	public AbstractBidiPropertyConfig(final BidiNodeConfig nodeConfig, String id, String name)
	{
		this.nodeConfig = nodeConfig;
		this.id = id;
		this.name = name;

		final Map<String, Method[]> allProps = getPropertiesFor(nodeConfig.getType());
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
			log.debug("Property " + nodeConfig.getType() + "." + id + " not available");
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
	 * @param name
	 *           the name to set
	 */
	public void setName(final String name)
	{
		this.name = name;
	}


	public BidiNodeConfig getNodeConfig()
	{
		return this.nodeConfig;
	}


	/**
	 * @return the propertyProcessor
	 */
	public BidiPropertyProcessor getProcessor()
	{
		return propertyProcessor;
	}

	/**
	 * @param propertyProcessor
	 *           the propertyProcessor to set
	 */
	public void setProcessor(BidiPropertyProcessor propertyProcessor)
	{
		this.propertyProcessor = propertyProcessor;
	}

	public List<BidiNodeConfig> getNewNodeConfigs()
	{
		return nodeMappingList;
	}

	/**
	 * @param nodeConfig
	 *           the nodeConfig to set
	 */
	public void setNewNodeMappings(final List<BidiNodeConfig> nodeConfig)
	{
		this.nodeMappingList = nodeConfig;
	}



	/**
	 * @return the propertyFilters
	 */
	public List<PropertyFilter> getPropertyFilters()
	{
		return propertyFilters;
	}

	/**
	 * @param propertyFilters
	 *           the propertyFilters to set
	 */
	public void setPropertyFilters(List<PropertyFilter> propertyFilters)
	{
		this.propertyFilters = propertyFilters;
	}

	public boolean isInitialized()
	{
		return this.isInitialized;
	}

	public void setInitialized(final boolean initialized)
	{
		this.isInitialized = initialized;
	}

	/**
	 * Compiles configuration settings by assuming this property belongs to passed node which itself belongs to passed graph.
	 * 
	 * @param complianceLevel
	 *           various levels for error handling
	 * @return true when compiling was successful
	 */
	public abstract boolean initialize(final int complianceLevel);

	/**
	 * @return the readMethod
	 */
	public Method getReadMethod()
	{
		return readMethod;
	}

	/**
	 * @param readMethod
	 *           the readMethod to set
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
	 * @param writeMethod
	 *           the writeMethod to set
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
	 * 
	 * @param interceptor
	 *           the interceptor to set
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
	 * 
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

	/**
	 * Find the declared (non-bridged) 'intercept' method.
	 * 
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


	public static Map<String, Method[]> getPropertiesFor(final Class<?> type)
	{
		Map<String, Method[]> result = beanPropertyMap.get(type);
		if (result == null)
		{
			result = createBeanPropertyLookup(type);
			beanPropertyMap.put(type, result);
		}
		return result;
	}

	/**
	 * Creates a lookup map containing all properties of passed type.
	 * <p/>
	 * Result maps a property name to a {@link BidiPropertyConfig}.
	 * </p>
	 * Any property which keeps java bean standard is found and used for {@link BidiPropertyConfig} creation. For finding all
	 * properties {@link Introspector} is used which returns general {@link PropertyDescriptor}. But read- and write methods
	 * provided by {@link PropertyDescriptor} are only used as "suggestion" here and are getting post-processed to assure following
	 * criteria:
	 * <p/>
	 * - no bridge or synthetic methods are allowed <br/>
	 * - co-variant return types are handled correctly <br/>
	 * 
	 * @param type
	 * @return Map
	 */
	private static Map<String, Method[]> createBeanPropertyLookup(Class<?> type)
	{
		final Map<String, Method[]> result = new TreeMap<String, Method[]>();
		final Set<String> done = new HashSet<String>();
		while (type != null)
		{
			// we are only interested in declared methods (no bridge/synthetic ones)
			final Method[] methods = type.getDeclaredMethods();
			for (final Method method : methods)
			{
				// only public, non-bridged methods are of interest
				if (!method.isBridge() && Modifier.isPublic(method.getModifiers()))
				{
					// potential bean-getter property?
					if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class)
					{
						// not processed yet?
						final String methodName = method.getName();
						if (!done.contains(methodName))
						{
							done.add(methodName);

							final Matcher m = BEAN_GETTER.matcher(methodName);
							String propertyName = null;
							if (m.matches())
							{
								propertyName = m.group(1);
							}
							else
							{
								if (method.getReturnType().equals(boolean.class))
								{
									final Matcher m2 = BEAN_BOOLEAN_GETTER.matcher(methodName);
									if (m2.matches())
									{
										propertyName = m2.group(1);
									}
								}
							}

							if (propertyName != null)
							{
								propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

								// get or create a PropertyConfig
								Method[] pCfg = result.get(propertyName);
								if (pCfg == null)
								{
									pCfg = new Method[2];
									result.put(propertyName, pCfg);
								}
								pCfg[0] = method;
							}
						}
					}

					// potential bean-setter property?
					if (method.getParameterTypes().length == 1 && method.getReturnType() == void.class)
					{
						// not processed yet?
						final String methodName = method.getName();
						if (!done.contains(methodName))
						{
							done.add(methodName);
							final Matcher setter = BEAN_SETTER.matcher(methodName);
							if (setter.matches())
							{
								String propertyName = setter.group(1);
								propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);

								// get or create a PropertyConfig
								Method[] pCfg = result.get(propertyName);
								if (pCfg == null)
								{
									pCfg = new Method[2];
									result.put(propertyName, pCfg);
								}
								pCfg[1] = method;
							}
						}
					}
				}

			}
			type = type.getSuperclass();
		}
		return result;
	}

	public String toExtString()
	{
		return this.toString();
	}


}
