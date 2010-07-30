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
package org.codehaus.graphprocessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.impl.DefaultBidiGraphConfig;
import org.codehaus.graphprocessor.samples.misc.InDto1;
import org.codehaus.graphprocessor.samples.misc.InDto2;
import org.codehaus.graphprocessor.samples.misc.TestDTO;
import org.codehaus.graphprocessor.samples.misc.TestModel;
import org.codehaus.graphprocessor.samples.usergraph.TuUserDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserModel;
import org.codehaus.graphprocessor.transform.BidiGraphTransformer;
import org.codehaus.graphprocessor.transform.GraphTransformer;
import org.junit.Ignore;



/**
 * Failing tests or prototype testing.
 */
@Ignore
public class GraphSandboxTest
{
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(GraphSandboxTest.class);


	public void testCyclicCollectionReferences()
	{
		final List list = new ArrayList();
		list.add(new TuUserDTO("firstname1"));
		list.add("placeholder");
		list.add(new TuUserDTO("firstname3"));
		list.add(list);

		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		final List result = graph.transform(list);
		assertSame(result, result.get(3));

	}

	public void testBidiGraphConfig()
	{
		final BidiGraphConfig srcGraph = new DefaultBidiGraphConfig(TuUserDTO.class);
		final BidiGraphConfig dstGraph = srcGraph.getTargetConfig();

		// assert circular TuUser-NodeConfig
		final BidiNodeConfig srcNodeCfg = srcGraph.getNodeConfig(TuUserDTO.class);
		final BidiNodeConfig dstNodeCfg = dstGraph.getNodeConfig(TuUserModel.class);

		// assert circular address-PropertyConfig
		final BidiPropertyConfig srcPropCfg = srcNodeCfg.getPropertyConfigByName("mainAddress");
		final BidiPropertyConfig dstPropCfg = dstNodeCfg.getPropertyConfigByName("mainAddress");

		// do we want this?
		assertSame(dstPropCfg, srcPropCfg.getTargetProperty());
	}


	public void testBidiGraphWithCollectionRoot()
	{
		final GraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		List<TuUserDTO> dtoList = new ArrayList<TuUserDTO>();
		dtoList.add(new TuUserDTO("user"));

		List<TuUserModel> modelList = graph.transform(dtoList);
		assertEquals(TuUserModel.class, modelList.get(0).getClass());

		modelList = new ArrayList<TuUserModel>();
		modelList.add(new TuUserModel("user"));
		dtoList = graph.transform(modelList);
		assertEquals(TuUserDTO.class, dtoList.get(0).getClass());
	}


	public void testInheritedNodes()
	{
		final BidiGraphTransformer graph = new BidiGraphTransformer(InDto2.class);

		// is behavior: only the last node of inheritance tree is accepted
		// wanted: every node is accepted
		final BidiNodeConfig nodeCfg1 = graph.getNodeConfig(InDto1.class);
		final BidiNodeConfig nodeCfg2 = graph.getNodeConfig(InDto2.class);

		assertEquals(InDto1.class, nodeCfg1.getType().getClass());
		assertEquals(InDto2.class, nodeCfg2.getType().getClass());
	}

	public void testInterceptor()
	{
		BidiGraphTransformer graph = new BidiGraphTransformer(TestDTO.class);

		BidiNodeConfig cfg = graph.getNodeConfig(TestDTO.class);
		BidiPropertyConfig[] uidProps = cfg.getUidProperties();

		TestDTO dto = new TestDTO();
		dto.setName("name");
		dto.setPk(Long.valueOf(10));

		graph.transform(dto, new TestModel());

		System.out.print(uidProps);
	}

	// public void testManualChanges()
	// {
	// // final BidiGraphTransformer graph = new BidiGraphTransformer(ProductFeatureDTO.class);
	// // graph.addNodeType(ClassificationAttributeUnitDTO.class);
	// // graph.compile();
	//
	// // this one behaves different (correct behavior) to the code snippet above (not correct behavior)
	// final BidiGraphTransformer graph = new BidiGraphTransformer();
	// graph.addNodes(ProductFeatureDTO.class);
	// graph.addNodes(ClassificationAttributeUnitDTO.class);
	// graph.initialize();
	//
	// LOG.debug(graph.getNodeMapping(ProductFeatureModel.class).getPropertyMappings());
	// LOG.debug(graph.getNodeMapping(ProductFeatureDTO.class).getPropertyMappings());
	// }


	public void moretest()
	{
		// UIDProps which declares a non-existing property
	}

	public static void main(final String[] argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));

		Logger.getRootLogger().setLevel(Level.DEBUG);
		final GraphSandboxTest test = new GraphSandboxTest();
		test.testInterceptor();
		// test.testManualChanges();
	}
}
