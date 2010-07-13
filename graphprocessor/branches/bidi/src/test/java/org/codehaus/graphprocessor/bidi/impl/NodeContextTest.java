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
package org.codehaus.graphprocessor.bidi.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.GraphConfiguration;
import org.codehaus.graphprocessor.bidi.BidiGraphContext;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeContext;
import org.codehaus.graphprocessor.bidi.BidiPropertyContext;
import org.codehaus.graphprocessor.impl.CachedClassLookupMap;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressModel;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryModel;
import org.codehaus.graphprocessor.samples.usergraph.TuUserDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserModel;
import org.codehaus.graphprocessor.transform.BidiGraphTransformer;
import org.codehaus.graphprocessor.transform.GraphTransformer;
import org.junit.Assert;
import org.junit.Test;



/**
 * Tests for correct context, their usage and their content. Checked context classes are: {@link BidiGraphContext},
 * {@link BidiNodeContext} and {@link BidiPropertyContext}.
 * <p/>
 * Some of the tested issues are:
 * <ul>
 * <li>are properties process in correct order</li>
 * <li>are distances information correct (including the virtual ones)</li>
 * <li>does child-node lookup works properly (including identity checks of used lookup-map)</li>
 * </ul>
 */
public class NodeContextTest
{

	private static final Logger log = Logger.getLogger(NodeContextTest.class);

	/**
	 * Special {@link GraphTransformer} which logs every {@link BidiNodeContext} which gets created.
	 */
	private static class TestGraphTransformer extends BidiGraphTransformer
	{
		private final List<List<BidiNodeContext>> loggedRuntimeNodes = new ArrayList<List<BidiNodeContext>>();

		public TestGraphTransformer(final Class clazz)
		{
			super(clazz);
		}

		@Override
		public void nodeContextCreated(final BidiNodeContext nodeCtx)
		{
			super.nodeContextCreated(nodeCtx);
			final List<BidiNodeContext> nodeCtxList = nodeCtx.getProcessingPath();
			this.loggedRuntimeNodes.add(nodeCtxList);
		}

		public List<List<BidiNodeContext>> getProcessedNodes()
		{
			return this.loggedRuntimeNodes;
		}
	}


	/**
	 * Tests processed properties for correct processing order. <br/>
	 * Tests {@link BidiNodeContext} for correct distance property
	 */
	@Test
	public void testPropOrderAndDistance()
	{
		final NodeContextTestSetup test = new NodeContextTestSetup(1);
		test.execute();

		final List<List<BidiNodeContext>> nodes = test.processedNodes;

		// ASSERTING

		// build 'expected' value (a String representation for each nodepath)
		// format is: [distance]:[sourceNodeType] -> [childNode]
		// note: this was used here before NodeCOntext#getPath was implemented
		final String[] expected = new String[]
		{ "0:TuUserDTO ", //
				"0:TuUserDTO 0:Collection ", //
				"0:TuUserDTO 0:Collection 1:TuAddressDTO ", //
				"0:TuUserDTO 0:Collection 1:TuAddressDTO 2:TuCountryDTO ", //
				"0:TuUserDTO 0:Collection 1:TuAddressDTO ", //
				"0:TuUserDTO 1:TuAddressDTO " };


		// build 'actual' value
		// i collect all information first instead of asserting each iteration step as this gives better
		// analysis/debug options in case (after a refactoring?) some errors occur
		final List actual = new ArrayList();
		for (final List<BidiNodeContext> nodePath : nodes)
		{
			String _actual = "";
			for (final BidiNodeContext nodeCtx : nodePath)
			{
				final int dist = ((NodeContextImpl) nodeCtx).getDistance();
				_actual = _actual + dist + ":" + nodeCtx.getNodeConfig().getType().getSimpleName() + " ";
			}
			actual.add(_actual);
		}

		// assert
		assertEquals(Arrays.asList(expected), actual);
	}

	/**
	 * Tests processed properties for correct processing order. <br/>
	 * Tests object which is used for {@link BidiNodeConfig} lookup for identity and/or equality.<br/>
	 * <p/>
	 * Test setup does not use collections.
	 */
	@Test
	public void testPropOrderAndNodeLookupInstance()
	{
		// TEST 1
		// no customization at all
		NodeContextTestSetup test = new NodeContextTestSetup(0);
		test.execute();

		// processed properties (name (or node type) and order)
		test.assertProcessedProperties("TuUserDTO|TuUserDTO,mainAddress|TuUserDTO,mainAddress,country|TuUserDTO,secondAddress|");

		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,1,-,-|0,1,1,1,-|0,1,1,1,1|0,1,1,1,-|");
		// nodeLookup: equality check
		assertEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));


		// TEST 2
		// distance based configuration at 0
		test = new NodeContextTestSetup(0);
		test.graphCtx.getConfiguration().addNodeConfig(0, test.STRING_NODECONFIG);
		test.execute();

		// processed properties (name (or node type) and order)
		test.assertProcessedProperties("TuUserDTO|TuUserDTO,mainAddress|TuUserDTO,mainAddress,country|TuUserDTO,secondAddress|");
		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,1,-,-|0,1,1,1,-|0,1,1,1,1|0,1,1,1,-|");

		// basic content check
		Assert.assertNull(test.getNodeConfigMapByMatrixId(0).get(String.class));
		Assert.assertEquals(test.STRING_NODECONFIG, test.getNodeConfigMapByMatrixId(1).get(String.class));

		// nodeLookup: equality check
		assertNotEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));


		// TEST 3
		// distance based configuration at 1
		test = new NodeContextTestSetup(0);
		GraphConfiguration graphConfiguration = test.graphCtx.getConfiguration();
		graphConfiguration.addNodeConfig(1, test.STRING_NODECONFIG);
		test.execute();

		// processed properties (name (or node type) and order)
		test.assertProcessedProperties("TuUserDTO|TuUserDTO,mainAddress|TuUserDTO,mainAddress,country|TuUserDTO,secondAddress|");
		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,2,-,-|0,1,2,2,-|0,1,2,2,2|0,1,2,2,-|");

		// basic content check
		// ... graphtransformer lookup (global graph configuration)
		Assert.assertNull(test.getNodeConfigMapByMatrixId(0).get(String.class));
		// ... initial root node lookup (global context configuration)
		Assert.assertNull(test.getNodeConfigMapByMatrixId(1).get(String.class));
		// ... first child node lookup
		Assert.assertEquals(test.STRING_NODECONFIG, test.getNodeConfigMapByMatrixId(2).get(String.class));


		// nodeLookup: equality check
		assertEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));
		assertNotEquals(test.getNodeConfigMapByMatrixId(1), test.getNodeConfigMapByMatrixId(2));

	}

	/**
	 * Tests processed properties for correct processing order. <br/>
	 * Tests object which is used for {@link BidiNodeConfig} lookup for identity and/or equality.<br/>
	 * <p/>
	 * Test setup does not use collections.
	 */
	@Test
	public void testPropOrderAndNodeLookupInstance2()
	{
		// TEST1
		// no distance based configuration at all
		NodeContextTestSetup test = new NodeContextTestSetup(1);
		test.execute();

		// processed properties (name (or node type) and order)
		final String exp = "TuUserDTO|TuUserDTO,addresses|TuUserDTO,addresses,TuAddressDTO|TuUserDTO,addresses,TuAddressDTO,country|TuUserDTO,addresses,TuAddressDTO|TuUserDTO,mainAddress|";
		test.assertProcessedProperties(exp);
		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,1,-,-,-|0,1,1,1,-,-|0,1,1,1,1,-|0,1,1,1,1,1|0,1,1,1,1,-|0,1,1,1,-,-|");

		// nodeLookup: equality check
		assertEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));

		// TEST2
		// distance based configuration at 0
		test = new NodeContextTestSetup(1);
		test.graphCtx.getConfiguration().addNodeConfig(0, test.STRING_NODECONFIG);
		test.execute();

		// processed properties (name (or node type) and order)
		test.assertProcessedProperties(exp);

		// basic content check
		Assert.assertNull(test.getNodeConfigMapByMatrixId(0).get(String.class));
		Assert.assertEquals(test.STRING_NODECONFIG, test.getNodeConfigMapByMatrixId(1).get(String.class));

		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,1,-,-,-|0,1,1,1,-,-|0,1,1,1,1,-|0,1,1,1,1,1|0,1,1,1,1,-|0,1,1,1,-,-|");

		// nodeLookup: equality check
		assertNotEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));


		// TEST3
		// distance based configuration at 1
		test = new NodeContextTestSetup(1);
		test.graphCtx.getConfiguration().addNodeConfig(1, test.STRING_NODECONFIG);
		test.execute();

		// processed properties (name (or node type) and order)
		test.assertProcessedProperties(exp);

		// basic content check
		Assert.assertNull(test.getNodeConfigMapByMatrixId(0).get(String.class));
		Assert.assertNull(test.getNodeConfigMapByMatrixId(1).get(String.class));
		Assert.assertEquals(test.STRING_NODECONFIG, test.getNodeConfigMapByMatrixId(2).get(String.class));

		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,2,-,-,-|0,1,2,2,-,-|0,1,2,2,2,-|0,1,2,2,2,2|0,1,2,2,2,-|0,1,2,2,-,-|");

		// nodeLookup: equality check
		assertEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));
		assertNotEquals(test.getNodeConfigMapByMatrixId(1), test.getNodeConfigMapByMatrixId(2));

	}


	/**
	 * Tests processed properties for correct processing order. <br/>
	 * Tests object which is used for {@link BidiNodeConfig} lookup for identity and/or equality.<br/>
	 * <p/>
	 * Test setup does use collections.
	 */
	@Test
	public void testPropOrderAndNodeLookupInstance3()
	{
		// TEST1
		// distance based configuration at 0
		NodeContextTestSetup test = new NodeContextTestSetup(1);

		// add new string node, overwrite existing address node
		test.graphCtx.getConfiguration().addNodeConfig(0, test.STRING_NODECONFIG);
		test.graphCtx.getConfiguration().addNodeConfig(0, test.ADDRESS_NODECONFIG);

		test.execute();

		// processed properties (name (or node type) and order)
		final String exp = "TuUserDTO|TuUserDTO,addresses|TuUserDTO,addresses,TuAddressDTO|TuUserDTO,addresses,TuAddressDTO,country|TuUserDTO,addresses,TuAddressDTO|TuUserDTO,mainAddress|";
		test.assertProcessedProperties(exp);
		// identity check: used map instances to lookup for child nodes
		test.assertUsedNodeLookupInstance("0,1,1,-,-,-|0,1,1,1,-,-|0,1,1,1,1,-|0,1,1,1,1,1|0,1,1,1,1,-|0,1,1,1,-,-|");

		// (un)equality check
		assertEquals(test.ADDRESS_NODECONFIG, test.getNodeConfigMapByMatrixId(1).get(TuAddressDTO.class));
		assertNotEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));
		assertNotSame(test.getNodeConfigMapByMatrixId(0).get(TuAddressDTO.class),
				test.getNodeConfigMapByMatrixId(1).get(TuAddressDTO.class));

		// TEST2
		// distance based configuration at 0 and 2
		test = new NodeContextTestSetup(1);

		// add new string node, overwrite existing address node
		test.graphCtx.getConfiguration().addNodeConfig(0, test.STRING_NODECONFIG);
		test.graphCtx.getConfiguration().addNodeConfig(1, test.ADDRESS_NODECONFIG);
		test.execute();

		test.assertProcessedProperties(exp);
		test.assertUsedNodeLookupInstance("0,1,2,-,-,-|0,1,2,2,-,-|0,1,2,2,2,-|0,1,2,2,2,2|0,1,2,2,2,-|0,1,2,2,-,-|");


		// (un)equality check
		assertNotEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));
		assertNotEquals(test.getNodeConfigMapByMatrixId(1), test.getNodeConfigMapByMatrixId(2));

		// TEST3
		// distance based configuration at 0 and 2
		test = new NodeContextTestSetup(1);

		// add new string node, overwrite existing address node
		test.graphCtx.getConfiguration().addNodeConfig(0, test.STRING_NODECONFIG);
		test.graphCtx.getConfiguration().addNodeConfig(1, test.ADDRESS_NODECONFIG);
		test.graphCtx.getConfiguration().addNodeConfig(2, test.LIST_NODECONFIG);
		test.execute();

		test.assertProcessedProperties(exp);
		test.assertUsedNodeLookupInstance("0,1,2,-,-,-|0,1,2,2,-,-|0,1,2,2,3,-|0,1,2,2,3,3|0,1,2,2,3,-|0,1,2,2,-,-|");

		// (un)equality check
		assertNotEquals(test.getNodeConfigMapByMatrixId(0), test.getNodeConfigMapByMatrixId(1));
		assertNotEquals(test.getNodeConfigMapByMatrixId(1), test.getNodeConfigMapByMatrixId(2));
		assertNotEquals(test.getNodeConfigMapByMatrixId(2), test.getNodeConfigMapByMatrixId(3));
	}

	@Test
	public void testSourceAndTargetValue()
	{
		final NodeContextTestSetup test = new NodeContextTestSetup(1);

		test.execute();

		final List<BidiNodeContext> nodeCtxList = test.processedNodes.get(3);

		BidiNodeContext node = nodeCtxList.get(0);
		Assert.assertNotNull(node.getSourceNodeValue());
		assertEquals(node.getSourceNodeValue().getClass(), TuUserDTO.class);
		assertEquals(node.getTargetNodeValue().getClass(), TuUserModel.class);
		Assert.assertSame(node.getSourceNodeValue(), test.userDto);

		node = nodeCtxList.get(1);
		Assert.assertNotNull(node.getSourceNodeValue());
		assertEquals(node.getSourceNodeValue().getClass(), ArrayList.class);
		assertEquals(node.getTargetNodeValue().getClass(), ArrayList.class);
		Assert.assertSame(node.getSourceNodeValue(), test.userDto.getAddresses());
		Assert.assertSame(node.getTargetNodeValue(), test.userModel.getAddresses());

		node = nodeCtxList.get(2);
		Assert.assertNotNull(node.getSourceNodeValue());
		assertEquals(node.getSourceNodeValue().getClass(), TuAddressDTO.class);
		assertEquals(node.getTargetNodeValue().getClass(), TuAddressModel.class);

		node = nodeCtxList.get(3);
		Assert.assertNotNull(node.getSourceNodeValue());
		assertEquals(node.getSourceNodeValue().getClass(), TuCountryDTO.class);
		assertEquals(node.getTargetNodeValue().getClass(), TuCountryModel.class);
	}

	@Test
	public void testNodePathForCollection()
	{
		// prepare test-setup 1
		final NodeContextTestSetup test = new NodeContextTestSetup(1);
		// but take only the addresses collection
		final Collection<TuAddressDTO> addresses = test.userDto.getAddresses();

		test.execute(addresses);
		test.assertProcessedProperties("Collection|Collection,TuAddressDTO|Collection,TuAddressDTO,country|Collection,TuAddressDTO|");
	}


	/**
	 * Provides two different test setups which are used for all tests here.
	 * <p/>
	 * Both setups are similar except that the second one uses an additional collection member.
	 */
	private class NodeContextTestSetup
	{
		private final DefaultBidiNodeConfig STRING_NODECONFIG;
		private final DefaultBidiNodeConfig ADDRESS_NODECONFIG;
		private final DefaultBidiNodeConfig LIST_NODECONFIG;

		TestGraphTransformer graph = null;
		GraphContextImpl graphCtx = null;
		TuUserDTO userDto = null;
		TuUserModel userModel = null;
		List<List<BidiNodeContext>> processedNodes = null;

		// a matrix [n x m] whose elements are numeric.
		// each number represents an object entity (identity id)
		private Object[][] identMatrix = null;

		// a list which holds each entity which was used to create the identity matrix
		// entity mapping is: "id of identMatrix" <-> "index of list element"
		private List<CachedClassLookupMap<BidiNodeConfig>> matrixIdList = null;

		private String identMatrixAsString = null;
		private String propertyMatrixAsString = null;


		/**
		 * Constructor.
		 */
		public NodeContextTestSetup(final int testSetupConfig)
		{
			// graph definition and graph context
			this.graph = new TestGraphTransformer(TuUserDTO.class);
			this.graphCtx = new GraphContextImpl(this.graph);

			STRING_NODECONFIG = new DefaultBidiNodeConfig(graph, String.class, String.class);
			ADDRESS_NODECONFIG = new DefaultBidiNodeConfig(graph, TuAddressDTO.class, TuAddressModel.class);
			LIST_NODECONFIG = new DefaultBidiNodeConfig(graph, List.class, List.class);

			// TestSetup 0:
			// create a user with two addresses
			// first address with a country
			// second address without a country
			if (testSetupConfig == 0)
			{
				final TuAddressDTO addr1 = new TuAddressDTO("first", "last");
				addr1.setCountry(new TuCountryDTO("country"));
				this.userDto = new TuUserDTO("userId");
				this.userDto.setMainAddress(addr1);
				this.userDto.setSecondAddress(new TuAddressDTO("first2, last2"));

				identMatrix = new Object[4][5];
			}

			// create a user, set an address which includes a country, set a collection of addresses
			if (testSetupConfig == 1)
			{
				final List<TuAddressDTO> addresses = new ArrayList<TuAddressDTO>();
				addresses.add(new TuAddressDTO("firstname1", "lastname1"));
				addresses.add(new TuAddressDTO("firstname2", "lastname2"));
				addresses.get(0).setCountry(new TuCountryDTO("id"));
				this.userDto = new TuUserDTO("userId");
				this.userDto.setMainAddress(new TuAddressDTO("first", "last"));
				this.userDto.setAddresses(addresses);

				identMatrix = new Object[6][6];
			}
		}


		/**
		 * Executes the test setup.
		 */
		public void execute()
		{
			this.userModel = this.execute(userDto);
		}

		/**
		 * Executes the test setup.
		 */
		public <T> T execute(final Object dto)
		{
			final T result = (T) graph.transform(graphCtx, dto);
			this.processedNodes = this.graph.getProcessedNodes();

			this.createIdentityMatrix();
			return result;
		}


		/**
		 * Returns a {@link CachedClassLookupMap} (node lookup map) which is mapped in the identity matrix under the given numeric
		 * id.
		 * 
		 * @param id
		 */
		public CachedClassLookupMap<BidiNodeConfig> getNodeConfigMapByMatrixId(final int id)
		{
			final CachedClassLookupMap<BidiNodeConfig> result = this.matrixIdList.get(id);
			return result;
		}

		/**
		 * @return identity matrix as string
		 */
		public String getIdentityMatrixAsString()
		{
			return this.identMatrixAsString;
		}

		/**
		 * @return property matrix as string
		 */
		public String getPropertyMatrixAsString()
		{
			return this.propertyMatrixAsString;
		}

		/**
		 * Creates the identity matrix.
		 * <p/>
		 * First matrix row:<br/>
		 * Element id's are always '0', entity is taken from @link DefaultGraphTransformer#getNodeConfigMap()}<br/>
		 * <p/>
		 * Second matrix row:<br/>
		 * Element id's are generally '1', entity is taken from {@link GraphContextImpl#getRuntimeNodeMappings(int)} whereas
		 * int-param is zero.
		 * <p/>
		 * Third row and following:<br/>
		 * Entities are taken from {@link NodeContextImpl#getChildNodeLookup()}
		 */
		private void createIdentityMatrix()
		{
			this.matrixIdList = new ArrayList<CachedClassLookupMap<BidiNodeConfig>>();

			// add nodelookup instances which are not available via logged NodeContext instances
			// ... static graphtransformer config (matrix id: 0)
			// CachedClassLookupMap<NodeConfig> nodeCfgMap = graph.getNodeMapping();
			CachedClassLookupMap<BidiNodeConfig> nodeCfgMap = new CachedClassLookupMap<BidiNodeConfig>(graph.getNodes());
			this.matrixIdList.add(nodeCfgMap);

			// ... initialy created lookup for distance zero (matrix id: 1)
			this.matrixIdList.add(this.graphCtx.getRuntimeNodeMappings(0));

			for (int i1 = 0; i1 < processedNodes.size(); i1++)
			{
				identMatrix[i1][0] = Integer.valueOf(0);
				identMatrix[i1][1] = Integer.valueOf(1);
				final List<BidiNodeContext> nodePath = processedNodes.get(i1);
				for (int i2 = 0; i2 < nodePath.size(); i2++)
				{
					final BidiNodeContext nodeCtx = nodePath.get(i2);
					Integer key = null;
					for (int i3 = 0; i3 < this.matrixIdList.size(); i3++)
					{
						// yes, identity check
						if (matrixIdList.get(i3) == ((NodeContextImpl) nodeCtx).getChildNodeLookup())
						{
							key = Integer.valueOf(i3);
							break;
						}
					}

					if (key == null)
					{
						key = Integer.valueOf(this.matrixIdList.size());
						this.matrixIdList.add(((NodeContextImpl) nodeCtx).getChildNodeLookup());
					}

					identMatrix[i1][i2 + 2] = key;
				}
			}

			this.identMatrixAsString = this.createIdentMatrixAsString();
			this.propertyMatrixAsString = this.createPropertyMatrixAsString();

			if (log.isDebugEnabled())
			{
				log.debug(this.identMatrixAsString);
			}
		}

		/**
		 * Asserts whether properties where processed in correct order.
		 */
		public void assertProcessedProperties(final String expectedProperties)
		{
			assertEquals(expectedProperties, this.getPropertyMatrixAsString());
		}

		/**
		 * Asserts whether identity matrix matches passed one.
		 */
		public void assertUsedNodeLookupInstance(final String expectedIdentityMatrix)
		{
			assertEquals(expectedIdentityMatrix, this.getIdentityMatrixAsString());
		}

		private String createIdentMatrixAsString()
		{
			String result = "";
			for (final Object[] objects : identMatrix)
			{
				for (final Object o : objects)
				{
					result = result + (o != null ? o.toString() : "-") + ",";
				}
				result = result.substring(0, result.length() - 1) + "|";
			}
			return result;
		}

		private String createPropertyMatrixAsString()
		{
			String result = "";
			for (final List<BidiNodeContext> nodeCtxList : this.processedNodes)
			{
				for (final BidiNodeContext nodeCtx : nodeCtxList)
				{
					String add = "";
					if (nodeCtx.getParentContext() != null && nodeCtx.getParentContext().getPropertyConfig() != null)
					{
						add = nodeCtx.getParentContext().getPropertyConfig().getName();
					}
					else
					{
						add = nodeCtx.getNodeConfig().getType().getSimpleName();
					}
					result = result + add + ",";
				}
				result = result.substring(0, result.length() - 1) + "|";
			}
			return result;
		}

	}


	/**
	 * Assert helper.
	 */
	static void assertNotEquals(final Object expected, final Object actual)
	{
		Assert.assertFalse(expected.equals(actual));
	}


	public static void main(final String[] argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.DEBUG);

		final NodeContextTest test = new NodeContextTest();
		test.testPropOrderAndNodeLookupInstance();
	}


}
