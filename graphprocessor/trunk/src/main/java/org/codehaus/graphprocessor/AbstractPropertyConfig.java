package org.codehaus.graphprocessor;

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



public abstract class AbstractPropertyConfig implements PropertyConfig, Initializable
{
	public static final int COMPLIANCE_LEVEL_LOW = 0;
	public static final int COMPLIANCE_LEVEL_MEDIUM = 1;
	public static final int COMPLIANCE_LEVEL_HIGH = 2;

	// PropertyConfig creation 
	// - uses a global (static) lookup cache (yes, static is intended here)
	// - does a factory bring advantages? 
	// (there were several issues with PropertyDescriptor read/write method detection so maybe that algorithm should be replaceable)
	private static Map<Class, Map<String, Method[]>> beanPropertyMap = new HashMap<Class, Map<String, Method[]>>();

	private static Pattern BEAN_GETTER = Pattern.compile("get(.*)");
	private static Pattern BEAN_BOOLEAN_GETTER = Pattern.compile("is(.*)");
	private static Pattern BEAN_SETTER = Pattern.compile("set(.*)");


	protected boolean isInitialized = false;
	protected NodeConfig nodeConfig = null;
	private List<PropertyFilter> propertyFilters = Collections.EMPTY_LIST;
	private PropertyProcessor propertyProcessor = null;
	private List<NodeConfig> nodeMappingList = Collections.EMPTY_LIST;


	public AbstractPropertyConfig(final NodeConfig nodeConfig)
	{
		this.nodeConfig = nodeConfig;
	}

	public NodeConfig getNodeConfig()
	{
		return this.nodeConfig;
	}


	/**
	 * @return the propertyProcessor
	 */
	public PropertyProcessor getProcessor()
	{
		return propertyProcessor;
	}

	/**
	 * @param propertyProcessor the propertyProcessor to set
	 */
	public void setProcessor(PropertyProcessor propertyProcessor)
	{
		this.propertyProcessor = propertyProcessor;
	}

	public List<NodeConfig> getNewNodeConfigs()
	{
		return nodeMappingList;
	}

	/**
	 * @param nodeConfig the nodeConfig to set
	 */
	public void setNewNodeMappings(final List<NodeConfig> nodeConfig)
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
	 * @param propertyFilters the propertyFilters to set
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
	 * @param complianceLevel various levels for error handling
	 * @return true when compiling was successful
	 */
	public abstract boolean initialize(final int complianceLevel);


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
	 * Result maps a property name to a {@link PropertyConfig}.
	 * </p>
	 * Any property which keeps java bean standard is found and used for {@link PropertyConfig} creation. For finding all
	 * properties {@link Introspector} is used which returns general {@link PropertyDescriptor}. But read- and write methods
	 * provided by {@link PropertyDescriptor} are only used as "suggestion" here and are getting post-processed to assure following
	 * criteria:
	 * <p/>
	 * - no bridge or synthetic methods are allowed <br/>
	 * - co-variant return types are handled correctly <br/>
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
