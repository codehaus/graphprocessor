package org.codehaus.graphprocessor.impl;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.PropertyProcessor;


public class BidiPropertyProcessingUnit extends PropertyProcessingUnitImpl
{
	private static final Logger log = Logger.getLogger(BidiPropertyProcessingUnit.class);

	private final PropertyConfig targetProperty;

	public BidiPropertyProcessingUnit(PropertyProcessor processor, PropertyConfig sourceProperty, PropertyConfig targetProperty)
	{
		super(processor, sourceProperty);
		this.targetProperty = targetProperty;
	}

	public PropertyConfig getTargetProperty()
	{
		return this.targetProperty;
	}

	@Override
	public String getId()
	{
		return getPropertyConfig().getName() + "-" + targetProperty.getName();
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
		PropertyConfig sourceProperty = getPropertyConfig();


		this.isInitialized = sourceProperty.isVirtualRead() || sourceProperty.isVirtualWrite();
		final boolean hasTarget = this.targetProperty != null && this.targetProperty.getWriteMethod() != null;

		if (hasTarget)
		{
			// this.readAnnotationConfiguration();
			// isInitialized = this.isVirtual();

			// read-property of source node type must provide read-method access
			// write-property of target node type must provide write-method access
			if (!isInitialized && sourceProperty.getReadMethod() != null)
			{
				// type check (source read-type vs. target write type) is only enabled when
				// both, read- and write type check is enabled
				isTypeCheckEnabled = sourceProperty.isReadTypeCheckEnabled() && targetProperty.isWriteTypeCheckEnabled();

				// ... having a NodeConfig for read-method return type: success
				Class sourceReadType = sourceProperty.getReadType();
				final NodeConfig nodeCfg = sourceProperty.getParentNode().getParentGraph().getAssignableNodeConfig(sourceReadType);
				if (nodeCfg != null)
				{
					// XXX: special handling for collections
					// no type check here because it's valid to have mismatches like List<->Set (CollectionNodeProcessor converts
					// automatically)
					if (Collection.class.isAssignableFrom(targetProperty.getWriteType()))
					{
						isNode = true;
						this.isInitialized = true;
					}
					else
					{
						final Class targetReadType = targetProperty.getReadType();
						final Class targetWriteType = targetProperty.getWriteType();

						// compiled successfully if read and write type are compatible
						// (including possible read/write interceptors)
						this.isInitialized = targetWriteType.isAssignableFrom(targetReadType);

						if (!this.isInitialized && !isTypeCheckEnabled)
						{
							this.isInitialized = targetReadType.isAssignableFrom(targetWriteType);
						}
						isNode = true;
					}
				}
				else
				{
					final Class targetWriteType = targetProperty.getWriteType();

					// compiled successfully if read and write type are compatible
					// (including possible read/write interceptors)
					this.isInitialized = targetWriteType.isAssignableFrom(sourceReadType);

					if (!this.isInitialized && !isTypeCheckEnabled)
					{
						this.isInitialized = sourceReadType.isAssignableFrom(targetWriteType);
					}
				}
			}
		}
		// else
		// {
		// // property is successfully initialized but still does not have a target
		// if (this.isInitialized)
		// {
		// final PropertyConfig propCfg = new VirtualPropertyConfig(this);
		// this.targetProperty = propCfg;
		// ((DefaultNodeConfig) this.nodeConfig.getTargetNodeConfig()).addPropertyConfig(propCfg);
		// }
		// }


		// debug
		if (log.isDebugEnabled() && sourceProperty.getParentNode().isDebugEnabled())
		{
			final String action = this.isInitialized ? "Take " : "Skip ";
			final String logMsg = action + toExtString();
			log.debug(logMsg);

			// if (isInitialized && !hasTarget)
			// {
			// log.debug("TAKE VIRTUALLY " + this.nodeConfig.getTargetNodeConfig().getType().getSimpleName() + "#" + this.getName());
			// }
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
	public String toExtString()
	{
		PropertyConfig sourceProp = getPropertyConfig();

		// read-information
		final String readPropName = sourceProp.getName();
		final Class readType = sourceProp.getReadType();
		final Method readMethod = sourceProp.getReadMethod();

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
		if (sourceProp.isVirtualRead())
		{
			read = sourceProp.getParentNode().getType().getSimpleName() + "#[virtual]";
		}
		else
		{
			read = sourceProp.getParentNode().getType().getSimpleName() + "#" + readPropName;
			if (readMethod != null)
			{
				read = read + ":" + readMethod.getReturnType().getSimpleName();
			}
			read = read + " -> ";
		}
		final PropertyInterceptor readInterceptor = sourceProp.getReadInterceptor();
		if (readInterceptor != null)
		{
			read = read + readInterceptor.getClass().getSimpleName() + ":" + readType.getSimpleName() + " -> ";
		}

		// create write information part for final log message
		String write = "";
		if (writeInterceptor != null)
		{
			final Method interceptMethod = AbstractPropertyConfig.getInterceptMethod(writeInterceptor);
			final Class writeConvReturnType = interceptMethod.getReturnType();
			final Class writeConvParamtype = interceptMethod.getParameterTypes()[1];
			write = " -> " + writeInterceptor.getClass().getSimpleName() + "(" + writeConvParamtype.getSimpleName() + ")" + ":"
					+ writeConvReturnType.getSimpleName();
		}

		write = write + " -> " + targetProperty.getParentNode().getType().getSimpleName() + "#" + writePropName;
		if (writeMethod != null)
		{
			write = write + "(" + writeMethod.getParameterTypes()[0].getSimpleName() + ")";
		}



		final NodeConfig nodeCfg = (readType != null) ? sourceProp.getParentNode().getParentGraph().getNodeConfig(readType) : null;
		final String transformed = (nodeCfg != null) ? "[" + targetProperty.getParentNode().getType().getSimpleName() + "]" : "[]";


		// add enabled/disabled flags ...
		String flags = "";
		// ... typecheck
		if (!isTypeCheckEnabled)
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
				final String fromType = nodeCfg == null ? readType.getSimpleName() : targetProperty.getParentNode().getType()
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
