package org.codehaus.graphprocessor.single;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.bidi.BidiGraphConfig;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.BidiPropertyConfig;
import org.codehaus.graphprocessor.bidi.impl.DefaultBidiGraphConfig;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressModel;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryModel;
import org.codehaus.graphprocessor.samples.usergraph.TuUserDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserModel;
import org.junit.Test;



public class ConfigObjectsTest
{
	private static final Logger log = Logger.getLogger(ConfigObjectsTest.class);

	@Test
	public void testBidiGraphConfig()
	{
		final BidiGraphConfig srcGraph = new DefaultBidiGraphConfig(TuUserDTO.class);
		final BidiGraphConfig dstGraph = srcGraph.getTargetConfig();

		assertDtoGraphConfig(srcGraph);
		assertModelGraphConfig(dstGraph);

		// assert circular reference is created correctly
		assertSame(srcGraph, dstGraph.getTargetConfig());

		// assert circular TuUser-NodeConfig
		final BidiNodeConfig srcNodeCfg = srcGraph.getNodeConfig(TuUserDTO.class);
		final BidiNodeConfig dstNodeCfg = dstGraph.getNodeConfig(TuUserModel.class);

		assertNotSame(srcNodeCfg, srcNodeCfg.getTargetNodeConfig());
		assertSame(srcNodeCfg, srcNodeCfg.getTargetNodeConfig().getTargetNodeConfig());

		// assert circular address-PropertyConfig
		final BidiPropertyConfig srcPropCfg = srcNodeCfg.getPropertyConfigByName("mainAddress");
		final BidiPropertyConfig dstPropCfg = dstNodeCfg.getPropertyConfigByName("mainAddress");

		assertNotSame(srcPropCfg, srcPropCfg.getTargetProperty());
		assertSame(srcPropCfg, srcPropCfg.getTargetProperty().getTargetProperty());

		assertNull(srcNodeCfg.getPropertyConfigByName("owner"));
		assertNotNull(dstNodeCfg.getPropertyConfigByName("owner"));
		assertNull((dstNodeCfg.getPropertyConfigByName("owner")).getTargetProperty());

	}

	@Test
	public void testBidiInitialization()
	{
		// final List<Map<String, Method[]>> props = new ArrayList(AbstractPropertyConfig.getPropertiesFor(TuAddressModel.class)
		// .entrySet());
		// log.debug(props);

		final BidiGraphConfig srcGraph = new DefaultBidiGraphConfig(TuUserDTO.class);

		this.assertDtoGraphConfig(srcGraph);

		final BidiGraphConfig dstGraph = srcGraph.getTargetConfig();

		// assert circular graphconfig
		assertNotSame(srcGraph, dstGraph);
		assertSame(srcGraph, dstGraph.getTargetConfig());

		// assert circular nodeconfig
		final BidiNodeConfig srcNodeCfg = srcGraph.getNodeConfig(TuUserDTO.class);
		final BidiNodeConfig dstNodeCfg = dstGraph.getNodeConfig(TuUserModel.class);

		// we must have source and target NodeConfig
		assertNotNull(srcNodeCfg);
		assertNotNull(dstNodeCfg);

		assertSame(srcGraph, srcNodeCfg.getGraphConfig());
		assertSame(dstGraph, dstNodeCfg.getGraphConfig());


		BidiPropertyConfig srcPropCfg = srcNodeCfg.getPropertyConfigByName("mainAddress");
		BidiPropertyConfig dstPropCfg = dstNodeCfg.getPropertyConfigByName("mainAddress");

		// we must have source and target NodeConfig
		assertNotNull(srcPropCfg);
		assertNotNull(dstPropCfg);

		assertSame(srcNodeCfg, srcPropCfg.getNodeConfig());
		assertSame(srcGraph, srcPropCfg.getNodeConfig().getGraphConfig());

		assertSame(dstNodeCfg, dstPropCfg.getNodeConfig());
		assertSame(dstGraph, dstPropCfg.getNodeConfig().getGraphConfig());

		// special property 'owner' which is not available at TuUserDTO but at TuUserModel
		srcPropCfg = srcNodeCfg.getPropertyConfigByName("owner");
		dstPropCfg = dstNodeCfg.getPropertyConfigByName("owner");

		log.debug(dstPropCfg);
	}

	private void assertDtoGraphConfig(final BidiGraphConfig graphCfg)
	{
		// assert GraphConfig properties
		List expected = Arrays.asList(TuUserDTO.class, TuCountryDTO.class, TuAddressDTO.class);
		assertEquals(expected.size(), graphCfg.getNodes().size());
		assertTrue(graphCfg.getNodes().keySet().containsAll(expected));

		// get NodeConfig for TuUserDTO
		final BidiNodeConfig nodeCfg = graphCfg.getNodeConfig(TuUserDTO.class);

		// assert TuUserDTO NodeConfig properties
		assertSame(graphCfg, nodeCfg.getGraphConfig());
		assertEquals(TuUserDTO.class, nodeCfg.getType());

		final Map<String, BidiPropertyConfig> propertiesCfg = nodeCfg.getProperties();
		expected = Arrays.asList("addresses-addresses", "class-class", "login-login", "mainAddress-mainAddress",
				"password-password", "secondAddress-secondAddress", "uid-uid");
		assertEquals(7, propertiesCfg.keySet().size());
		assertTrue(propertiesCfg.keySet().containsAll(expected));
	}

	private void assertModelGraphConfig(final BidiGraphConfig graphCfg)
	{
		// assert GraphConfig properties
		List expected = Arrays.asList(TuUserModel.class, TuCountryModel.class, TuAddressModel.class);
		assertEquals(expected.size(), graphCfg.getNodes().size());
		assertTrue(graphCfg.getNodes().keySet().containsAll(expected));

		// get NodeConfig for TuUserDTO
		final BidiNodeConfig nodeCfg = graphCfg.getNodeConfig(TuUserModel.class);

		// assert TuUserDTO NodeConfig properties
		assertSame(graphCfg, nodeCfg.getGraphConfig());
		assertEquals(TuUserModel.class, nodeCfg.getType());

		final Map<String, BidiPropertyConfig> propertiesCfg = nodeCfg.getProperties();
		expected = Arrays.asList("addresses-addresses", "class-class", "login-login", "mainAddress-mainAddress",
				"password-password", "secondAddress-secondAddress", "uid-uid", "owner-owner");

		assertEquals(expected.size(), propertiesCfg.keySet().size());
		assertTrue(propertiesCfg.keySet().containsAll(expected));
	}

	public static void main(final String[] argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));

		Logger.getRootLogger().setLevel(Level.DEBUG);
		final ConfigObjectsTest test = new ConfigObjectsTest();
		test.testBidiInitialization();
	}


}
