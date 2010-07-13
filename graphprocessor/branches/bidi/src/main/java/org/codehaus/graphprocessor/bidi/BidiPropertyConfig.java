package org.codehaus.graphprocessor.bidi;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.List;

import org.codehaus.graphprocessor.PropertyContext;
import org.codehaus.graphprocessor.PropertyFilter;
import org.codehaus.graphprocessor.PropertyInterceptor;
import org.codehaus.graphprocessor.PropertyProcessor;
import org.codehaus.graphprocessor.transform.GraphTransformer;


/**
 * Configuration information for a GraphNode property.
 * <p/>
 * Note: This is similar to a {@link PropertyDescriptor} but whereas a {@link PropertyDescriptor} follows strongly java bean
 * standard a {@link BidiPropertyConfig} this implementation allows various "incompatible" settings and additionally provides
 * {@link GraphTransformer} specific information like available {@link PropertyInterceptor}.
 */
public interface BidiPropertyConfig
{


	BidiPropertyConfig getTargetProperty();

	BidiNodeConfig getNodeConfig();

	boolean isNode();

	String toExtString();


	List<BidiNodeConfig> getNewNodeConfigs();

	List<PropertyFilter> getPropertyFilters();

	PropertyProcessor getProcessor();

	/**
	 * Returns an ID which is unique for the Node which this property belongs too.
	 * <p/>
	 * In general this is the property name but this is not guaranteed.
	 * 
	 * @return property id
	 */
	String getId();

	/**
	 * Returns the name of this property.
	 * 
	 * @return property name
	 */
	String getName();

	/**
	 * Returns the read method for that property.
	 * 
	 * @return read method
	 */
	Method getReadMethod();

	/**
	 * Returns the write method for that property.
	 * 
	 * @return write method
	 */
	Method getWriteMethod();

	/**
	 * Returns a {@link PropertyInterceptor} which gets applied for the read value of this property.
	 * 
	 * @return {@link PropertyInterceptor}
	 */
	PropertyInterceptor<Object, Object> getReadInterceptor();

	/**
	 * Returns a {@link PropertyInterceptor} which gets applied for the write value of this property.
	 * 
	 * @return {@link PropertyInterceptor}
	 */
	PropertyInterceptor<Object, Object> getWriteInterceptor();

	/**
	 * Returns the read-value type.
	 * <p/>
	 * Type is either return type of read-method or if an interceptor is assigned the return type of
	 * {@link PropertyInterceptor#intercept(PropertyContext, Object)}
	 * 
	 * @return read-value type
	 */
	Class<?> getReadType();

	/**
	 * Returns the write-value type
	 * <p/>
	 * Type is either return type of write-method or if an interceptor is assigned the return type of
	 * {@link PropertyInterceptor#intercept(PropertyContext, Object)}
	 * 
	 * @return write-value type
	 */
	Class<?> getWriteType();


	/**
	 * True (default) when enabling type check which assures during build-time of graph, that this read-type can be assigned to a
	 * target property write type (not this property write type)
	 * <p/>
	 * Example: read-type: NUMBER; target write-type INTEGER:<br/>
	 * With enabled type check (default) this property will skipped and not processed during graph processing.
	 * 
	 * @return true (default) for enabled type check
	 */
	boolean isReadTypeCheckEnabled();

	/**
	 * True (default) when checking whether write-type is assignable from a source read-type.
	 * 
	 * @return true (default) for enabled type check
	 */
	boolean isWriteTypeCheckEnabled();

	boolean isVirtualRead();

	boolean isVirtualWrite();

	// boolean isNode();


}
