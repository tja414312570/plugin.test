package com.yanan.test.junit;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.yanan.frame.plugin.ProxyModel;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.annotations.Support;
import com.yanan.frame.plugin.handler.InvokeHandler;
import com.yanan.frame.plugin.handler.MethodHandler;

/**
 * 测试参数记录
 * @author Administrator
 *
 */
@Support(Test.class)
@Register(model = ProxyModel.CGLIB,priority = Integer.MAX_VALUE)
public class TestMethodHandler implements InvokeHandler{
	public static final String METHOD_HANDLER_TOKEN = TestMethodHandler.class.getName();
	@Override
	public void before(MethodHandler methodHandler) {
		ExtensionContext context = PluginTestContext.getCurrentTestContext();
		PluginTestContext testContext = PluginTestContext.getTestContext(context);
		testContext.addTestCaseVariable(context,METHOD_HANDLER_TOKEN,methodHandler);
	}

	@Override
	public void after(MethodHandler methodHandler) {
	}

	@Override
	public void error(MethodHandler methodHandler, Throwable exception) {
		// TODO Auto-generated method stub
		
	}

}
