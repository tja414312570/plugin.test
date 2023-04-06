package com.yanan.utils.beans.xml;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


/**
 * 反射工具，提供类的获取等等一系列功能
 * 
 * @author Administrator
 * @version 1.0.1
 * @author YaNan
 *
 */
public class ReflectUtils {
	/**
	 * 判断类是否存在，参数 完整类名
	 * 
	 * @param className 类名
	 * @return boolean:是否存在
	 */
	public static boolean exists(String className) {
		try {
			Class.forName(className);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 获得类下所有公开(public修饰）的属性 传入 完整类名
	 * 
	 * @param className String:class name,the target class name
	 * @return all public fields
	 * @throws ClassNotFoundException 类没有找到异常
	 */
	public static Field[] getFields(String className) throws ClassNotFoundException {
		Class<?> loadClass = Class.forName(className);
		return loadClass.getFields();
	}

	/**
	 * 获得类下所有属性 传入 完整类名
	 * 
	 * @param className 目标类
	 * @return all public fields
	 * @throws ClassNotFoundException 类没有找到异常
	 */
	public static Field[] getDeclaredFields(String className) throws ClassNotFoundException {
		Class<?> loadClass = Class.forName(className);
		return loadClass.getDeclaredFields();
	}

	/**
	 * 获取类的所有属性，包括其父类的所有属性
	 * 
	 * @param className 目标类名
	 * @return 属性集合
	 * @throws ClassNotFoundException ex
	 */
	public static Field[] getAllFields(String className) throws ClassNotFoundException {
		Class<?> loadClass = Class.forName(className);
		return getAllFields(loadClass);
	}

	/**
	 * 获得类下所有声明的属性
	 * 
	 * @param className 目标类
	 * @return 属性集合
	 */
	public static Field[] getAllFields(Class<?> targetClass) {
		List<Field> list = new ArrayList<>();
		while (targetClass != null && !targetClass.equals(Object.class)) {
			Field[] fields = targetClass.getDeclaredFields();
			for (Field field : fields) {
				list.add(field);
			}
			targetClass = targetClass.getSuperclass();
		}
		return list.toArray(new Field[list.size()]);
	}

	/**
	 * 通过类名获取类的所有public的方法
	 * 
	 * @param className 类名
	 * @return 方法的集合
	 * @throws ClassNotFoundException ex
	 */
	public static Method[] getMethods(String className) throws ClassNotFoundException {
		Class<?> loadClass = Class.forName(className);
		return loadClass.getMethods();
	}

	/**
	 * 通过类名获取所有的方法
	 * 
	 * @param className 类名
	 * @return 方法集合
	 * @throws ClassNotFoundException ex
	 */
	public static Method[] getDeclaredMethods(String className) throws ClassNotFoundException {
		Class<?> loadClass = Class.forName(className);
		return loadClass.getDeclaredMethods();
	}

	/**
	 * 通过类名获取所有方法，包括其父类的方法
	 * 
	 * @param className 类名
	 * @return 方法集合
	 * @throws ClassNotFoundException ex
	 * @throws NoSuchMethodException  ex
	 * @throws SecurityException      ex
	 */
	public static Method[] getAllMethods(String className)
			throws ClassNotFoundException, NoSuchMethodException, SecurityException {
		Class<?> loadClass = Class.forName(className);
		return getAllMethods(loadClass);
	}

	/**
	 * 获取类的所有方法，包括父类的方法
	 * 
	 * @param targetClass 目标类
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException     ex
	 */
	public static Method[] getAllMethods(Class<?> targetClass) throws NoSuchMethodException, SecurityException {
		List<Method> list = new ArrayList<>();
		while (targetClass != null && !targetClass.equals(Object.class)) {
			Method[] methods = targetClass.getDeclaredMethods();
			for (Method method : methods) {
				list.add(method);
			}
			targetClass = targetClass.getSuperclass();
		}
		return list.toArray(new Method[list.size()]);
	}

	/**
	 * 判断公开的方法是否存在，参数 完整类名，方法名，参数类型（可选）
	 * 
	 * @param className  类名
	 * @param methodName 方法名
	 * @param argTypes  参数类型
	 * @return boolean: 是否存在
	 * @throws ClassNotFoundException 没有类异常
	 */
	public static boolean hasMethod(String className, String methodName, Class<?>... argTypes)
			throws ClassNotFoundException {
		try {
			Class<?> loadClass = Class.forName(className);
			loadClass.getMethod(methodName, argTypes);
			return true;
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	/**
	 * 判断方法是否存在，参数 完整类名，方法名，参数类型（可选）
	 * 
	 * @param className  类名
	 * @param methodName 方法名
	 * @param argTypes   参数类型
	 * @return boolean: 是否存在
	 * @throws ClassNotFoundException ex
	 */
	public static boolean hasDeclaredMethod(String className, String methodName, Class<?>... argTypes)
			throws ClassNotFoundException {
		try {
			Class<?> loadClass = Class.forName(className);
			loadClass.getDeclaredMethod(methodName, argTypes);
			return true;
		} catch (NoSuchMethodException e) {
		}
		return false;
	}

	/**
	 * 创建属性的is方法
	 * 
	 * @param fieldName 属性名
	 * @return 属性的is方法
	 */
	public static String createFieldIsMethod(String fieldName) {
		return createFieldMethod("is", fieldName);
	}
	/**
	 * 创建属性的is方法
	 * 
	 * @param field 属性
	 * @return 属性的is方法
	 */
	public static String createFieldIsMethod(Field field) {
		return createFieldIsMethod(field.getName());
	}
	/**
	 * 创建属性的suffix的方法，比如is,bool生成 isBool
	 * 
	 * @param suffix 前缀
	 * @param field 属性名
	 * @return fieldName的xx方法
	 */
	public static String createFieldMethod(String suffix, String fieldName) {
		return new StringBuilder(suffix).append(fieldName.substring(0, 1).toUpperCase())
				.append(fieldName.substring(1, fieldName.length())).toString();
	}

	/**
	 * 创建属性get方法
	 * 
	 * @param field 属性
	 * @return 属性的get方法
	 */
	public static String createFieldGetMethod(Field field) {
		return createFieldGetMethod(field.getName());
	}
	/**
	 * 创建属性的get方法
	 * 
	 * @param fieldName 属性名
	 * @return the method name for get filed
	 */
	public static String createFieldGetMethod(String fieldName) {
		return createFieldMethod("get", fieldName);
	}
	/**
	 * 创建属性的set方法
	 * 
	 * @param fieldName 属性名
	 * @return 属性的set方法
	 */
	public static String createFieldSetMethod(String fieldName) {
		return createFieldMethod("set", fieldName);
	}

	/**
	 * 创建属性set方法
	 * 
	 * @param field 属性
	 * @return 属性的set方法
	 */
	public static String createFieldSetMethod(Field field) {
		return createFieldSetMethod(field.getName());
	}

	/**
	 * 创建属性的add方法
	 * 
	 * @param fieldName 属性名
	 * @return add方法
	 */
	public static String createFieldAddMethod(String fieldName) {
		return createFieldMethod("add", fieldName);
	}

	/**
	 * 获取类的方法，传入String类名，String方法名，参数类型数组（可选）
	 * 
	 * @param cls            类
	 * @param methodName     方法名
	 * @param parameterTypes 参数类型
	 * @return target method
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException     ex
	 */
	public static Method getDeclaredMethod(Class<?> cls, String methodName, Class<?>... parameterTypes)
			throws NoSuchMethodException, SecurityException {
		return cls.getDeclaredMethod(methodName, parameterTypes);
	}

	/**
	 * 调用加载器内加载对象的某个静态方法，传入String方法名，参数所需要的参数（可选）
	 * 
	 * @param clzz       类
	 * @param methodName 方法名
	 * @param args       调用参数
	 * @return invoke result
	 * @throws NoSuchMethodException     ex
	 * @throws SecurityException         ex
	 * @throws IllegalAccessException    ex
	 * @throws IllegalArgumentException  ex
	 * @throws InvocationTargetException ex
	 */
	public static Object invokeStaticMethod(Class<?> clzz, String methodName, Object... args)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Class<?>[] argsTypes = ParameterUtils.getParameterBaseType(args);
		return invokeStaticMethod(clzz,methodName,argsTypes,args);
	}

	/**
	 * 调用加载器内加载对象的某个静态方法，传入String方法名，参数所需要的参数（可选）
	 * 
	 * @param clzz           类
	 * @param methodName     方法名
	 * @param parameterTypes 参数类型
	 * @param args           参数
	 * @return invoke result
	 * @throws NoSuchMethodException     ex
	 * @throws SecurityException         ex
	 * @throws IllegalAccessException    ex
	 * @throws IllegalArgumentException  ex
	 * @throws InvocationTargetException ex
	 */
	public static Object invokeStaticMethod(Class<?> clzz, String methodName, Class<?>[] parameterTypes, Object... args)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException {
		Method method = clzz.getMethod(methodName, parameterTypes);
		if (method == null)
			throw new NoSuchMethodException();
		return invokeMethod(null,method,args);
	}
	/**
	 * 调用方法
	 * @param instance 实例
	 * @param method 方法
	 * @param args 参数
	 * @return 调用结果
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 */
	public static Object invokeMethod(Object instance,Method method,Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Object result;
		if(Modifier.isPublic(method.getModifiers())) {
			result = method.invoke(instance, args);
		}else {
			try {
				method.setAccessible(true);
				result = method.invoke(instance, args);
			}finally {
				method.setAccessible(false);
			}
		}
		return result;
	}
	/**
	 * 调用方法
	 * @param instance 实例
	 * @param methodName 方法
	 * @param args 参数
	 * @return 调用结果
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws SecurityException ex
	 * @throws NoSuchMethodException  
	 */
	public static Object invokeMethod(Object instance,String methodName,Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException , SecurityException {
		Class<?>[] types = ParameterUtils.getParameterTypes(args);
		Method method = instance.getClass().getMethod(methodName, types);
		return invokeMethod(instance,method,args);
	}
	/**
	 * 调用方法
	 * @param instance 实例
	 * @param methodName 方法
	 * @param argsType 参数类型
	 * @param args 参数
	 * @return 调用结果
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws SecurityException ex
	 * @throws NoSuchMethodException  
	 */
	public static Object invokeMethod(Object instance,String methodName,Class<?>[] argsType,Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException , SecurityException {
		Method method = instance.getClass().getMethod(methodName, argsType);
		return invokeMethod(instance,method,args);
	}
	/**
	 * 调用方法
	 * @param instance 实例
	 * @param methodName 方法
	 * @param args 参数
	 * @return 调用结果
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws SecurityException ex
	 * @throws NoSuchMethodException  
	 */
	public static Object invokeDeclaredMethod(Object instance,String methodName,Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException , SecurityException {
		Class<?>[] types = ParameterUtils.getParameterTypes(args);
		Method method = instance.getClass().getDeclaredMethod(methodName, types);
		return invokeMethod(instance,method,args);
	}
	/**
	 * 调用方法
	 * @param instance 实例
	 * @param methodName 方法
	 * @param argsType 参数类型
	 * @param args 参数
	 * @return 调用结果
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws SecurityException ex
	 * @throws NoSuchMethodException  
	 */
	public static Object invokeDeclaredMethod(Object instance,String methodName,Class<?>[] argsType,Object... args) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException , SecurityException {
		Method method = instance.getClass().getDeclaredMethod(methodName, argsType);
		return invokeMethod(instance,method,args);
	}
	/**
	 * 判断一个类是否继承自某个接口，不支持包含父类继承的接口的继承 eg. class A implements B{}
	 * implementOf(A.class,B.class) ==》true class A implements B{},class C extends
	 * A{}; implementOf(C.class,B.class) ==》false
	 * 
	 * @param orginClass     要判断的类
	 * @param interfaceClass 要验证实现的接口
	 * @return 是否实现
	 */
	public static boolean implementOf(Class<?> orginClass, Class<?> interfaceClass) {
		Class<?>[] cls = orginClass.getInterfaces();
		for (Class<?> cCls : cls) {
			if (cCls.equals(interfaceClass)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断一个类是否继承自某个接口，支持包含父类继承的接口的继承 class A implements B{}
	 * implementOf(A.class,B.class) ==》true class A implements B{},class C extends
	 * A{}; implementOf(C.class,B.class) ==》true
	 * 
	 * @param childClass     要判断的类
	 * @param interfaceClass 要验证实现的接口
	 * @return whether true
	 */
	public static boolean implementsOf(Class<?> childClass, Class<?> interfaceClass) {
		Class<?> tempClass = childClass;
		while (tempClass != null) {
			if (tempClass.equals(interfaceClass)) {
				return true;
			}
			Class<?>[] interfaces = tempClass.getInterfaces();
			for (Class<?> inter : interfaces) {
				if (interfaceClass.equals(inter)) {
					return true;
				}
			}
			tempClass = tempClass.getSuperclass();
		}
		return false;
	}

	/**
	 * 判断一个类是否是另一个类的子类
	 * 
	 * @param childClass  child class
	 * @param parentClass parent class
	 * @return boolean
	 */
	public static boolean extendOf(Class<?> childClass, Class<?> parentClass) {
		return childClass.getSuperclass().equals(parentClass);
	}

	/**
	 * 判断一个类是否继承了另一个类,采用多级查找机制，两个类可能是父子，孙爷关系
	 * 
	 * @param childClass  child class
	 * @param parentClass parent class
	 * @return boolean
	 */
	public static boolean extendsOf(Class<?> childClass, Class<?> parentClass) {
		Class<?> tempClass = childClass;
		while (tempClass != null) {
			if (tempClass.equals(parentClass)) {
				return true;
			}
			tempClass = tempClass.getSuperclass();
		}
		return false;
	}

	/**
	 * 获取属性的值
	 * 
	 * @param field         属性
	 * @param proxyInstance 实例
	 * @return 值
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException   ex
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getFieldValue(Field field, Object instance)
			throws IllegalArgumentException, IllegalAccessException {
		Object result ;
		if(Modifier.isPublic(field.getModifiers())) {
			result = field.get(instance);
		}else {
			try {
				field.setAccessible(true);
				result = field.get(instance);
			}finally {
				field.setAccessible(false);
			}
		}
		return (T) result;
	}
	/**
	 * 获取属性的值
	 * 
	 * @param fieldName         属性
	 * @param proxyInstance 实例
	 * @return 值
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException   ex
	 * @throws SecurityException  ex
	 * @throws NoSuchFieldException  ex
	 */
	public static <T> T getFieldValue(String fieldName, Object instance)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = instance.getClass().getField(fieldName);
		return getFieldValue(field,instance);
	}
	/**
	 * 获取属性的值
	 * 
	 * @param fieldName         属性
	 * @param proxyInstance 实例
	 * @return 值
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException   ex
	 * @throws SecurityException  ex
	 * @throws NoSuchFieldException  ex
	 */
	public static <T> T getDeclaredFieldValue(String fieldName, Object instance)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		return getFieldValue(field,instance);
	}
	/**
	 * 设置属性值
	 * @param field 属性
	 * @param instance 实例
	 * @param value 值
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException ex
	 */
	public static void setFieldValue(Field field, Object instance,Object value)
			throws IllegalArgumentException, IllegalAccessException {
		if(Modifier.isPublic(field.getModifiers())) {
			field.set(instance, value);
		}else {
			try {
				field.setAccessible(true);
				field.set(instance, value);
			}finally {
				field.setAccessible(false);
			}
		}
		
	}
	/**
	 * 设置属性值
	 * @param fieldName 属性
	 * @param instance 实例
	 * @param value 值
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException ex
	 * @throws SecurityException ex
	 * @throws NoSuchFieldException  ex
	 */
	public static void setFieldValue(String fieldName, Object instance,Object value)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = instance.getClass().getField(fieldName);
		setFieldValue(field, instance, value);
	}
	/**
	 * 设置属性值
	 * @param fieldName 属性
	 * @param instance 实例
	 * @param value 值
	 * @throws IllegalArgumentException ex
	 * @throws IllegalAccessException ex
	 * @throws SecurityException ex
	 * @throws NoSuchFieldException  ex
	 */
	public static void setDeclaredFieldValue(String fieldName, Object instance,Object value)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Field field = instance.getClass().getDeclaredField(fieldName);
		setFieldValue(field, instance, value);
	}
	/**
	 * 类型传入类型是否为非空类型，主要用于某些值在初始化的时候不能为null
	 * 
	 * @param type target type
	 * @return boolean
	 */
	public static boolean isNotNullType(Class<?> type) {
		return type.equals(int.class) || type.equals(long.class) || type.equals(float.class)
				|| type.equals(double.class) || type.equals(short.class) || type.equals(boolean.class) ? true : false;
	}

	/**
	 * 获取field为List的泛型
	 * 
	 * @param field the field
	 * @return the generic type
	 */
	public static Class<?> getListGenericType(Field field) {
		Type genericType = field.getGenericType();
		if (genericType != null && genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			// 得到泛型里的class类型对象
			Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
			return genericClazz;
		}
		return null;
	}
	/**
	 * 获取类的指定泛型接口
	 * 
	 * @param field the field
	 * @return 
	 * @return the generic type
	 */
	public static Type getGenericInterface(Class<?> clzz,Class<?> target) {
		Type[] types = clzz.getGenericInterfaces();
		for(Type type : types) {
			if(type instanceof ParameterizedType && ((ParameterizedType)type).getRawType().equals(target)) {
				return type;
			}
		}
		throw new IllegalStateException();
	}
	/**
	 * 获取Parameter为List的泛型
	 * 
	 * @param 参数
	 * @return generic type of the parameter
	 */
	public static Class<?> getListGenericType(Parameter parm) {
		Type genericType = parm.getParameterizedType();
		if (genericType != null && genericType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericType;
			// 得到泛型里的class类型对象
			Class<?> genericClazz = (Class<?>) pt.getActualTypeArguments()[0];
			return genericClazz;
		}
		return null;
	}

	/**
	 * 获取数组的类型
	 * 
	 * @param 数组类
	 * @return the type of array
	 */
	public static Class<?> getArrayType(Class<?> arrayClass) {
		if (arrayClass.isArray()) {
			return arrayClass.getComponentType();
		}
		return null;
	}

	/**
	 * 获取一个类的外部类，如果这个类是另一个类的内部类时
	 * 
	 * @param targetClass 寻找的目标类
	 * @return 外部类
	 * @throws ClassNotFoundException ex
	 */
	public static Class<?> getOuterClass(Class<?> targetClass) throws ClassNotFoundException {
		String className = targetClass.getName();
		className = className.substring(0, className.lastIndexOf('$'));
		Class<?> outerClass = Class.forName(className);
		return outerClass;
	}

	public static StackTraceElement getStackTraceElement(int i) {
		StackTraceElement[] stacks = new RuntimeException().getStackTrace();
		return stacks[i+2];
	}
	public static Class<?> getStackTraceClass(int i) {
		try {
			return Class.forName(getStackTraceElement(i).getClassName());
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	public static Class<?>[] getActualType(Type type){
		if (type == null ) {
			throw new RuntimeException("Missing type parameter.");
		}
		System.err.println(type);
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			Type[] types = pt.getActualTypeArguments();
			if (types != null && types.length > 0) {
				Class<?>[] classes = new Class<?>[types.length];
				for (int i = 0; i < classes.length; i++) {
					classes[i] = (Class<?>) types[i];
				}
				return classes;
			}
		}
		return null;
	}
	public static Class<?>[] getParameterizedType(Field field) {
		Type type = field.getGenericType(); 
		return getActualType(type);
	}
	public static Class<?>[] getParameterizedType(Parameter parameter) {
		Type type = parameter.getParameterizedType();
		return getActualType(type);
	}
	public static Class<?>[] getParameterizedType(AnnotatedElement parameter) {
		if(parameter instanceof Field)
			return getParameterizedType((Field) parameter);
		if(parameter instanceof Parameter)
			return getParameterizedType((Parameter) parameter);
		throw new IllegalArgumentException();
	}
	
}