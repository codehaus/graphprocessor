package org.codehaus.graphprocessor.bidi.impl;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;



public class DefaultBidiPropertyConfig extends AbstractBidiPropertyConfig implements BidiPropertyConfig
{
	private static final Logger log = Logger.getLogger(DefaultBidiPropertyConfig.class);

	private static final BidiPropertyProcessorImpl DEFAULT_PROPERTY_PROCESSOR = new BidiPropertyProcessorImpl();

	private DefaultBidiPropertyConfig targetProperty = null;
	private boolean _isTypeCheckEnabled = false;
	private boolean _isNode = false;


	public DefaultBidiPropertyConfig(final BidiNodeConfig node, final String sourceProperty)
	{
		super(node, sourceProperty + "-" + sourceProperty, sourceProperty);
		this.targetProperty = new DefaultBidiPropertyConfig(node.getTargetNodeConfig(), sourceProperty, this);
		setProcessor(DEFAULT_PROPERTY_PROCESSOR);
	}

	public DefaultBidiPropertyConfig(final BidiNodeConfig node, final String sourceProperty, String targetProperty)
	{
		super(node, sourceProperty + "-" + targetProperty, sourceProperty);
		this.targetProperty = new DefaultBidiPropertyConfig(node.getTargetNodeConfig(), targetProperty, this);
		setProcessor(DEFAULT_PROPERTY_PROCESSOR);
	}


	private DefaultBidiPropertyConfig(final BidiNodeConfig node, final String sourceProperty,
			final DefaultBidiPropertyConfig target)
	{
		super(node, sourceProperty + "-" + target.getName(), sourceProperty);
		this.targetProperty = target;
		setProcessor(target.getProcessor());
	}


	public DefaultBidiPropertyConfig getTargetProperty()
	{
		return this.targetProperty;
	}

	void setTargetProperty(final DefaultBidiPropertyConfig target)
	{
		this.targetProperty = target;
	}

	@Override
	public BidiNodeConfig getNodeConfig()
	{
		return super.getNodeConfig();
	}


	public boolean isNode()
	{
		return this._isNode;
	}


	/**
	 * Compiles configuration settings by assuming this property belongs to passed node which itself belongs to passed graph.
	 * 
	 * @param complianceLevel
	 *           various levels for error handling
	 * @return true when compiling was successful
	 */
	@Override
	public boolean initialize(final int complianceLevel)
	{
		// // read-write PropertyConfig must be compiled in case this PropertyMapping was created with
		// // read/write property names (and no concrete read/write methods)
		// if (!this.isPropertyCfgInitialized)
		// {
		// final PropertyConfig pRead = nodeMapping.getSourceConfig().getProperties().get(this.readPropertyConfig.getName());
		// final PropertyConfig pWrite = nodeMapping.getTargetConfig().getProperties().get(this.writePropertyConfig.getName());
		// this.readPropertyConfig.mergeWith(pRead);
		// this.writePropertyConfig.mergeWith(pWrite);
		// this.isPropertyCfgInitialized = true;
		// }
		//
		// final Method writeMethod = getWriteMethod();

		this.isInitialized = this.isVirtualRead() || this.isVirtualWrite();
		boolean hasTarget = this.targetProperty != null && this.targetProperty.getWriteMethod() != null;

		if (hasTarget)
		{
			// this.readAnnotationConfiguration();
			// isInitialized = this.isVirtual();

			// read-property of source node type must provide read-method access
			// write-property of target node type must provide write-method access
			if (!isInitialized && getReadMethod() != null)
			{
				// type check (source read-type vs. target write type) is only enabled when
				// both, read- and write type check is enabled
				this._isTypeCheckEnabled = this.isReadTypeCheckEnabled() && this.targetProperty.isWriteTypeCheckEnabled();

				// ... having a NodeConfig for read-method return type: success
				final BidiNodeConfig nodeCfg = getNodeConfig().getGraphConfig().getAssignableNodeConfig(getReadType());
				if (nodeCfg != null)
				{
					// XXX: special handling for collections
					// no type check here because it's valid to have mismatches like List<->Set (CollectionNodeProcessor converts
					// automatically)
					if (Collection.class.isAssignableFrom(targetProperty.getWriteType()))
					{
						this._isNode = true;
						this.isInitialized = true;
					}
					else
					{
						final Class readType = nodeCfg.getTargetNodeConfig().getType();
						final Class writeType = targetProperty.getWriteType();

						// compiled successfully if read and write type are compatible
						// (including possible read/write interceptors)
						this.isInitialized = writeType.isAssignableFrom(readType);

						if (!this.isInitialized && !this._isTypeCheckEnabled)
						{
							this.isInitialized = readType.isAssignableFrom(writeType);
						}
						this._isNode = true;
					}
				}
				else
				{
					final Class readType = getReadType();
					final Class writeType = targetProperty.getWriteType();

					// compiled successfully if read and write type are compatible
					// (including possible read/write interceptors)
					this.isInitialized = writeType.isAssignableFrom(readType);

					if (!this.isInitialized && !this._isTypeCheckEnabled)
					{
						this.isInitialized = readType.isAssignableFrom(writeType);
					}
				}
			}
		}


		// debug
		if (log.isDebugEnabled() && getNodeConfig().isDebugEnabled())
		{
			final String action = this.isInitialized ? "Take " : "Skip ";
			final String logMsg = action + toExtString();
			log.debug(logMsg);
		}

		// error handling in case compilation fails
		if (!isInitialized)
		{
			final String logMsg = toExtString();
			if (complianceLevel == COMPLIANCE_LEVEL_HIGH)
			{
				throw new GraphException("Skip " + logMsg);
			}
			if (complianceLevel == COMPLIANCE_LEVEL_MEDIUM)
			{
				log.error(" Invalid " + logMsg);
			}
		}

		return isInitialized;
	}

	// private void readAnnotationConfiguration()
	// {
	// final Method write = this.targetProperty.getWriteMethod();
	// if (write != null && write.isAnnotationPresent(GraphProperty.class))
	// {
	// final GraphProperty writeAnno = write.getAnnotation(GraphProperty.class);
	// this._isVirtual = writeAnno.virtual();
	// }
	// }

	/**
	 * Enhanced toString representation. Use carefully as this method has an performance impact.
	 * 
	 * @return String representation
	 */
	@Override
	public String toExtString()
	{
		// read-information
		final String readPropName = this.getName();
		final Class readType = this.getReadType();
		final Method readMethod = this.getReadMethod();

		// write-information
		String writePropName = "[...]";
		Class writeType = null;
		Method writeMethod = null;
		PropertyInterceptor writeInterceptor = null;

		if (targetProperty != null)
		{
			writePropName = targetProperty.getName();
			writeType = targetProperty.getWriteType();
			writeMethod = targetProperty.getWriteMethod();
			writeInterceptor = targetProperty.getWriteInterceptor();
		}


		// create read-information part for final log message
		String read = "";
		if (isVirtualRead())
		{
			read = getNodeConfig().getType().getSimpleName() + "#[virtual]";
		}
		else
		{
			read = getNodeConfig().getType().getSimpleName() + "#" + readPropName;
			if (readMethod != null)
			{
				read = read + ":" + readMethod.getReturnType().getSimpleName();
			}
			read = read + " -> ";
		}
		final PropertyInterceptor readInterceptor = this.getReadInterceptor();
		if (readInterceptor != null)
		{
			read = read + readInterceptor.getClass().getSimpleName() + ":" + readType.getSimpleName() + " -> ";
		}

		// create write information part for final log message
		String write = "";
		if (writeInterceptor != null)
		{
			final Class writeConvReturnType = targetProperty.getInterceptMethod(writeInterceptor).getReturnType();
			final Class writeConvParamtype = targetProperty.getInterceptMethod(writeInterceptor).getParameterTypes()[1];
			write = " -> " + writeInterceptor.getClass().getSimpleName() + "(" + writeConvParamtype.getSimpleName() + ")" + ":"
					+ writeConvReturnType.getSimpleName();
		}

		write = write + " -> " + getNodeConfig().getTargetNodeConfig().getType().getSimpleName() + "#" + writePropName;
		if (writeMethod != null)
		{
			write = write + "(" + writeMethod.getParameterTypes()[0].getSimpleName() + ")";
		}



		final BidiNodeConfig nodeCfg = (readType != null) ? getNodeConfig().getGraphConfig().getNodeConfig(readType) : null;
		final String transformed = (nodeCfg != null) ? "[" + (nodeCfg).getTargetNodeConfig().getType().getSimpleName() + "]" : "[]";


		// add enabled/disabled flags ...
		String flags = "";
		// ... typecheck
		if (!_isTypeCheckEnabled)
		{
			flags = flags + " typecheck off";
		}

		if (flags.length() > 0)
		{
			flags = " (" + flags + ")";
		}


		// add conflicts
		String conflicts = "";
		if (!isInitialized())
		{
			// ... no read method (getter)?
			if (readMethod == null)
			{
				conflicts = conflicts + "no read method ";
			}

			// ... no write method (setter)?
			if (writeMethod == null)
			{
				conflicts = conflicts + "no write method";
			}


			if (readMethod != null && writeMethod != null)
			{
				final String fromType = nodeCfg == null ? readType.getSimpleName() : (nodeCfg).getTargetNodeConfig().getType()
						.getSimpleName();
				// ... read/write type not compatible (no node)
				conflicts = conflicts + "read<->write type mismatch (" + fromType + "<->" + writeType.getSimpleName();
			}

			if (conflicts.length() > 0)
			{
				conflicts = " (" + conflicts + ")";
			}
		}

		// start creating final log message
		final String logMsg = read + transformed + write + flags + conflicts;


		return logMsg;
	}

}
