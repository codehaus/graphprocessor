package org.codehaus.graphprocessor.samples.misc;

import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.GraphProperty;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;


@GraphNode(target = TestModel.class, uidProperties = "pk")
public class TestDTO
{
	public static class StringToLongInterceptor implements PropertyInterceptor<String, Long>
	{
		@Override
		public Long intercept(BidiPropertyContext propertyCtx, String propertyValue)
		{
			return Long.valueOf(propertyValue);
		}
	}

	private Long pk;
	private String name;


	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Long getPk()
	{
		return pk;
	}

	@GraphProperty(interceptor = StringToLongInterceptor.class)
	public void setPk(Long pk)
	{
		this.pk = pk;
	}


}
