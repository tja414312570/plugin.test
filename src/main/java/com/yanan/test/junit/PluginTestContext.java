package com.yanan.test.junit;

import java.time.LocalDateTime;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.Environment;
import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.test.junit.extension.PluginExetension;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.resource.ResourceManager;

/**
 * 组件测试上下文
 * @author yanan
 *
 */
public class PluginTestContext{
	private static Logger logger =LoggerFactory.getLogger(PluginTestContext.class);
	private static Environment environment;
	static {
		environment = Environment.getEnviroment();
	}
	private ExtensionContext extensionContext;
	public PluginTestContext(ExtensionContext context) {
		this.setExtensionContext(context);
	}
	/**
	 * 获取测试上下文
	 * @param context 扩展上下文
	 * @return 测试上下文
	 */
	public static PluginTestContext getTestContext(ExtensionContext context) {
		Assert.isNull(context,"the context is null");
		String contextId = context.getUniqueId();
		PluginTestContext pluginTestContext = environment.getVariable(contextId);
		preparedPluginContext(context);
		if(pluginTestContext == null) {
			environment.executorOnce(contextId, ()->{
				environment.setVariable(contextId, new PluginTestContext(context));
			});
			pluginTestContext = environment.getVariable(contextId);
		}
		return pluginTestContext;
	}
	/**
	 * 准备Plugin上下文环境
	 * @param context 扩展上下文
	 */
	private static void preparedPluginContext(ExtensionContext context) {
		String rootId = context.getRoot().getUniqueId();
		environment.executorOnce(rootId, ()->{
			logger.info("plugin test frame snapshot version");
			Class<?> testClass = context.getRequiredTestClass();
			logger.info("current test main class:"+context.getRequiredTestClass().getName());
			//设置测试上下文类路径为主类路径
			String classPath = testClass.getResource(".").getPath();
			String packagePath =testClass.getPackage().getName().replace(".", "/");
			classPath = classPath.substring(0,classPath.indexOf(packagePath));
			logger.info("test environment classpath :"+classPath);
			ResourceManager.setClassPath(classPath, 0);
			logger.info("start plugin at ["+LocalDateTime.now()+"]");
			long start = System.currentTimeMillis();
			//初始化Plugin
			prparedBootEnvrionment(context);
			long times = System.currentTimeMillis()-start;
			logger.info("plugin started at ["+times+" ms]");
		});
	}
	private static void prparedBootEnvrionment(ExtensionContext context) {
		PlugsFactory.init();
	}
	public ExtensionContext getExtensionContext() {
		return extensionContext;
	}
	public void setExtensionContext(ExtensionContext extensionContext) {
		this.extensionContext = extensionContext;
	}
	public Object getTestInstance(Class<?> testClass) {
		if(PlugsFactory.getInstance().getRegisterDefinition(testClass)==null)
			PlugsFactory.getInstance().addDefinition(testClass);
		return PlugsFactory.getPluginsInstance(testClass);
	}
	public void testCompleted(ExtensionContext context) {
		
	}
	public void addTestCase(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		store.put(context.getUniqueId(), context);
	}
}
