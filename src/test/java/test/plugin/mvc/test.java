package test.plugin.mvc;

import java.util.concurrent.locks.LockSupport;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.servlets.ServletContextInit;
import com.yanan.test.junit.TestMethodHandler;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.resource.ResourceManager;

public class test {
	public static void main(String[] args) {
		PlugsFactory.init();
		PlugsFactory.getInstance().addRegisterDefinition(TestMethodHandler.class);
		SimplePluginTest test = PlugsFactory.getPluginsInstance(SimplePluginTest.class);
		test.test(null);
		test.test(null);
		test.test(null);
	}
}
