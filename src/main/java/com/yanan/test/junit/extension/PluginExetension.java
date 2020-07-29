package com.yanan.test.junit.extension;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.jupiter.api.extension.TestInstancePreDestroyCallback;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.plugin.builder.PluginInstanceFactory;
import com.yanan.test.junit.PluginTestContext;
import com.yanan.utils.reflect.AppClassLoader;

import junit.framework.AssertionFailedError;

/**
 * Plugin 测试环境上下文扩展
 * 
 * @author yanan
 *
 */
public class PluginExetension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor,
BeforeEachCallback, AfterEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback,
ParameterResolver,TestInstanceFactory,TestInstancePreDestroyCallback{
	private Logger logger =LoggerFactory.getLogger(PluginExetension.class);
	public static final Namespace NAMESPACE = Namespace.create(PluginExetension.class);
	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		PluginTestContext testContext = PluginTestContext.getTestContext(context);
		testContext.beginTest();
	}
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return true;
	}
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Class<?> parameterType = parameterContext.getParameter().getType();
		return AppClassLoader.castType(null, parameterType);
	}
	@Override
	public void afterTestExecution(ExtensionContext context) throws Exception {
		PluginTestContext testContext = PluginTestContext.getTestContext(context);
		testContext.addTestCase(context);
		logger.debug(String.format("prepared test instance for class : %s", context.getRequiredTestClass()));
	}
	@Override
	public void beforeTestExecution(ExtensionContext context) throws Exception {
	}
	@Override
	public void afterEach(ExtensionContext context) throws Exception {
	
	}
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
	}
	@Override
	public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
	}
	@Override
	public void afterAll(ExtensionContext context) throws Exception {
		PluginTestContext testContext = PluginTestContext.getTestContext(context);
		testContext.testCompleted(context);
		logger.debug(String.format("all test completed at [%s ms]", testContext.getTestTimes()));
		PlugsFactory.getInstance().destory();
	}
	@Override
	public Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
			throws TestInstantiationException {
		Class<?> testClass = factoryContext.getTestClass();
		logger.debug("prepared test instance for class :"+testClass);
		long start = System.currentTimeMillis();
		PluginTestContext testContext = PluginTestContext.getTestContext(extensionContext);
		Object instance = testContext.getTestInstance(testClass);
		long times = System.currentTimeMillis()-start;
		logger.debug("created instance for class :"+testClass+" at ["+times+" ms]");
		return instance;
	}
	@Override
	public void preDestroyTestInstance(ExtensionContext context) throws Exception {
		Object instance = context.getTestInstance().get();
		logger.debug("test instance destory :"+instance);
		PluginInstanceFactory.destoryInstance(instance);
	}


}
