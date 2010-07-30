package org.codehaus.graphprocessor.samples.misc;

public class TestModel
{
	private String pk;
	private String name;

	public TestModel()
	{

	}


	public String getName()
	{
		return name;
	}


	public void setName(String name)
	{
		this.name = name;
	}


	public TestModel(String pk)
	{
		this.pk = pk;
	}

	public String getPk()
	{
		return pk;
	}


}
