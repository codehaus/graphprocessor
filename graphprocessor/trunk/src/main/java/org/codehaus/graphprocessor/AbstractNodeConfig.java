package org.codehaus.graphprocessor;

import java.util.Iterator;
import java.util.Map;



/**
 * Abstract base implementation for {@link NodeConfig}
 */
public abstract class AbstractNodeConfig implements NodeConfig, Initializable
{
	// // PropertyConfig creation
	// // - uses a global (static) lookup cache (yes, static is intended here)
	// // - does a factory bring advantages?
	// // (there were several issues with PropertyDescriptor read/write method detection so maybe that algorithm should be
	// replaceable)
	// private static Map<Class, Map<String, PropertyConfig>> propConfigMap = new HashMap<Class, Map<String, PropertyConfig>>();
	//
	// protected Map<String, PropertyConfig> getPropertiesFor(final Class<?> type)
	// {
	// Map<String, PropertyConfig> result = propConfigMap.get(type);
	// if (result == null)
	// {
	// result = createPropertiesFor(type);
	// propConfigMap.put(type, result);
	// }
	// return result;
	// }
	//
	//
	// private static Pattern BEAN_GETTER = Pattern.compile("get(.*)");
	// private static Pattern BEAN_BOOLEAN_GETTER = Pattern.compile("is(.*)");
	// private static Pattern BEAN_SETTER = Pattern.compile("set(.*)");

	protected boolean isNodeInitialized = false;
	protected boolean isPropertiesInitialized = false;

	private GraphConfig graphConfig = null;
	private NodeProcessor nodeProcessor = null;

	public AbstractNodeConfig(final GraphConfig graphConfig)
	{
		this.graphConfig = graphConfig;
	}

	@Override
	public GraphConfig getGraphConfig()
	{
		return this.graphConfig;
	}


	/**
	 * Initializes this node.
	 * <p/>
	 */
	public boolean initialize(int complianceLevel)
	{
		// step1: initialize node
		if (!this.isNodeInitialized)
		{
			this.isNodeInitialized = this.initializeNode();
		}

		if (!this.isPropertiesInitialized)
		{
			// step2: initialize properties; filter out invalid ones
			this.isPropertiesInitialized = initializeProperties();
		}
		return true;
	}

	protected abstract boolean initializeNode();

	/**
	 * Initializes each {@link PropertyConfig} of this node.
	 * <p/>
	 * If {@link PropertyConfig} is already initialized, it gets skipped.<br/>
	 * If {@link PropertyConfig} is not initialized, their initializer method gets called. If initialization fails, property gets
	 * removed from that node.
	 * @return true when initialization succeeds
	 */
	protected boolean initializeProperties()
	{
		// initialize/refresh properties (when not already done)
		final Map<String, PropertyConfig> properties = this.getProperties();
		for (final Iterator<PropertyConfig> iter = properties.values().iterator(); iter.hasNext();)
		{
			final PropertyConfig pCfg = iter.next();
			if (pCfg instanceof AbstractPropertyConfig)
			{
				final AbstractPropertyConfig aPropCfg = (AbstractPropertyConfig) pCfg;
				if (!aPropCfg.isInitialized())
				{
					final boolean valid = aPropCfg.initialize(AbstractPropertyConfig.COMPLIANCE_LEVEL_LOW);
					if (!valid)
					{
						iter.remove();
					}
				}
			}
			else
			{
				throw new GraphException("Need an instance of " + AbstractPropertyConfig.class.getSimpleName());
			}
		}
		return true;
	}

	public boolean isInitialized()
	{
		return this.isNodeInitialized && this.isPropertiesInitialized;
	}

	protected void setInitialized(final boolean initialized)
	{
		this.isNodeInitialized = initialized;
		this.isPropertiesInitialized = initialized;
	}



	// /**
	// * Creates a lookup map containing all properties of passed type.
	// * <p/>
	// * Result maps a property name to a {@link PropertyConfig}.
	// * </p>
	// * Any property which keeps java bean standard is found and used for {@link PropertyConfig} creation. For finding all
	// * properties {@link Introspector} is used which returns general {@link PropertyDescriptor}. But read- and write
	// * methods provided by {@link PropertyDescriptor} are only used as "suggestion" here and are getting post-processed
	// * to assure following criteria:
	// * <p/>
	// * - no bridge or synthetic methods are allowed <br/>
	// * - co-variant return types are handled correctly <br/>
	// *
	// * @param type
	// * @return
	// */
	// private Map<String, PropertyConfig> createPropertiesFor(Class<?> type)
	// {
	// final Map<String, PropertyConfig> result = new TreeMap<String, PropertyConfig>();
	// final Set<String> done = new HashSet<String>();
	// while (type != null)
	// {
	// // we are only interested in declared methods (no bridge/synthetic ones)
	// final Method[] methods = type.getDeclaredMethods();
	// for (final Method method : methods)
	// {
	// // only public, non-bridged methods are of interest
	// if (!method.isBridge() && Modifier.isPublic(method.getModifiers()))
	// {
	// // potential bean-getter property?
	// if (method.getParameterTypes().length == 0 && method.getReturnType() != void.class)
	// {
	// // not processed yet?
	// final String methodName = method.getName();
	// if (!done.contains(methodName))
	// {
	// done.add(methodName);
	//
	// final Matcher m = BEAN_GETTER.matcher(methodName);
	// String propertyName = null;
	// if (m.matches())
	// {
	// propertyName = m.group(1);
	// }
	// else
	// {
	// if (method.getReturnType().equals(boolean.class))
	// {
	// final Matcher m2 = BEAN_BOOLEAN_GETTER.matcher(methodName);
	// if (m2.matches())
	// {
	// propertyName = m2.group(1);
	// }
	// }
	// }
	//
	// if (propertyName != null)
	// {
	// propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
	//
	// // get or create a PropertyConfig
	// DefaultPropertyConfig pCfg = (DefaultPropertyConfig) result.get(propertyName);
	// if (pCfg == null)
	// {
	// pCfg = this.createPropertyConfig(propertyName);
	// result.put(propertyName, pCfg);
	// }
	// pCfg.setReadMethod(method);
	// }
	// }
	// }
	//
	// // potential bean-setter property?
	// if (method.getParameterTypes().length == 1 && method.getReturnType() == void.class)
	// {
	// // not processed yet?
	// final String methodName = method.getName();
	// if (!done.contains(methodName))
	// {
	// done.add(methodName);
	// final Matcher setter = BEAN_SETTER.matcher(methodName);
	// if (setter.matches())
	// {
	// String propertyName = setter.group(1);
	// propertyName = Character.toLowerCase(propertyName.charAt(0)) + propertyName.substring(1);
	//
	// // get or create a PropertyConfig
	// DefaultPropertyConfig pCfg = (DefaultPropertyConfig) result.get(propertyName);
	// if (pCfg == null)
	// {
	// pCfg = this.createPropertyConfig(propertyName);
	// result.put(propertyName, pCfg);
	// }
	// pCfg.setWriteMethod(method);
	// }
	// }
	// }
	// }
	//
	// }
	// type = type.getSuperclass();
	// }
	// return result;
	// }
	//
	// protected DefaultPropertyConfig createPropertyConfig(final String propertyName)
	// {
	// return new DefaultPropertyConfig(this, propertyName);
	// }

	public boolean isDebugEnabled()
	{
		return true;
	}

	/**
	 * @return the nodeProcessor
	 */
	public NodeProcessor getProcessor()
	{
		return nodeProcessor;
	}

	/**
	 * @param nodeProcessor
	 *           the nodeProcessor to set
	 */
	public void setProcessor(NodeProcessor nodeProcessor)
	{
		this.nodeProcessor = nodeProcessor;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.NodeConfig#getPropertyProcessor(java.lang.Class)
	 */
	@Override
	public PropertyProcessor getPropertyProcessor(Class propertyType)
	{
		return getGraphConfig().getPropertyProcessor(propertyType);
	}




}
