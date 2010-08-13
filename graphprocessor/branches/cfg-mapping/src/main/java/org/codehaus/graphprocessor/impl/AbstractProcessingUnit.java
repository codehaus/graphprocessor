package org.codehaus.graphprocessor.impl;

import java.util.Collection;

import org.codehaus.graphprocessor.Initializable;


public abstract class AbstractProcessingUnit implements Initializable
{
	private boolean isInitialized;

	public abstract Collection getChildProcessingUnits();

	protected boolean initializeChilds(int complianceLevel)
	{
		boolean result = true;
		Collection<?> units = getChildProcessingUnits();
		for (Object unit : units)
		{
			if (unit instanceof Initializable)
			{
				Initializable init = (Initializable) unit;
				if (!init.isInitialized())
				{
					result = result & init.initialize(complianceLevel);
				}
			}
		}
		return result;
	}

	@Override
	public boolean isInitialized()
	{
		return isInitialized;
	}

	public void setInitialized(boolean value)
	{
		isInitialized = value;
	}

	@Override
	public boolean initialize(int complianceLevel)
	{
		setInitialized(true);
		return isInitialized;
	}


}
