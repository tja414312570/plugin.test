package test.plugin.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
	@ParameterizedTest
	@ValueSource(ints = {2, 4, 8})
	public void test2(int num) {
		System.out.println(num);
		assertEquals("A", "A");
	}
}
