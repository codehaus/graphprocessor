package org.codehaus.graphprocessor.single;

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.GraphNode;
import org.codehaus.graphprocessor.bidi.BidiNodeConfig;
import org.codehaus.graphprocessor.bidi.DefaultBidiNodeConfig;
import org.codehaus.graphprocessor.bidi.DefaultBidiPropertyConfig;
import org.codehaus.graphprocessor.samples.usergraph.TuAddressDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuCountryModel;
import org.codehaus.graphprocessor.samples.usergraph.TuMediaDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUnitDTO;
import org.codehaus.graphprocessor.samples.usergraph.TuUserDTO;
import org.codehaus.graphprocessor.transform.BidiGraphTransformer;
import org.junit.Test;



/**
 * Tests graph initialization use-case oriented.
 */
public class GraphInitializerTest2
{
	@GraphNode(target = CollectionTypeDTO.class)
	private static class CollectionTypeDTO
	{
		// collected some basic DTOs which are not having any child nodes
		private List<TuCountryDTO> prop1;
		private List prop2;
		private Collection<TuMediaDTO> prop3;
		private Collection<Object> prop4;
		private Collection<? extends TuUnitDTO> prop5;

		/**
		 * @return the prop1
		 */
		public List<TuCountryDTO> getProp1()
		{
			return prop1;
		}

		/**
		 * @param prop1
		 *           the prop1 to set
		 */
		public void setProp1(final List<TuCountryDTO> prop1)
		{
			this.prop1 = prop1;
		}

		/**
		 * @return the prop2
		 */
		public List getProp2()
		{
			return prop2;
		}

		/**
		 * @param prop2
		 *           the prop2 to set
		 */
		public void setProp2(final List prop2)
		{
			this.prop2 = prop2;
		}

		/**
		 * @return the prop3
		 */
		public Collection<TuMediaDTO> getProp3()
		{
			return prop3;
		}

		/**
		 * @param prop3
		 *           the prop3 to set
		 */
		public void setProp3(final Collection<TuMediaDTO> prop3)
		{
			this.prop3 = prop3;
		}

		/**
		 * @return the prop4
		 */
		public Collection<Object> getProp4()
		{
			return prop4;
		}

		/**
		 * @param prop4
		 *           the prop4 to set
		 */
		public void setProp4(final Collection<Object> prop4)
		{
			this.prop4 = prop4;
		}

		/**
		 * @return the prop5
		 */
		public Collection<? extends TuUnitDTO> getProp5()
		{
			return prop5;
		}

		/**
		 * @param prop5
		 *           the prop5 to set
		 */
		public void setProp5(final Collection<? extends TuUnitDTO> prop5)
		{
			this.prop5 = prop5;
		}
	}

	@Test
	public void testManualConfiguration()
	{
		final BidiGraphTransformer graph = new BidiGraphTransformer();

		final DefaultBidiNodeConfig cfg = new DefaultBidiNodeConfig(graph, TuUserDTO.class, TuUserDTO.class);
		graph.addNode(cfg);

		final BidiNodeConfig cfg2 = graph.getNodeConfig(TuUserDTO.class);

		assertEquals(TuUserDTO.class, cfg2.getType());
		assertEquals(TuUserDTO.class, cfg2.getTargetNodeConfig().getType());
		Assert.assertNull(cfg2.getNodeFactory());
		Assert.assertNotNull(cfg.getProcessor());
	}

	@Test
	public void testManualReconfiguration()
	{
		// create a graph
		final BidiGraphTransformer graph = new BidiGraphTransformer(TuUserDTO.class);

		final DefaultBidiNodeConfig node = (DefaultBidiNodeConfig) graph.getNodeConfig(TuAddressDTO.class);
		DefaultBidiPropertyConfig prop = (DefaultBidiPropertyConfig) node.getPropertyConfigByName("country");

		// node must be initialized
		Assert.assertTrue(node.isInitialized());
		Assert.assertTrue(prop.isInitialized());

		// modify one of this nodes child properties
		prop = new DefaultBidiPropertyConfig(node, "country");
		// node.putPropertyMapping(prop);
		node.addPropertyConfig(prop);

		// that property was not initialized yet...
		Assert.assertFalse(node.isInitialized());
		Assert.assertFalse(prop.isInitialized());
		// ... but types are already known (detected during construction)
		Assert.assertEquals(TuCountryDTO.class, prop.getReadType());
		Assert.assertEquals(TuCountryModel.class, prop.getTargetProperty().getWriteType());

		// just a transformation which should trigger any necessary initializations
		final TuUserDTO user = new TuUserDTO();
		user.setMainAddress(new TuAddressDTO());
		graph.transform(user);

		// Assert that PropertyMapping was lazily initialized
		Assert.assertTrue(node.isInitialized());
		Assert.assertTrue(prop.isInitialized());
		Assert.assertEquals(TuCountryDTO.class, prop.getReadType());
		Assert.assertEquals(TuCountryModel.class, prop.getTargetProperty().getWriteType());

	}

	@Test
	public void testCollectionTypeDetection()
	{
		final BidiGraphTransformer graph = new BidiGraphTransformer(CollectionTypeDTO.class);

		Assert.assertNotNull(graph.getNodeConfig(TuCountryDTO.class));
		Assert.assertNotNull(graph.getNodeConfig(TuMediaDTO.class));
		Assert.assertNotNull(graph.getNodeConfig(TuUnitDTO.class));

	}


	public static void main(final String... argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.DEBUG);

		final GraphInitializerTest2 test = new GraphInitializerTest2();
		test.testManualReconfiguration();
	}



}
