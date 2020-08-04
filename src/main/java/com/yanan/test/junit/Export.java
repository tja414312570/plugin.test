package com.yanan.test.junit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 报告配置
 * @author yanan
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Export {
	/**
	 * 参数名称
	 * @return 属性名
	 */
	String value();
	/**
	 * 打印报告详情的分类
	 * @return 类型
	 */
	ReportType[] types() default ReportType.ALL;
}