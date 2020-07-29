# plugin.test
基于junit5的测试组件
## 最简单的测试
```java
package test.plugin.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.annotations.Service;
import com.yanan.frame.plugin.builder.resolver.ParameterResolver;
import com.yanan.test.junit.Export;
import com.yanan.test.junit.ReportResolver;
import com.yanan.test.junit.StandTextReportResolver;
import com.yanan.test.junit.extension.PluginExetension;

@Export(value="project:/target/test_export.txt")//导出文件地址
@ExtendWith(PluginExetension.class)//使用Plugin上下文
@ReportResolver(StandTextReportResolver.class)//使用标准文本打印测试报告
@Register//标记为Register才能使用@Service注入服务
public class SimplePluginTest {
	@Service(attribute="array")
	private ParameterResolver<?> resolver;
	@Test
	public void test(@Service(attribute="array") ParameterResolver<?> resolver) {
		if(true) {
			throw new NullPointerException();
		}
	}
	@Test
	public void test() {
		assertEquals("a", "A");
		
	}
	@Test
	public void test2(int num) {
		assertEquals("A", "A");
	}
}

```
## 生成的报告文件内容
```java
-----------------------------------------------------------
    PLUGIN TEST REPORT SNAPSHOT
-----------------------------------------------------------
test name  : SimplePluginTest
test class : test.plugin.mvc.SimplePluginTest
test date  : 2020-07-29T23:05:37.231
test times : 108 ms
test result: all :3 ,errors : 1 ,failures : 1 ,rate: 0.33 %
---------------------the-report-details--------------------

-----------------------------------------------------------
case name     : JUnit Jupiter.SimplePluginTest.test()
case result   : failed
case times    : 38 ms
case message  : expected: <a> but was: <A>
case parameter: []

-----------------------------------------------------------
case name     : JUnit Jupiter.SimplePluginTest.test(ParameterResolver)
case result   : error
case times    : 4 ms
case message  : null
case parameter: [com.yanan.frame.plugin.builder.resolver.ArrayParameterResolver@a5fe93]
java.lang.NullPointerException
      at test.plugin.mvc.SimplePluginTest.test(SimplePluginTest.java:26)
      at test.plugin.mvc.SimplePluginTest$$EnhancerByCGLIB$$4968d7b2.CGLIB$test$1(<generated>:-1)
      at test.plugin.mvc.SimplePluginTest$$EnhancerByCGLIB$$4968d7b2$$FastClassByCGLIB$$7da765c6.invoke(<generated>:-1)
      at net.sf.cglib.proxy.MethodProxy.invokeSuper(MethodProxy.java:228)
      at com.yanan.frame.plugin.handler.PlugsHandler.intercept(PlugsHandler.java:215)
      at test.plugin.mvc.SimplePluginTest$$EnhancerByCGLIB$$4968d7b2.test(<generated>:-1)
      at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-2)
      at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
      at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
      at java.lang.reflect.Method.invoke(Method.java:498)
      at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:686)
      at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
      at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
      at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:149)
      at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:140)
      at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:84)
      at org.junit.jupiter.engine.execution.ExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(ExecutableInvoker.java:115)
      at org.junit.jupiter.engine.execution.ExecutableInvoker.lambda$invoke$0(ExecutableInvoker.java:105)
      at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
      at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
      at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
      at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
      at org.junit.jupiter.engine.execution.ExecutableInvoker.invoke(ExecutableInvoker.java:104)
      at org.junit.jupiter.engine.execution.ExecutableInvoker.invoke(ExecutableInvoker.java:98)
      at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$6(TestMethodTestDescriptor.java:212)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:208)
      at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:137)
      at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:71)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:135)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$7(NodeTestTask.java:125)
      at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:135)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:123)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:122)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:80)
      at java.util.ArrayList.forEach(ArrayList.java:1259)
      at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:38)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:139)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$7(NodeTestTask.java:125)
      at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:135)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:123)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:122)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:80)
      at java.util.ArrayList.forEach(ArrayList.java:1259)
      at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:38)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$5(NodeTestTask.java:139)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$7(NodeTestTask.java:125)
      at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:135)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:123)
      at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:122)
      at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:80)
      at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:32)
      at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
      at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:51)
      at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:248)
      at org.junit.platform.launcher.core.DefaultLauncher.lambda$execute$5(DefaultLauncher.java:211)
      at org.junit.platform.launcher.core.DefaultLauncher.withInterceptedStreams(DefaultLauncher.java:226)
      at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:199)
      at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:141)
      at org.eclipse.jdt.internal.junit5.runner.JUnit5TestReference.run(JUnit5TestReference.java:98)
      at org.eclipse.jdt.internal.junit.runner.TestExecution.run(TestExecution.java:41)
      at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:542)
      at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.runTests(RemoteTestRunner.java:770)
      at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.run(RemoteTestRunner.java:464)
      at org.eclipse.jdt.internal.junit.runner.RemoteTestRunner.main(RemoteTestRunner.java:210)

-----------------------------------------------------------
case name     : JUnit Jupiter.SimplePluginTest.test2(int)
case result   : sucess
case times    : 1 ms
case parameter: [0]
```
