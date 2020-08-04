package com.yanan.test.junit;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * 测试报告渲染接口
 * <p>此接口用于扩展测试报告生成
 * @author yanan
 */
public interface PluginTestReportResolver {
	/**
	 * 测试报告渲染方法
	 * @param context 组件上下文
	 * @param contextList 所有测试的上下文
	 */
	void render(PluginTestContext context,List<ExtensionContext> contextList);
}