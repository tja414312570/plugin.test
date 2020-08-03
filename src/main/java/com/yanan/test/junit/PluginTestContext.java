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
import com.yanan.utils.reflect.ReflectUtils;
import com.yanan.utils.reflect.TypeToken;
import com.yanan.utils.resource.ResourceManager;


/**
 * 组件测试上下文
 * @author yanan
 *
 */
public class PluginTestContext{
	//测试案例token
	private static final String TEST_CASE_TOKEN = "_plugin_test_case";
	//测试案例开始时间token
	private static final String TIME_START_TOKEN = "_plugin_test_start_time";
	//测试开始时间token
	private static final String TIME_TEST_TOKEN = "_plugin_test_time";
	//每个案例的变量token
	private static final String CONTEXT_VARIABLE_TOKEN = "_plugin_test_variable";;
	private static Logger logger =LoggerFactory.getLogger(PluginTestContext.class);
	//环境
	private static Environment environment;
	static {
		environment = Environment.getEnviroment();
	}
	//当前环境的扩展上下文
	private ExtensionContext extensionContext;
	//失败数量
	private int failedCount;
	//错误数量
	private int errorCount;
	//成功数量
	private int successCount;
	//整个测试耗时
	private long testTimes;
	//测试开始时间
	private long startTimes;
	//总案例数量
	private int allCount;
	//案例集合
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
		String contextId = context.getRoot().getUniqueId().intern();
		environment.executeOnlyOnce(contextId, ()->{
			environment.setVariable(contextId, new PluginTestContext(context));
			preparedPluginContext(context);
		});
		PluginTestContext pluginTestContext = environment.getVariable(contextId);
		return pluginTestContext;
	}
	/**
	 * 准备Plugin上下文环境
	 * @param context 扩展上下文
	 */
	private static void preparedPluginContext(ExtensionContext context) {
		String rootId = context.getRoot().getUniqueId();
		environment.executeOnlyOnce(rootId, ()->{
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
	//准备报告解析器
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
	//准备环境
	private static void prparedBootEnvrionment(ExtensionContext context) {
		try {
			//基本的plugin初始化
			PlugsFactory.init();
			//添加调用记录注册器
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
			PlugsFactory.getInstance().addRegisterDefinition(testClass);
		return PlugsFactory.getPluginsInstance(testClass);
	}
	/**
	 * 测试完成
	 * @param context 扩展上下文
	 */
	public void testCompleted(ExtensionContext context) {
		this.testTimes = System.currentTimeMillis() - startTimes;
		testContextSet =  getCaseSet(context);
		this.allCount = testContextSet.size();
		AtomicInteger errorNum = new AtomicInteger(0);
		AtomicInteger failuresNum = new AtomicInteger(0);
		testContextSet.forEach(caseContext->{
			Throwable error = caseContext.getExecutionException().orElse(null);
			if(error != null) {
				if(ReflectUtils.extendsOf(error.getClass(), AssertionFailedError.class)) {
					failuresNum.addAndGet(1);
				}else {
					errorNum.addAndGet(1);
				}
			}
		});
		this.errorCount = errorNum.get();
		this.failedCount = failuresNum.get();
		this.successCount = testContextSet.size()-this.errorCount-this.failedCount;
		double rate = successCount/(double)testContextSet.size()*100;
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
	/**
	 * 生成测试报告
	 */
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
	/**
	 * 获取方法标示
	 * @param context 扩展上下文
	 * @return 标示
	 */
	public String getMethodToken(ExtensionContext context) {
		StringBuffer stringBuilder = new StringBuffer("");
		ExtensionContext temp = context;
		do {
			stringBuilder.insert(0, context.getDisplayName());
			context = context.getParent().orElse(null);
		}while(context != null 
				&& !context.equals(temp.getRoot())
				&& stringBuilder.insert(0, ".").length()>0);
		return stringBuilder.toString();
	}
	public void addTestCase(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		store.put(context.getUniqueId()+TIME_START_TOKEN, System.currentTimeMillis());
		contextThreadLocal.set(context);
	}
	/**
	 * 单个案例测试完成
	 * @param context 上下文
	 */
	public void completedTestCase(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		long times = System.currentTimeMillis()-store.get(context.getUniqueId()+TIME_START_TOKEN, Long.class);
		store.put(context.getUniqueId()+TIME_TEST_TOKEN,times);
		List<ExtensionContext> caseSet =  getCaseSet(context);
		caseSet.add(context);
		contextThreadLocal.remove();
	}
	/**
	 * 获取当前的案例上下文，线程安全
	 * @return 扩展上下文
	 */
	public static ExtensionContext getCurrentTestContext() {
		return contextThreadLocal.get();
	}
	public long getTestTime(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		return store.get(context.getUniqueId()+TIME_TEST_TOKEN,Long.class);
	}
	/**
	 * 获取所有测试案例的集合
	 * @param context 上下文
	 * @return 案例集合
	 */
	public List<ExtensionContext> getCaseSet(ExtensionContext context) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		String testCaseId = context.getRoot().getUniqueId();
		String caseId = (testCaseId+TEST_CASE_TOKEN).intern();
		environment.executeOnlyOnce(caseId, ()->{
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
	/**
	 * 设置测试案例上下文变量
	 * @param context 案例上下文
	 * @param key key
	 * @param value value
	 */
	public void addTestCaseVariable(ExtensionContext context, String key,Object value) {
		Store store = context.getRoot().getStore(PluginExetension.NAMESPACE);
		String rootId = (context.getUniqueId()+CONTEXT_VARIABLE_TOKEN).intern();
		environment.executeOnlyOnce(rootId, ()->{
			store.put(rootId, new HashMap<>());
		});
		Map<String,Object> variable = store.get(rootId,new TypeToken<Map<String,Object>>(){}.getTypeClass());
		variable.put(key, value);
	}
	/**
	 * 获取测试案例变量
	 * @param context 案例的上下文
	 * @param key key
	 * @return 变量
	 */
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
