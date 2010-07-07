package org.codehaus.graphprocessor.bidi;

import org.codehaus.graphprocessor.NodeConfig;
import org.codehaus.graphprocessor.NodeFactory;


public interface BidiNodeConfig extends NodeConfig
{

	BidiNodeConfig getTargetNodeConfig();

	/**
	 * Returns a {@link BidiPropertyConfig} for a mapping whose source and target property name are matching passed name.
	 * @param propertyName
	 *           name of source and target property
	 * @return {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig getPropertyConfigByName(String propertyName);

	/**
	 * Returns a {@link BidiPropertyConfig} for passed property mappings.
	 * <p/>
	 * Property mapping is given as source property name and target property name. If target property name is 'null' it's assumed
	 * that it's name equals source property name.
	 * @param sourcePropName
	 *           name of source property
	 * @param targetPropName
	 *           name of target property
	 * @return {@link BidiPropertyConfig}
	 */
	BidiPropertyConfig getPropertyConfigByName(String sourcePropName, String targetPropName);

	BidiPropertyConfig removePropertyConfigByName(String propertyName);

	BidiPropertyConfig removePropertyConfigByName(String propertyName, String targetPropName);


	@Override
	BidiGraphConfig getGraphConfig();

	/**
	 * @return the nodeFactory
	 */
	NodeFactory getNodeFactory();

	boolean isDebugEnabled();

}
