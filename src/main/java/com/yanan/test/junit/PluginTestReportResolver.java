package com.yanan.test.junit;

import java.util.List;

import org.junit.jupiter.api.extension.ExtensionContext;

public interface PluginTestReportResolver {
	void render(PluginTestContext context,List<ExtensionContext> contextList);
}
