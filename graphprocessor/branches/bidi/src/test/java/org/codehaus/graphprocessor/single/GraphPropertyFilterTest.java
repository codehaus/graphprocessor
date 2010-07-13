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
package org.codehaus.graphprocessor.single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collection;
import java.util.Collections;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.basic.BasicNodeFilter;
import org.codehaus.graphprocessor.basic.NullPropertyFilter;
import org.codehaus.graphprocessor.bidi.DefaultBidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiGraphContext;
import org.codehaus.graphprocessor.impl.GraphContextImpl;
import org.codehaus.graphprocessor.samples.productgraph.TpProductDTO;
import org.codehaus.graphprocessor.samples.productgraph.TpProductModel;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserModel;
import org.codehaus.graphprocessor.transform.BidiGraphTransformer;
import org.junit.Assert;
import org.junit.Test;



public class GraphPropertyFilterTest
{

	@Test
	public void transformWithFilter()
	{
		// each source member whose result is 'null' isn't injected into the target
		// create copy mapping
		final BidiGraphTransformer graph = new BidiGraphTransformer();
		graph.addNode(new DefaultBidiNodeConfig(graph, TpProductDTO.class, TpProductModel.class));

		final TpProductDTO source = new TpProductDTO(null, "dtoEan");
		final TpProductModel target = new TpProductModel();
		target.setCode("code");
		target.setEan("ean");

		// transform (1)
		graph.transform(source, target);

		// assert
		// - target member 'code' was overwritten by 'null' os source
		Assert.assertNull(target.getCode());
		assertEquals(source.getEan(), target.getEan());

		final NullPropertyFilter filter = new NullPropertyFilter();

		final BidiGraphContext ctx = new GraphContextImpl(graph);
		ctx.getPropertyFilterList().add(filter);

		// transform (2)
		target.setCode("code");
		graph.transform(ctx, source, target);

		//
		// TEST COLLECTIONS (null)
		//

		// assert
		assertEquals("code", target.getCode());
		assertEquals(source.getEan(), target.getEan());
	}

	@Test
	public void testDefaultNodeFilter()
	{
		// setup a simple graph
		final TuUserDTO userDto = new TuUserDTO("uid");
		final TuAddressDTO adr = new TuAddressDTO("firstname", "lastname");
		final TuCountryDTO country = new TuCountryDTO("country");
		adr.setCountry(country);
		adr.setOwner(userDto);
		userDto.setMainAddress(adr);

		// initialize graph; add filter manually
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);
		BidiGraphContext ctx = graph.createGraphContext();
		ctx.getNodeFilterList().add(new BasicNodeFilter());
		TuUserModel result = null;

		// TEST 1) no filtering at all
		result = graph.transform(ctx, userDto);

		// max distance was two vertices (or three nodes incl. root)
		assertNotNull(result.getMainAddress());
		assertEquals(2, ctx.getMaxDistance());
		assertEquals(result.getMainAddress().getCountry().getCode(), country.getCode());

		// TEST 2) filtering by a max allowed distance of 1
		ctx = graph.createGraphContext();
		ctx.getNodeFilterList().add(new BasicNodeFilter(1));

		result = graph.transform(ctx, userDto);

		assertEquals(1, ctx.getMaxDistance());
		assertNotNull(result.getMainAddress());
		assertNull(result.getMainAddress().getCountry());

		// TEST 3) leaf-processing with whitelist
		// maxAllowedDistance = 1 (includes TsAddressDTO); leaf processing for TsCountryDTO as whitelist)
		BasicNodeFilter filter = new BasicNodeFilter(1);
		filter.setLeafNodeProcessing((Collection) Collections.singleton(TuCountryDTO.class), true);
		ctx = graph.createGraphContext();
		ctx.getNodeFilterList().add(filter);

		result = graph.transform(ctx, userDto);

		assertEquals(2, ctx.getMaxDistance());
		assertNull(result.getMainAddress().getOwner());
		assertNotNull(result.getMainAddress().getCountry());
		assertEquals(country.getCode(), result.getMainAddress().getCountry().getCode());

		// TEST 3) leaf-processing with blacklist
		// maxAllowedDistance = 1 (includes TsAddressDTO); leaf processing for TsCountryDTO as blacklist)
		filter = new BasicNodeFilter(1);
		filter.setLeafNodeProcessing((Collection) Collections.singleton(TuCountryDTO.class), false);
		ctx = graph.createGraphContext();
		ctx.getNodeFilterList().add(filter);
		result = graph.transform(ctx, userDto);

		assertEquals(2, ctx.getMaxDistance());
		assertNotNull(result.getMainAddress().getOwner());
		assertNull(result.getMainAddress().getCountry());
	}

	public static void main(final String[] argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));
		//		Logger log = Logger.getLogger(ObjTree.class);
		//		log.setLevel(Level.DEBUG);
		//
		//		log = Logger.getLogger(DefaultObjTreeNodeCopier.class);
		//		log.setLevel(Level.DEBUG);

		Logger.getRootLogger().setLevel(Level.DEBUG);
		final GraphPropertyFilterTest test = new GraphPropertyFilterTest();
		test.testDefaultNodeFilter();
	}


}
