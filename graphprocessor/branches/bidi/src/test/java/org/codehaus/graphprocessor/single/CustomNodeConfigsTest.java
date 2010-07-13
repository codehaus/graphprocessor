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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.AbstractPropertyConfig;
import org.codehaus.graphprocessor.GraphContext;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.DefaultBidiNodeConfig;
import org.codehaus.graphprocessor.bidi.DefaultBidiPropertyConfig;
import org.codehaus.graphprocessor.impl.GraphContextImpl;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressModel;
import org.codehaus.graphprocessor.samples.usergraph.TuUserDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserModel;
import org.codehaus.graphprocessor.transform.BidiGraphTransformer;
import org.junit.Test;



public class CustomNodeConfigsTest
{
	@Test
	public void testGlobalPropertyConfiguration()
	{
		// setup graph values (address name)
		final TuUserDTO user = new TuUserDTO();
		user.setMainAddress(new TuAddressDTO("firstname", "lastname"));

		// configure graph transformer
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		// TEST 1: remove a NodeProperty

		// retrieve NodeConfig for address node from transformer
		final DefaultBidiNodeConfig cfg = (DefaultBidiNodeConfig) graph.getNodeConfig(TuAddressDTO.class);

		// assert nodes properties as they are configured from transformer by default
		final Map<String, BidiPropertyConfig> m = cfg.getProperties();
		assertNotNull(cfg.getPropertyConfigByName("firstname"));
		assertNotNull(cfg.getPropertyConfigByName("lastname"));
		assertNotNull(cfg.getPropertyConfigByName("owner"));

		// remove 'firstname' node property from processing list
		cfg.removePropertyConfigByName("firstname");

		// transform
		TuUserModel model = graph.transform(user);

		// assert 'firstname' was not processed
		assertNull(model.getMainAddress().getFirstname());
		assertEquals("lastname", model.getMainAddress().getLastname());

		// TEST 2: Add a NodeProperty which maps different read/write methods

		// now add a new NodeProperty which maps lastname to firstname
		DefaultBidiPropertyConfig prop = new DefaultBidiPropertyConfig(cfg, "lastname", "firstname");
		prop.initialize(AbstractPropertyConfig.COMPLIANCE_LEVEL_HIGH);
		cfg.addPropertyConfig(prop);

		// transform
		model = graph.transform(user);

		// assert result
		assertNotNull(model.getMainAddress().getFirstname());
		assertEquals("lastname", model.getMainAddress().getFirstname());
		assertEquals("lastname", model.getMainAddress().getLastname());

		// TEST 3: Add a NodeProperty which uses a custom PropertyTransformer

		// define the property transformer

		// reconfigure graph
		cfg.removePropertyConfigByName("propertyId");
		prop = new DefaultBidiPropertyConfig(cfg, "firstname");
		prop.getTargetProperty().setWriteInterceptor(new ToUppercaseConverter());
		prop.initialize(AbstractPropertyConfig.COMPLIANCE_LEVEL_HIGH);
		cfg.addPropertyConfig(prop);

		// transform
		model = graph.transform(user);

		// assert result
		assertEquals("lastname", model.getMainAddress().getLastname());
		assertEquals("FIRSTNAME", model.getMainAddress().getFirstname());
	}



	/**
	 * Similar to first test but with additional transformation.
	 */
	@Test
	public void testDistanceBasedNodeConfiguration2()
	{
		// setup graph values (address name)
		final TuUserDTO user = new TuUserDTO();
		user.setMainAddress(new TuAddressDTO("firstname", "lastname"));

		// configure graph transformer
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		final BidiNodeConfig cfg = graph.getNodeConfig(TuAddressDTO.class);
		final GraphContext ctx = new GraphContextImpl(graph);

		// NodeMapping expected = ctx.getCurrentNodeConfig().getNodeConfig(TuAddressDTO.class);
		final BidiNodeConfig actual = ctx.getConfiguration().getNodeConfig(0, TuAddressDTO.class);
		assertEquals(cfg, actual);

		// create a new NodeConfig based on a original one
		// final DefaultNodeMapping newCfg = new DefaultNodeMapping(graph, cfg);
		// final DefaultPropertyMapping nodeProp = new DefaultPropertyMapping(newCfg, "firstname", "lastname", null,
		// new ToUppercaseConverter());

		final DefaultBidiNodeConfig newCfg = new DefaultBidiNodeConfig(graph, cfg.getType());
		final DefaultBidiPropertyConfig nodeCfg = new DefaultBidiPropertyConfig(newCfg, "firstname", "lastname");
		nodeCfg.setReadInterceptor(new ToUppercaseConverter());

		// property compilation will be done lazy
		newCfg.addPropertyConfig(nodeCfg);

		ctx.getConfiguration().addNodeConfig(1, newCfg);

		// transform
		TuUserModel model = graph.transform(ctx, user);

		// assert property 'lastName'
		// 'lastName' was retrieved from source property 'firstName' which additionally has a toUppercase read-interceptor assigned
		assertEquals("FIRSTNAME", model.getMainAddress().getLastname());


		// transform
		model = graph.transform(user);

		// assert result
		assertEquals("firstname", model.getMainAddress().getFirstname());
		assertEquals("lastname", model.getMainAddress().getLastname());
	}

	@Test
	public void testPropertyBasedNodeConfiguration()
	{
		// setup a graph (user, address)
		final TuUserDTO user = new TuUserDTO();
		user.setMainAddress(new TuAddressDTO("firstname", "lastname"));
		user.setSecondAddress(new TuAddressDTO("firstname2", "lastname2"));

		// create graph transformer
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		// create a new NodeConfig instance for type 'AddressDTO'...
		final DefaultBidiNodeConfig newCfg = new DefaultBidiNodeConfig(graph, TuAddressDTO.class);
		// ... without property 'lastname'
		newCfg.removePropertyConfigByName("lastname");

		// and add this new NodeConfig to property 'secondAddress'
		// by that this NodeConfig becomes active as child of this property and not globally for each AddressDTO type
		final DefaultBidiPropertyConfig p = (DefaultBidiPropertyConfig) graph.getNodeConfig(TuUserDTO.class)
				.getPropertyConfigByName("secondAddress");
		p.setNewNodeMappings((List) Arrays.asList(newCfg));

		// transform
		final TuUserModel result = graph.transform(user);

		// property 'firstname' and 'lastname' are processed for parent-property 'mainAddress'
		assertEquals("firstname", result.getMainAddress().getFirstname());
		assertEquals("lastname", result.getMainAddress().getLastname());

		// only property 'firstname' is processed for parent-property 'secondAddress'
		assertEquals("firstname2", result.getSecondAddress().getFirstname());
		assertNull(result.getSecondAddress().getLastname());
	}

	@Test
	public void testDistanceAndPropertyAndCollection()
	{
		// setup a graph (user, address)
		final TuUserDTO user = new TuUserDTO();
		user.setMainAddress(new TuAddressDTO("firstname", "lastname"));

		final List<TuAddressDTO> addresses = new ArrayList<TuAddressDTO>();
		addresses.add(new TuAddressDTO("1firstname", "1lastname"));
		addresses.add(new TuAddressDTO("2firstname", "2lastname"));
		user.setAddresses(addresses);

		// create graph transformer
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		// create a new NodeConfig for type 'AddressDTO' node
		final DefaultBidiNodeConfig newCfg = new DefaultBidiNodeConfig(graph, TuAddressDTO.class);
		// ... clear all auto-detected properties
		Map<String, BidiPropertyConfig> props = newCfg.removeAllProperties();
		// ... and add property 'firstname'
		// that PropertyConfig is reused (same instance) from another AddressDTO node
		newCfg.addPropertyConfig(graph.getNodeConfig(TuAddressDTO.class).getPropertyConfigByName("firstname"));

		final DefaultBidiPropertyConfig p = (DefaultBidiPropertyConfig) graph.getNodeConfig(TuUserDTO.class)
				.getPropertyConfigByName("secondAddress");
		p.setNewNodeMappings((List) Arrays.asList(newCfg));

		final GraphContext ctx = graph.createGraphContext();

		ctx.getConfiguration().addNodeConfig(1, newCfg);

		final TuUserModel model = graph.transform(ctx, user);
		final List<TuAddressModel> _adr = new ArrayList<TuAddressModel>(model.getAddresses());

		assertEquals(model.getMainAddress().getFirstname(), "firstname");
		assertNull(model.getMainAddress().getLastname());
		assertEquals(_adr.get(0).getFirstname(), "1firstname");
		assertNull(_adr.get(0).getLastname());
		assertNull(_adr.get(1).getLastname());
	}

	@Test
	public void testDistanceAndPropertyAndCollection2()
	{
		// setup a graph (user, address)
		final TuAddressDTO adr = new TuAddressDTO("firstname", "lastname");

		final List<TuAddressDTO> addresses = new ArrayList<TuAddressDTO>();
		addresses.add(new TuAddressDTO("1firstname", "1lastname"));
		addresses.add(new TuAddressDTO("2firstname", "2lastname"));
		adr.setMoreAddresses(addresses);

		// create graph transformer
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		// create a new NodeConfig for type 'AddressDTO' node
		final DefaultBidiNodeConfig newCfg = new DefaultBidiNodeConfig(graph, TuAddressDTO.class);
		// ... clear all auto-detected properties
		newCfg.removeAllProperties();
		// ... and add property 'firstname'
		// that PropertyConfig is newly created (could also be reused from another AddressDTO node)
		newCfg.addPropertyConfig(new DefaultBidiPropertyConfig(newCfg, "firstname"));

		final GraphContext ctx = graph.createGraphContext();

		ctx.getConfiguration().addNodeConfig(1, newCfg);

		final TuAddressModel model = graph.transform(ctx, adr);
		final List<TuAddressModel> _adr = new ArrayList<TuAddressModel>(model.getMoreAddresses());

		assertEquals(model.getFirstname(), "firstname");
		assertEquals(model.getLastname(), "lastname");
		assertEquals(_adr.get(0).getFirstname(), "1firstname");
		assertNull(_adr.get(0).getLastname());
		assertNull(_adr.get(1).getLastname());
	}

	@Test
	public void testDistanceAndPropertyAndCollection3()
	{
		final List<TuAddressDTO> addresses = new ArrayList<TuAddressDTO>();
		addresses.add(new TuAddressDTO("first1", "last1"));
		addresses.add(new TuAddressDTO("first2", "last2"));
		addresses.add(new TuAddressDTO("first3", "last3"));


		// create graph transformer
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		final DefaultBidiNodeConfig newCfg = new DefaultBidiNodeConfig(graph, TuAddressDTO.class);
		newCfg.removeAllProperties();
		newCfg.addPropertyConfig(new DefaultBidiPropertyConfig(newCfg, "firstname"));

		final GraphContext ctx = graph.createGraphContext();

		ctx.getConfiguration().addNodeConfig(0, newCfg);

		final List<TuAddressModel> list = graph.transform(ctx, addresses);

		assertEquals(list.get(0).getFirstname(), "first1");
		assertNull(list.get(0).getLastname());
		assertEquals(list.get(1).getFirstname(), "first2");
		assertNull(list.get(1).getLastname());
		assertEquals(list.get(2).getFirstname(), "first3");
		assertNull(list.get(2).getLastname());
	}


	@Test
	public void testDistanceAndPropertyAndCollection4()
	{
		// user and addresses (collection)
		final TuUserDTO user = new TuUserDTO("user");
		user.setAddresses(new ArrayList<TuAddressDTO>());
		user.getAddresses().add(new TuAddressDTO("first1", "last1"));

		// create graph and retrieve context
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);
		final GraphContextImpl ctx = (GraphContextImpl) graph.createGraphContext();

		// // modify graph context
		// final DefaultNodeMapping nodeCfg = new DefaultNodeMapping(graph, TuAddressDTO.class, TuAddressModel.class);
		// nodeCfg.putPropertyMapping(new DefaultPropertyMapping(nodeCfg, "firstname"));
		// ctx.getConfiguration().addNodeMapping(0, nodeCfg);

		// modify graph context
		final DefaultBidiNodeConfig nodeCfg = new DefaultBidiNodeConfig(graph, TuAddressDTO.class, TuAddressModel.class);
		nodeCfg.addPropertyConfig(new DefaultBidiPropertyConfig(nodeCfg, "firstname"));
		ctx.getConfiguration().addNodeConfig(0, nodeCfg);

		// transform
		final TuUserModel model = graph.transform(ctx, user);

		// assert
		final List<TuAddressModel> list = (List) model.getAddresses();

		assertEquals("first1", list.get(0).getFirstname());
		assertEquals("last1", list.get(0).getLastname());
		// assertNull(list.get(0).getLastname());

	}





	public static void main(final String argc[])
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.DEBUG);

		final CustomNodeConfigsTest test = new CustomNodeConfigsTest();
		test.testDistanceAndPropertyAndCollection4();
	}
}
