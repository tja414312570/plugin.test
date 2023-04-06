package test.plugin.mvc;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class Tssss {
	public static interface Tests{
		String test();
		default String test(String name) {
			System.err.println(this);
			return test()+name;
		}
	}

	public static class JdkProxy implements InvocationHandler{

		int ALLOW_MODES = MethodHandles.Lookup.PUBLIC 
				| MethodHandles.Lookup.PRIVATE 
				| MethodHandles.Lookup.PROTECTED 
				| MethodHandles.Lookup.PACKAGE;
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			System.err.println("调用方法:"+method+"，参数:"+Arrays.toString(args));
			if(method.getDeclaringClass().equals(Object.class)){
	            return method.invoke(this,args);
	        }
		       if(method.isDefault()){
		            Class<?> parentClass = method.getDeclaringClass();
		            Constructor<?> constructor = MethodHandles.Lookup.
		                    class.getDeclaredConstructor(Class.class,int.class);
		            constructor.setAccessible(true);
		            MethodHandles.Lookup lookup = (Lookup) constructor.newInstance(parentClass,ALLOW_MODES);
		            MethodHandle methodHandle = lookup.unreflectSpecial(method, parentClass);
		            methodHandle.bindTo(proxy);
		            Object[] objects = new Object[args.length + 1];
		            objects[0] = proxy;
		            for (int i = 0; i < args.length; i++) {
		                objects[i + 1] = args[i];
		            }
		            return methodHandle.invokeWithArguments(objects);
		        }
//			System.err.println(method.isDefault());
//			if(method.isDefault())
//				return method.invoke(proxy, args);
//			throw new RuntimeException();
			return "jdk method "+method.getName();
		}
	}
	public static void main(String[] args) throws IllegalArgumentException {
		System.out.println(System.getProperty("java.specification.version"));
		Tests t = (Tests) Proxy.newProxyInstance(Tssss.class.getClassLoader(), new Class<?>[]{Tests.class}, new JdkProxy());
		System.err.println(t.test("hello "));
	}
}
