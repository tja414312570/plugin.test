package test.plugin.mvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.yanan.frame.plugin.annotations.Destory;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.annotations.Service;
import com.yanan.frame.plugin.builder.resolver.ParameterResolver;
import com.yanan.framework.resource.DefaultResourceLoader;
import com.yanan.framework.resource.ResourceLoader;
import com.yanan.test.junit.Export;
import com.yanan.test.junit.ReportResolver;
import com.yanan.test.junit.ReportType;
import com.yanan.test.junit.StandTextReportResolver;
import com.yanan.test.junit.extension.PluginExetension;
import com.yanan.utils.resource.ResourceManager;

@Register
@Export(value="project:/target/test.txt",types= {ReportType.ERROR,ReportType.FAILED})
@ExtendWith(PluginExetension.class)
@ReportResolver(StandTextReportResolver.class)
public class PluginTaskTest {
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
	
	@Destory
	public void destory() {
		System.out.println("销毁了:"+this);
	}
}
