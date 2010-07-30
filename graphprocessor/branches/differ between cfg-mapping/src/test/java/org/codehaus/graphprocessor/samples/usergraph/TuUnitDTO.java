package org.codehaus.graphprocessor.samples.usergraph;

import org.codehaus.graphprocessor.GraphNode;


@GraphNode(target = TuUnitModel.class)
public class TuUnitDTO
{
	private String isocode = null;

	/**
	 * @return the isocode
	 */
	public String getIsocode()
	{
		return isocode;
	}

	/**
	 * @param isocode
	 *           the isocode to set
	 */
	public void setIsocode(final String isocode)
	{
		this.isocode = isocode;
	}

}
