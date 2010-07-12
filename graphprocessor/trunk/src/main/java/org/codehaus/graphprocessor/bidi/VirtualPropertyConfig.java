package org.codehaus.graphprocessor.bidi;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.codehaus.graphprocessor.PropertyFilter;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.PropertyProcessor;



public class VirtualPropertyConfig implements BidiPropertyConfig
{

	private final BidiPropertyConfig targetPropertyConfig;

	public VirtualPropertyConfig(BidiPropertyConfig targetProperty)
	{
		this.targetPropertyConfig = targetProperty;
	}

	@Override
	public String getId()
	{
		return targetPropertyConfig.getId();
	}


	@Override
	public String getName()
	{
		return targetPropertyConfig.getName();
	}

	@Override
	public List<BidiNodeConfig> getNewNodeConfigs()
	{
		return Collections.EMPTY_LIST;
	}

	@Override
	public BidiNodeConfig getNodeConfig()
	{
		return targetPropertyConfig.getNodeConfig().getTargetNodeConfig();
	}

	@Override
	public PropertyProcessor getProcessor()
	{
		return getNodeConfig().getPropertyProcessor(targetPropertyConfig.getWriteType());
	}

	@Override
	public List<PropertyFilter> getPropertyFilters()
	{
		return Collections.EMPTY_LIST;
	}

	@Override
	public PropertyInterceptor<Object, Object> getReadInterceptor()
	{
		return null;
	}

	@Override
	public Method getReadMethod()
	{
		return null;
	}

	@Override
	public Class<?> getReadType()
	{
		return null;
	}

	@Override
	public PropertyInterceptor<Object, Object> getWriteInterceptor()
	{
		return null;
	}

	@Override
	public Method getWriteMethod()
	{
		return null;
	}

	@Override
	public Class<?> getWriteType()
	{
		return null;
	}

	@Override
	public boolean isReadTypeCheckEnabled()
	{
		return false;
	}

	@Override
	public boolean isVirtualRead()
	{
		return false;
	}

	@Override
	public boolean isVirtualWrite()
	{
		return false;
	}

	@Override
	public boolean isWriteTypeCheckEnabled()
	{
		return false;
	}


	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.bidi.BidiPropertyConfig#getTargetProperty()
	 */
	@Override
	public BidiPropertyConfig getTargetProperty()
	{
		return targetPropertyConfig;
	}

	/*
	 * (non-Javadoc)
	 * @see de.hybris.platform.webservices.util.objectgraphtransformer.bidi.BidiPropertyConfig#isNode()
	 */
	@Override
	public boolean isNode()
	{
		return targetPropertyConfig.isNode();
	}

	@Override
	public String toExtString()
	{
		return "TODO";
	}






}
