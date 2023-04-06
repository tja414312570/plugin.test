package test.plugin.mvc;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class MethodHandlesUtil {
	private static final int ALLOWED_MODES = MethodHandles.Lookup.PRIVATE
			|MethodHandles.Lookup.PROTECTED
			|MethodHandles.Lookup.PACKAGE
			|MethodHandles.Lookup.PACKAGE;
	private static Constructor<MethodHandles.Lookup> java8LookupConstructor = null;
	private static Method privateLookupInMethod = null;
	static {
		try {
			privateLookupInMethod = MethodHandles.class.getMethod("", null)
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
