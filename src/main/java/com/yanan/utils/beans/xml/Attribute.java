package com.yanan.utils.beans.xml;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 获取节点的属性
 * @author Administrator
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Attribute {
	/**
	 * 默认值
	 * @return 默认值
	 */
	String value() default "";
	/**
	 * 映射名
	 * @return 映射名
	 */
	String name() default "";
	
}
