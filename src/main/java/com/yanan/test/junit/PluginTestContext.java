package com.yanan.test.junit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.Environment;
import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.test.junit.extension.PluginExetension;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.reflect.TypeToken;
import com.yanan.utils.resource.ResourceManager;


/**
 * 组件测试上下文
 * @author yanan
 *
 */
public class PluginTestContext{
	private static final String TEST_CASE_TOKEN = "_plugin_test_case";
	private static final String TIME_START_TOKEN = "_plugin_test_start_time";
	private static final String TIME_TEST_TOKEN = "_plugin_test_time";
	private static final String CONTEXT_VARIABLE_TOKEN = "_plugin_test_variable";;
	private static Logger logger =LoggerFactory.getLogger(PluginTestContext.class);
	private static Environment environment;
	static {
		environment = Environment.getEnviroment();
	}
	private ExtensionContext extensionContext;
	private int failedCount;
	private int errorCount;
	private int successCount;
	private long testTimes;
	private long startTimes;
	private int allCount;
	private List<ExtensionContext> testContextSet;
	private static ThreadLocal<ExtensionContext> contextThreadLocal = new InheritableThreadLocal<>();
	public List<ExtensionContext> getTestContextSet() {
		return testContextSet;
	}
	public PluginTestContext(ExtensionContext context) {
		this.setExtensionContext(context);
	}
	public int getFailedCount() {
		return failedCount;
	}
	public int getErrorCount() {
		return errorCount;
	}
	public int getSuccessCount() {
		return successCount;
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
			//准备报告解析器
			preparedReportResolver(context);
			long times = System.currentTimeMillis()-start;
			logger.info("plugin started at ["+times+" ms]");
		});
	}
	private static void preparedReportResolver(ExtensionContext context) {
		try {
			PlugsFactory.getInstance().addPlugininDefinition(PluginTestReportResolver.class);
			ReportResolver reportResolver = context.getRequiredTestClass().getAnnotation(ReportResolver.class);
			if(reportResolver != null) {
				for(Class<?> resolverClass : reportResolver.value()) {
					PlugsFactory.getInstance().addRegisterDefinition(resolverClass);
				}
			}
		}catch(Throwable t) {
			logger.error("a error occur when prepared report resolver",t);
		}
	}
	private static void prparedBootEnvrionment(ExtensionContext context) {
		try {
			PlugsFactory.init();
			PlugsFactory.getInstance().addRegisterDefinition(TestMethodHandler.class);
		}catch(RuntimeException t) {
			logger.error("a error occur when prepared boot environment",t);
			throw t;
		}
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
		this.testTimes = System.currentTimeMillis() - startTimes;
		testContextSet =  getCaseSet(context);
		this.allCount = testContextSet.size();
		AtomicInteger errorNum = new AtomicInteger(0);
		AtomicInteger failuresNum = new AtomicInteger(0);
		testContextSet.forEach(caseContext->{
			Throwable error = caseContext.getExecutionException().orElse(null);
			if(error != null) {
				if(AppClassLoader.extendsOf(error.getClass(), AssertionFailedError.class)) {
					failuresNum.addAndGet(1);
				}else {
					errorNum.addAndGet(1);
				}
			}
		});
		this.errorCount = errorNum.get();
		this.failedCount = failuresNum.get();
		this.successCount = testContextSet.size()-this.errorCount-this.failedCount;
		double rate = successCount/(double)testContextSet.size();
		logger.debug(String.format("all case num :%s ,errors : %s ,failures : %s ,rate: %.2f %%", 
				testContextSet.size(),this.errorCount,this.failedCount,rate));
		logger.debug(String.format("the detail report please use report resolver", 
				testContextSet.size(),errorNum.get(),failuresNum.get(),rate));
		try {
			exportReport();
		}catch(Throwable t) {
			logger.error("a error occur when export report",t);
		}
	}
	private void exportReport() {
		List<PluginTestReportResolver> resolverInstanceList = PlugsFactory.getPluginsInstanceList(PluginTestReportResolver.class);
		for(PluginTestReportResolver resolver : resolverInstanceList) {
			resolver.render(this, this.testContextSet);
		}
	}
	public static String getTestCaseToken() {
		return TEST_CASE_TOKEN;
	}
	public static Environment getEnvironment() {
		return environment;
	}
	public String getMethodToken(ExtensionContext context) {
		StringBuffer stringBuilder = new StringBuffer("");
		while(context != null) {
			stringBuilder.insert(0, context.getDisplayName());
			context = context.getParent().orElse(null);
			if(context != null) {
				stringBuilder.insert(0, ".");
			}
		}
		return stringBuilder.toString();
	}
	public void addTestCase(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		store.put(context.getUniqueId()+TIME_START_TOKEN, System.currentTimeMillis());
		contextThreadLocal.set(context);
	}
	public void completedTestCase(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		long times = System.currentTimeMillis()-store.get(context.getUniqueId()+TIME_START_TOKEN, Long.class);
		store.put(context.getUniqueId()+TIME_TEST_TOKEN,times);
		List<ExtensionContext> caseSet =  getCaseSet(context);
		caseSet.add(context);
		contextThreadLocal.remove();
	}
	public static ExtensionContext getCurrentTestContext() {
		return contextThreadLocal.get();
	}
	public long getTestTime(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		return store.get(context.getUniqueId()+TIME_TEST_TOKEN,Long.class);
	}
	public List<ExtensionContext> getCaseSet(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		String testCaseId = context.getRoot().getUniqueId();
		String caseId = (testCaseId+TEST_CASE_TOKEN).intern();
		environment.executorOnce(caseId, ()->{
			store.put(caseId, new ArrayList<ExtensionContext>());
		});
		List<ExtensionContext> caseMap = store.get(caseId,
				new TypeToken<ArrayList<ExtensionContext>>() {}.getTypeClass());
		return caseMap;
	}
	public long getTestTimes() {
		return testTimes;
	}
	public void beginTest() {
		this.startTimes = System.currentTimeMillis();
	}
	public int getAllCount() {
		return allCount;
	}
	public void addTestCaseVariable(ExtensionContext context, String key,Object value) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		String rootId = (context.getUniqueId()+CONTEXT_VARIABLE_TOKEN).intern();
		environment.executorOnce(rootId, ()->{
			store.put(rootId, new HashMap<>());
		});
		Map<String,Object> variable = store.get(rootId,new TypeToken<Map<String,Object>>(){}.getTypeClass());
		variable.put(key, value);
	}
	@SuppressWarnings("unchecked")
	public <T> T getTestCaseVariable(ExtensionContext context, String key) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		String rootId = (context.getUniqueId()+CONTEXT_VARIABLE_TOKEN).intern();
		Map<String,Object> variable = store.get(rootId,new TypeToken<Map<String,Object>>(){}.getTypeClass());
		if(variable == null)
			return null;
		return (T) variable.get(key);
	}
}
