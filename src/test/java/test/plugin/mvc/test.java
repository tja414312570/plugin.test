package test.plugin.mvc;

import java.util.Arrays;
import java.util.concurrent.locks.LockSupport;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.servlets.ServletContextInit;
import com.yanan.test.junit.TestMethodHandler;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.resource.ResourceManager;

public class test {
	public static void main(String[] args) throws ClassNotFoundException {
		Class<?> clzz = Class.forName("test.plugin.mvc.NestdTest$FirstNestTest");
		System.out.println(Arrays.toString(clzz.getDeclaredConstructors()));
		String className = clzz.getName();
		className = className.substring(0,className.lastIndexOf('$'));
		Class<?> outerClass = Class.forName(className);
		System.out.println(outerClass);
	}
}
