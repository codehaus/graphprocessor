package org.codehaus.graphprocessor.samples.usergraph;

import org.codehaus.graphprocessor.GraphNode;


@GraphNode(target = TuMediaModel.class)
public class TuMediaDTO
{
	private String code = null;

	/**
	 * @return the code
	 */
	public String getCode()
	{
		return code;
	}

	/**
	 * @param code
	 *           the code to set
	 */
	public void setCode(final String code)
	{
		this.code = code;
	}


}
