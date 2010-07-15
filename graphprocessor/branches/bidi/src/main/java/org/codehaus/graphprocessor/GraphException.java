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
package org.codehaus.graphprocessor;

/**
 * A GraphProcessor specific {@link RuntimeException} which can be thrown during processing/initializing a graph.
 */
public class GraphException extends RuntimeException
{

	public GraphException()
	{
		super();
	}

	public GraphException(final String message, final Throwable cause)
	{
		super(message, cause);
	}

	public GraphException(final String message)
	{
		super(message);
	}

	public GraphException(final Throwable cause)
	{
		super(cause);
	}

}
