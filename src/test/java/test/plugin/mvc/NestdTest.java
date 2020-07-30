package test.plugin.mvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.definition.RegisterDefinition;
import com.yanan.test.junit.Export;
import com.yanan.test.junit.ReportResolver;
import com.yanan.test.junit.StandTextReportResolver;
import com.yanan.test.junit.extension.PluginExetension;

@Export(value="project:/src/test/report/{c}_{d}.txt")//导出文件地址
@ExtendWith(PluginExetension.class)//使用Plugin上下文
@ReportResolver(StandTextReportResolver.class)//使用标准文本打印测试报告
@DisplayName("内嵌测试类")
@Register
public class NestdTest {
    @BeforeEach
    void init() {
        System.out.println("测试方法执行前准备");
    }
    @Register
    @Nested
    @DisplayName("第一个内嵌测试类")
   class FirstNestTest {
    	FirstNestTest(){};
    	FirstNestTest(String str){};
        @Test
        void test() {
        	RegisterDefinition reg = PlugsFactory.getPluginsHandler(this).getRegisterDefinition();
        	System.out.println(reg);
            System.out.println("第一个内嵌测试类执行测试");
        }
    }

    @Nested
    @DisplayName("第二个内嵌测试类")
    class SecondNestTest {
        @Test
        void test() {
            System.out.println("第二个内嵌测试类执行测试");
        }
    }
}