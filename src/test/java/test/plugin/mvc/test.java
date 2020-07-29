package test.plugin.mvc;

import com.yanan.frame.plugin.PlugsFactory;
import com.yanan.frame.servlets.ServletContextInit;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.resource.ResourceManager;

public class test {
	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getContextClassLoader().getResource(""));
		ResourceManager.addClassPath("/Volumes/GENERAL/git/plugin.mvc/target/classes/");
		System.out.println(ResourceManager.classPath());
		PlugsFactory.init();
	}
}
