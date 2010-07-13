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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.codehaus.graphprocessor.bidi.impl.NodeContextTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



@RunWith(Suite.class)
@SuiteClasses(
{ ClassLookupMapTest.class, //
		ConfigObjectsTest.class, //
		GraphGeneralTest.class, //
		GraphInitializerTest.class, //
		GraphInitializerTest2.class, //
		NodeContextTest.class, //
		PropertyConverterTest.class, //
		ConvertNodeToPropertyTest.class, //
		VirtualPropertyTest.class, //
		IdentAndEqualNodesTest.class, //
		GraphNodeFactoryTest.class, //
		GraphPropertyFilterTest.class, //
		CustomNodeConfigsTest.class, //
		// BidiGraphTest.class //

})
public class GraphProcessorTestSuite
{

	public static void main(final String[] argc)
	{
		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout("%-5p [%c{1}] %m%n")));
		Logger.getRootLogger().setLevel(Level.DEBUG);

		// new BidiGraphTransformer(AddressDTO.class);
	}

}
