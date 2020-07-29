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
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.resource.Resource;
import com.yanan.utils.string.StringUtil;

/**
 * 标准文本报告解析
 * @author yanan
 *
 */
@Register
public class StandTextReportResolver implements PluginTestReportResolver{
	private static final String VERSION = "SNAPSHOT";
	private Logger logger =LoggerFactory.getLogger(StandTextReportResolver.class);
	private String exportPath;
	private ReportType[] reportType;
	@Override
	public void render(PluginTestContext context, List<ExtensionContext> contextList) {
		logger.debug("prepared export report");
		ExtensionContext mainContext = context.getExtensionContext();
		Class<?> requiredClass = mainContext.getRequiredTestClass();
		Export export = requiredClass.getAnnotation(Export.class);
		if(export != null) {
			exportPath = export.value();
			reportType = export.types();
		}
		if(reportType == null)
			reportType = new ReportType[] {ReportType.ALL};
		Assert.isTrue(StringUtil.isBlank(exportPath), "the export path is null");
		ResourceLoader resourceLoader = new DefaultResourceLoader();
		String realPath = exportPath.replace("{}", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		Resource resource = resourceLoader.getResource(realPath);
		if(resource == null)
			throw new ResourceLoaderException("the resource is not found "+realPath);
		logger.debug("the export path is "+resource.getPath());
		OutputStream outputStream = null;
		Writer writer = null;
		try {
			outputStream = resource.getOutputStream();
			writer = new OutputStreamWriter(outputStream);
			writerLn(writer,"-----------------------------------------------------------");
			writerLn(writer,"    PLUGIN TEST REPORT "+VERSION);
			writerLn(writer,"-----------------------------------------------------------");
			writerLn(writer,"test name  : "+mainContext.getDisplayName());
			writerLn(writer,"test class : "+mainContext.getRequiredTestClass().getName());
			writerLn(writer,"test date  : "+LocalDateTime.now());
			writerLn(writer,"test times : "+context.getTestTimes()+" ms");
			double rate = (double)context.getSuccessCount()/(double)context.getAllCount();
			writerLn(writer,"test result: "+String.format("all :%s ,errors : %s ,failures : %s ,rate: %.2f %%", 
					context.getAllCount(),context.getErrorCount(),context.getFailedCount(),rate));
			writerLn(writer,"---------------------the-report-details--------------------");
			writerLn(writer,"");
			for(ExtensionContext caseContext : contextList) {
				writerLn(writer,"-----------------------------------------------------------");
				Throwable error = caseContext.getExecutionException().orElse(null);
				String caseName = context.getMethodToken(caseContext);
				long times = context.getTestTime(caseContext);
				MethodHandler methodHandler = context.getTestCaseVariable(caseContext, TestMethodHandler.METHOD_HANDLER_TOKEN);
				if(error != null) {
					if(AppClassLoader.extendsOf(error.getClass(), AssertionFailedError.class)) {
						if(matchType(ReportType.FAILED)) {
							writerLn(writer,"case name     : "+caseName);
							writerLn(writer,"case result   : failed");
							writerLn(writer,"case times    : "+times+" ms");
							writerLn(writer,"case message  : "+error.getMessage());
							writerLn(writer,"case parameter: "+Arrays.toString(methodHandler.getParameters()));
						}
//						writerLn(writer,String.format("name:%s  result:failed  message:%s  times:%s ms  parameters:%s", caseName,error.getMessage(),times,Arrays.toString(methodHandler.getParameters())));
					}else if(matchType(ReportType.ERROR)) {
						writerLn(writer,"case name     : "+caseName);
						writerLn(writer,"case result   : error");
						writerLn(writer,"case times    : "+times+" ms");
						writerLn(writer,"case message  : "+error.getMessage());
						writerLn(writer,"case parameter: "+Arrays.toString(methodHandler.getParameters()));
						writerLn(writer,error.getClass().getName()+(error.getMessage()==null?"":":"+error.getMessage()));
						for(StackTraceElement stack : error.getStackTrace()) {
							writerLn(writer,"      at "+stack.getClassName()+"."+stack.getMethodName()+"("+stack.getFileName()+":"+stack.getLineNumber()+")");
						}
					}
				}else if(matchType(ReportType.SUCCESS)){
					writerLn(writer,"case name     : "+caseName);
					writerLn(writer,"case result   : sucess");
					writerLn(writer,"case times    : "+times+" ms");
					writerLn(writer,"case parameter: "+Arrays.toString(methodHandler.getParameters()));
//					writerLn(writer,String.format("name:%s  result:success  times:%s ms", caseName,times));
				}
				writerLn(writer,"");
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
