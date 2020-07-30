package com.yanan.test.junit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import com.yanan.frame.plugin.ProxyModel;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.annotations.Support;
import com.yanan.frame.plugin.handler.InvokeHandler;
import com.yanan.frame.plugin.handler.MethodHandler;

/**
 * 测试参数记录Handler
 * <p>用于记录测试的时候每个case的调用参数等信息
 * @author Administrator
 *
 */
@Support(value={Test.class},name= {"org.junit.jupiter.params.ParameterizedTest"})
@Register(model = ProxyModel.CGLIB,priority = Integer.MAX_VALUE)
public class TestMethodHandler implements InvokeHandler{
	public static final String METHOD_HANDLER_TOKEN = TestMethodHandler.class.getName();
	@Override
	public void before(MethodHandler methodHandler) {
		//获取当前调用的context
		ExtensionContext context = PluginTestContext.getCurrentTestContext();
		//获取Plugin的测试context
		PluginTestContext testContext = PluginTestContext.getTestContext(context);
		//记录变量
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
