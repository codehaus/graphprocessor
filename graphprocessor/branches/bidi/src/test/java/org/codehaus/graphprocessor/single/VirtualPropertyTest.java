package org.codehaus.graphprocessor.single;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.GraphProperty;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;
import org.codehaus.graphprocessor.transform.BidiGraphTransformer;
import org.junit.Assert;
import org.junit.Test;



public class VirtualPropertyTest
{
	public static class TestVirtualPropConverter implements PropertyInterceptor
	{
		@Override
		public Object intercept(final BidiPropertyContext ctx, final Object source)
		{
			// source node has no such property; property value must always be null
			Assert.assertNull(source);

			// take the parent nodes hash-code as value
			final Object node = ctx.getParentContext().getSourceNodeValue();
			return String.valueOf(node.hashCode());
		}
	}

	/**
	 * Use-case: generated POJO
	 */
	@GraphNode(target = TestVirtualPropTarget.class)
	public static class TestVirtualPropSource
	{
		private String value1;

		public String getValue1()
		{
			return value1;
		}
	}


	/**
	 * Use-case: hand-written POJO Setter of virtual property 'value2' must be invoked whenever TestVirtualProPSource (generated)
	 * gets transformed into TestVirtualPropTarget.
	 */
	@GraphNode(target = TestVirtualPropSource.class)
	public static class TestVirtualPropTarget
	{
		private String value1;
		private String value2;

		public String getValue1()
		{
			return value1;
		}

		public void setValue1(final String value1)
		{
			this.value1 = value1;
		}

		public String getValue2()
		{
			return this.value2;
		}

		@GraphProperty(virtual = true, interceptor = TestVirtualPropConverter.class)
		public void setValue2(final String value2)
		{
			this.value2 = value2;
		}
	}


	@Test
	public void testVirtualProperty()
	{
		final BidiGraphTransformer graph = new BidiGraphTransformer(TestVirtualPropSource.class);

		//
		// XXX: should consider to change behavior:
		// virtual setter properties of target are not part of source-node anymore
		// instead NodeProcessor has to ask target node for virtual properties directly and processes them after other props are
		// processed
		// 'virtual' will become a valid annotation property for getter and setter
		// (yes, even virtual get makes sense: e.g. when "splitting' property value into multiple ones) and set them directly at
		// target
		final BidiNodeConfig nodeConfig = graph.getNodeConfig(TestVirtualPropSource.class);
		Assert.assertNotNull(nodeConfig.getPropertyConfigByName("value1"));
		// XXX: according above changes: will never evaluate to true again
		Assert.assertNull(nodeConfig.getPropertyConfigByName("value2"));

		final TestVirtualPropSource source = new TestVirtualPropSource();
		source.value1 = "value1";

		final TestVirtualPropTarget result = graph.transform(source);
		Assert.assertEquals(source.getValue1(), result.getValue1());
		Assert.assertEquals(String.valueOf(source.hashCode()), result.getValue2());
	}

	public static void main(final String[] argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.DEBUG);
		final VirtualPropertyTest test = new VirtualPropertyTest();
		// test.testVirtualPropertyConfiguration();
		test.testVirtualProperty();
	}




}
