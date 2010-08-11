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
package org.codehaus.graphprocessor.bidi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.graphprocessor.GraphException;
import org.codehaus.graphprocessor.Initializable;
import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeContext;
import org.codehaus.graphprocessor.NodeFactory;
import org.codehaus.graphprocessor.NodeProcessingUnit;
import org.codehaus.graphprocessor.PropertyConfig;
import org.codehaus.graphprocessor.PropertyContext;
import org.codehaus.graphprocessor.PropertyProcessingUnit;
import org.codehaus.graphprocessor.PropertyProcessor;
import org.codehaus.graphprocessor.impl.BidiNodeProcessingUnit;
import org.codehaus.graphprocessor.impl.BidiPropertyProcessingUnit;
import org.codehaus.graphprocessor.impl.GraphContextImpl;
import org.codehaus.graphprocessor.impl.NodeContextImpl;
import org.codehaus.graphprocessor.impl.PropertyContextImpl;




/**
 * A default node transformer implementation which transforms a source type into a target type and copies various properties from
 * source to target instance.
 */
public class BidiNodeProcessor extends AbstractNodeProcessor
{
	private static final Logger log = Logger.getLogger(BidiNodeProcessor.class);

	@Override
	public <T> T process(final NodeContextImpl nodeCtx, final Object source, T target)
	{
		NodeProcessingUnit nodeProcUnit = nodeCtx.getProcessingUnit();

		// lazy NodeMapping compilation if necessary
		NodeConfig nodeCfg = nodeProcUnit.getNodeConfig();

		if (nodeCfg instanceof Initializable)
		{
			Initializable init = (Initializable) nodeCfg;
			if (!init.isInitialized())
			{
				init.initialize(0);
			}
		}

		final GraphContextImpl graphCtx = nodeCtx.getGraphContext();

		// update node context
		nodeCtx.setTargetNodeValue(target);

		// if no target is passed, an appropriate one must be provided somehow...
		if (target == null)
		{
			// get Map of already processed source nodes and their targets
			final Map processedNodes = graphCtx.getProcessedNodes();

			// target lookup strategy 1:
			// a 'target' instance is already available when e.g. nodes are referencing each other (cycles)
			// identity-lookup (keys are source node instances)
			target = (T) processedNodes.get(source);

			// update node context
			nodeCtx.setTargetNodeValue(target);


			// second: create a new instance for 'target'
			if (target == null)
			{
				// target lookup strategy 2:
				// equality lookup (keys are ID value based on UID property annotation of source node)
				// target lookup strategy 3:
				// merging: source node has a parent node which provides under current processed property a target node
				// target lookup strategy 4:
				// create a new instance
				target = (T) this.getOrCreateTargetNode(nodeCtx, source);

				processedNodes.put(source, target);

				// update node context
				nodeCtx.setTargetNodeValue(target);

				// we really should have 'target' now...
				if (target != null)
				{
					this.processProperties(nodeCtx, source, target);
				}
				else
				{
					log.error("Can't convert " + source.getClass());
				}
			}
		}
		else
		{
			// update identity map
			graphCtx.getProcessedNodes().put(source, target);

			// update equality map
			final Object nodeId = getValueId(nodeCtx, source);
			graphCtx.getProcessedNodesId().put(nodeId, target);

			this.processProperties(nodeCtx, source, target);
		}

		return target;
	}

	/**
	 * Processes all properties of current Node.
	 * 
	 * @param nodeCtx
	 *           {@link NodeContextImpl} current node context
	 * @param source
	 *           source node value
	 * @param target
	 *           target node value (never null)
	 */
	protected void processProperties(final NodeContextImpl nodeCtx, final Object source, final Object target)
	{
		// all properties which have to be processed (includes those which are itself nodes)
		BidiNodeProcessingUnit processingUnit = (BidiNodeProcessingUnit) nodeCtx.getProcessingUnit();
		Collection<BidiPropertyProcessingUnit> properties = (Collection) processingUnit.getChildProcessingUnits();

		for (final BidiPropertyProcessingUnit property : properties)
		{
			final PropertyContext propCtx = createChildPropertyContext(nodeCtx, property);
			PropertyProcessor processor = propCtx.getProcessingUnit().getProcessor();
			processor.process(propCtx, source, target);
		}

		// // NEW: process virtual write properties from target node
		// Map<String, BidiPropertyConfig> props = ((BidiNodeConfig) nodeCtx.getNodeConfig()).getTargetNodeConfig().getProperties();
		// for (Map.Entry<String, BidiPropertyConfig> entry : props.entrySet())
		// {
		// BidiPropertyConfig property = entry.getValue();
		// if (property.isVirtualWrite())
		// {
		// BidiPropertyConfig sourcePropertyConfig = new VirtualPropertyConfig(property);
		// final BidiPropertyContext propCtx = createChildPropertyContext(nodeCtx, sourcePropertyConfig);
		// property.getProcessor().process(propCtx, source, target);
		// }
		// }

	}

	protected PropertyContext createChildPropertyContext(final NodeContextImpl nodeCtx, final PropertyProcessingUnit processingUnit)
	{
		return nodeCtx.createChildPropertyContext(processingUnit);
	}

	private <T> T getOrCreateTargetNode(final NodeContextImpl nodeCtx, final Object srcNodeValue)
	{
		final GraphContextImpl ctx = nodeCtx.getGraphContext();

		// get UID
		final Object srcNodeUid = this.getValueId(nodeCtx, srcNodeValue);

		// lookup node cache whether a target node value is already available
		Object result = ctx.getProcessedNodesId().get(srcNodeUid);


		if (result != null)
		{
			final Object parentValue = this.getValueFromParentNode(nodeCtx);

			if (parentValue != null)
			{

				final Object pUid = this.getValueId(nodeCtx, parentValue);
				if (!pUid.equals(srcNodeUid))
				{
					throw new GraphException("Illeagal state");
				}
			}
		}
		else
		{
			// XXX: this one triggers the Getter (lazy-loading) of an optional available parent model
			result = this.getValueFromParentNode(nodeCtx);

			// strategy 1: ask nodefactory
			result = this.getValueFromNodeFactory(nodeCtx, srcNodeValue);

			// strategy 2: ask target element whether a node is already available
			if (result == null)
			{
				result = this.getValueFromParentNode(nodeCtx);
			}

			// strategy 3: use configured node mapping
			if (result == null)
			{
				NodeConfig targetNodeCfg = ((BidiNodeProcessingUnit) nodeCtx.getProcessingUnit()).getTargetNode().getNodeConfig();
				final Class<?> nodeType = targetNodeCfg.getType();
				result = this.createNode(nodeType);
				this.notifyNodeCreatedListener(nodeCtx, result);
			}
			ctx.getProcessedNodesId().put(srcNodeUid, result);
		}

		return (T) result;
	}

	// /**
	// * Get UID (if any) for passed node value.
	// *
	// * @param nodeCtx
	// * @param srcNodeValue
	// * @return
	// */
	// private Object getValueId(final NodeContext nodeCtx, final Object srcNodeValue)
	// {
	// Object result = this.getNodeValueUID(nodeCtx, srcNodeValue);
	// if (result == null)
	// {
	// result = Integer.valueOf(srcNodeValue.hashCode());
	// }
	// return result;
	// }

	// getValueUID
	private Object getValueId(final NodeContext nodeCtx, final Object srcNodeValue)
	{
		// take all properties which are configured to be taken for creation of a 'uid'
		final PropertyConfig[] uidProps = nodeCtx.getProcessingUnit().getNodeConfig().getUidProperties();
		Object result = null;
		if (uidProps != null && uidProps.length > 0)
		{
			result = "";
			for (final PropertyConfig pCfg : uidProps)
			{
				final Method m = pCfg.getReadMethod();
				if (m != null)
				{
					try
					{
						final Object id = m.invoke(srcNodeValue, (Object[]) null);
						if (id != null)
						{
							// XXX:
							result = id.toString();
						}
						else
						{
							result = null;
							break;
						}

					}
					catch (final Exception e)
					{
						result = null;
						break;
					}
				}
				else
				{
					result = null;
					break;
				}
			}
		}

		if (result == null)
		{
			result = Integer.valueOf(srcNodeValue.hashCode());
		}

		return result;
	}

	private Object getValueFromNodeFactory(final NodeContext nodeCtx, final Object srcNodeValue)
	{
		Object result = null;
		final NodeFactory factory = nodeCtx.getProcessingUnit().getNodeConfig().getNodeFactory();

		if (factory != null)
		{
			result = factory.getValue(nodeCtx, srcNodeValue);
		}
		return result;
	}


	/**
	 * Get node value from parent node value. Used to enable "merging" with already existing properties at target node
	 * 
	 * @param nodeCtx
	 * @return Object
	 */
	private Object getValueFromParentNode(final NodeContextImpl nodeCtx)
	{
		Object result = null;

		// take parent context
		final PropertyContextImpl parentPropCtx = nodeCtx.getParentContext();

		// root nodes have no parent
		// child nodes have at least two parents (the property and the node which this property belongs too))
		if (parentPropCtx != null && !parentPropCtx.getParentContext().getProcessingUnit().getNodeConfig().isVirtual())
		{
			// take current processing parent node value
			final Object pNodeValue = parentPropCtx.getParentContext().getTargetNodeValue();

			// get read method from target graphs write property
			// final Method readMethod = (parentPropCtx.getPropertyConfig()).getTargetProperty().getReadMethod();
			PropertyConfig targetPropCfg = ((BidiPropertyProcessingUnit) parentPropCtx.getProcessingUnit()).getTargetProperty();
			final Method readMethod = targetPropCfg.getReadMethod();
			if (readMethod != null)
			{
				try
				{
					result = readMethod.invoke(pNodeValue, (Object[]) null);
				}
				catch (final InvocationTargetException e)
				{
					throw new GraphException("Error reading " + parentPropCtx.createTargetPathString(), e);
				}
				catch (final IllegalAccessException e)
				{
					throw new GraphException("Error reading " + parentPropCtx.createTargetPathString(), e);
				}
			}
		}
		return result;
	}

}
