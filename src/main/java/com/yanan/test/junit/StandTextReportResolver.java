package com.yanan.test.junit;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yanan.frame.plugin.annotations.Register;
import com.yanan.frame.plugin.handler.MethodHandler;
import com.yanan.framework.resource.DefaultResourceLoader;
import com.yanan.framework.resource.ResourceLoader;
import com.yanan.framework.resource.ResourceLoaderException;
import com.yanan.utils.ArrayUtils;
import com.yanan.utils.asserts.Assert;
import com.yanan.utils.reflect.ReflectUtils;
import com.yanan.utils.resource.Resource;
import com.yanan.utils.string.StringUtil;

/**
 * 标准文本报告解析，用于生成txt格式的报告
 * @author yanan
 *
 */
@Register
public class StandTextReportResolver implements PluginTestReportResolver{
	//版本
	private static final String VERSION = "SNAPSHOT";
	private Logger logger =LoggerFactory.getLogger(StandTextReportResolver.class);
	//导出路径
	private String exportPath;
	//过滤的报告类型
	private ReportType[] reportType;
	@Override
	public void render(PluginTestContext context, List<ExtensionContext> contextList) {
		logger.debug("prepared export report");
		//获取根扩展上下文
		ExtensionContext mainContext = context.getExtensionContext();
		//获取主类
		Class<?> requiredClass = mainContext.getRequiredTestClass();
		//解析导出标签
		Export export = requiredClass.getAnnotation(Export.class);
		if(export != null) {
			exportPath = export.value();
			reportType = export.types();
		}
		if(reportType == null)
			reportType = new ReportType[] {ReportType.ALL};
		Assert.isTrue(StringUtil.isBlank(exportPath), "the export path is null");
		//获取导出路径的资源
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		String realPath = exportPath
				.replace("{d}", new SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(new Date()))
				.replace("{c}", requiredClass.getSimpleName());
		Resource resource = resourceLoader.getResource(realPath);
		if(resource == null)
			throw new ResourceLoaderException("the resource is not found "+realPath);
		logger.debug("the export path is "+resource.getPath());
		OutputStream outputStream = null;
		Writer writer = null;
		try {
			outputStream = resource.getOutputStream();
			writer = new OutputStreamWriter(outputStream);
			//写入统计信息
			writerLn(writer,"-----------------------------------------------------------");
			writerLn(writer,"    PLUGIN TEST REPORT "+VERSION);
			writerLn(writer,"-----------------------------------------------------------");
			writerLn(writer,"test name  : "+mainContext.getDisplayName());
			writerLn(writer,"test class : "+mainContext.getRequiredTestClass().getName());
			writerLn(writer,"test date  : "+LocalDateTime.now());
			writerLn(writer,"test times : "+context.getTestTimes()+" ms");
			double rate = (double)context.getSuccessCount()/(double)context.getAllCount()*100;
			writerLn(writer,"test result: "+String.format("all :%s ,errors : %s ,failures : %s ,rate: %.2f %%", 
					context.getAllCount(),context.getErrorCount(),context.getFailedCount(),rate));
			writerLn(writer,"---------------------the-report-details--------------------");
			writerLn(writer,"");
			//循环打印每个测试case
			for(ExtensionContext caseContext : contextList) {
				writeReport(context, writer, caseContext);
			}
		} catch (IOException e) {
			throw new ResourceLoaderException("failed to create report file "+resource.getPath(),e);
		}finally {
			if(writer != null)
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(outputStream != null)
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	/**
	 * 写入报告
	 * @param context 上下文
	 * @param writer 写入器
	 * @param caseContext 要写入的测试上下文
	 * @throws IOException ex
	 */
	public void writeReport(PluginTestContext context, Writer writer, ExtensionContext caseContext) throws IOException {
		writerLn(writer,"-----------------------------------------------------------");
		//错误
		Throwable error = caseContext.getExecutionException().orElse(null);
		//case 名称
		String caseName = context.getMethodToken(caseContext);
		//当前case执行时间
		long times = context.getTestTime(caseContext);
		//获取此case的调用拦截的数据
		MethodHandler methodHandler = context.getTestCaseVariable(caseContext, TestMethodHandler.METHOD_HANDLER_TOKEN);
		if(error != null) {
			//如果是断言错误
			if(ReflectUtils.extendsOf(error.getClass(), AssertionFailedError.class)) {
				if(matchType(ReportType.FAILED)) {
					wrtieFailedReport(writer, error, caseName, times, methodHandler);
				}
				//其它错误
			}else if(matchType(ReportType.ERROR)) {
				wrtieFailedReport(writer, error, caseName, times, methodHandler);
				writerLn(writer,error.getClass().getName()+(error.getMessage()==null?"":":"+error.getMessage()));
				for(StackTraceElement stack : error.getStackTrace()) {
					writerLn(writer,"      at "+stack.getClassName()+"."+stack.getMethodName()+"("+stack.getFileName()+":"+stack.getLineNumber()+")");
				}
			}
			//成功
		}else if(matchType(ReportType.SUCCESS)){
			writerLn(writer,"case name     : "+caseName);
			writerLn(writer,"case result   : sucess");
			writerLn(writer,"case times    : "+times+" ms");
			writerLn(writer,"case parameter: "+(methodHandler==null?"UNKNOW":Arrays.toString(methodHandler.getParameters())));
		}
		writerLn(writer,"");
	}
	/**
	 * 写入失败报告
	 * @param writer writer
	 * @param error 异常
	 * @param caseName caseName
	 * @param times 时间
	 * @param methodHandler 方法拦截
	 * @throws IOException 异常
	 */
	public void wrtieFailedReport(Writer writer, Throwable error, String caseName, long times,
			MethodHandler methodHandler) throws IOException {
		writerLn(writer,"case name     : "+caseName);
		writerLn(writer,"case result   : failed");
		writerLn(writer,"case times    : "+times+" ms");
		writerLn(writer,"case message  : "+error.getMessage());
		writerLn(writer,"case parameter: "+(methodHandler==null?"UNKNOW":Arrays.toString(methodHandler.getParameters())));
	}
	/**
	 * 判断打印的报告类型
	 * @param type 当前case类型
	 * @return 是否匹配
	 */
	private boolean matchType(ReportType type) {
		return ArrayUtils.indexOf(this.reportType, type) != -1
				|| ArrayUtils.indexOf(this.reportType, ReportType.ALL) != -1;
	}
	private void writerLn(Writer writer, String content) throws IOException {
		writer.write(content);
		writer.write("\r\n");
		writer.flush();
	}

}
