package com.yanan.utils.beans.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 参数工具，提供类的获取等等一系列功能
 * @author Administrator
 * @version 1.0.1
 * @author YaNan
 *
 */
public class ParameterUtils{
	/**
	 * 将包装类型转化基础类型
	 * 
	 * @param patchType 包装类型
	 * @return 原始类型
	 */
	public static Class<?> patchBaseType(Object patchType) {
		// 无类型
		if (patchType.getClass().equals(Void.class)) {
			return void.class;
		}
		// 整形
		if (patchType.getClass().equals(Integer.class)) {
			return int.class;
		}
		if (patchType.getClass().equals(Short.class)) {
			return short.class;
		}
		if (patchType.getClass().equals(Long.class)) {
			return long.class;
		}
		// 浮点
		if (patchType.getClass().equals(Double.class)) {
			return double.class;
		}
		if (patchType.getClass().equals(Float.class)) {
			return float.class;
		}
		// 字节
		if (patchType.getClass().equals(Byte.class)) {
			return byte.class;
		}
		if (patchType.getClass().equals(Character.class)) {
			return char.class;
		}
		// 布尔
		if (patchType.getClass().equals(Boolean.class)) {
			return boolean.class;
		}
		return patchType.getClass();
	}
	/**
	 * 将一个类型转化为目标类型
	 * @param orgin orgin
	 * @param targetType target type
	 * @return cast type
	 */
	public static Object castType(Object orgin, Class<?> targetType) {
		if(orgin != null
			&& (ReflectUtils.implementsOf(orgin.getClass(), targetType)
					||ReflectUtils.extendsOf(orgin.getClass(), targetType))) {
			return orgin;
		}
		// 整形
		if (targetType.equals(int.class)) {
			return orgin == null?0:(int)(Integer.parseInt((orgin.toString()).equals("") ? "0" : orgin.toString()));
		}
		if (targetType.equals(short.class)) {
			return orgin == null?0:Short.parseShort((String) orgin);
		}
		if (targetType.equals(long.class)) {
			return orgin == null?0:Long.parseLong(orgin.toString());
		}
		if (targetType.equals(byte.class)) {
			return orgin == null?0:Byte.parseByte(orgin.toString());
		}
		// 浮点
		if (targetType.equals(float.class)) {
			return orgin == null?0:Float.parseFloat(orgin.toString());
		}
		if (targetType.equals(double.class)) {
			return orgin == null?false:Double.parseDouble(orgin.toString());
		}
		// 日期
		if (targetType.equals(Date.class)) {
			try {
				if(orgin == null)
					return null;
				if(ReflectUtils.extendsOf(orgin.getClass(), Date.class)) {
					return orgin;
				}
				return SimpleDateFormat.getInstance().parse(orgin.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		// 布尔型
		if (targetType.equals(boolean.class)) {
			return orgin == null?false:Boolean.parseBoolean((String) orgin);
		}
		// char
		if (targetType.equals(char.class)) {
			return orgin == null?0:(char) orgin;
		}
		if (targetType.equals(String.class)) {
			return orgin == null?null:orgin.toString();
		}
		// 没有匹配到返回源数据
		return orgin;
	}

	/**
	 * 将字符类型转换为目标类型
	 * 
	 * @param clzz array class 
	 * @param arg arg array
	 * @param format format
	 * @return cast result
	 * @throws ParseException ex
	 */
	public static Object parseBaseTypeArray(Class<?> clzz, String[] arg, String format) throws ParseException {
		if (!clzz.isArray()) {
			return parseBaseType(clzz, arg[0], format);
		}
		if (clzz.equals(String[].class)) {
			return arg;
		}
		Object[] args = new Object[arg.length];
		for (int i = 0; i < args.length; i++) {
			args[i] = parseBaseType(clzz, arg[i], format);
		}
		return args;
	}
	/**
	 * 判断类是否为可支持的基本类型
	 * 
	 * @param clzz judge class
	 * @return whether 
	 */
	public static boolean isBaseType(Class<?> clzz) {
		if (clzz.equals(String.class))
			return true;
		if (clzz.equals(boolean.class))
			return true;
		if (clzz.equals(int.class))
			return true;
		if (clzz.equals(float.class))
			return true;
		if (clzz.equals(byte.class))
			return true;
		if (clzz.equals(short.class))
			return true;
		if (clzz.equals(long.class))
			return true;
		if (clzz.equals(double.class))
			return true;
		if (clzz.equals(char.class))
			return true;
		// 八个基本数据类型的包装类型
		if (clzz.equals(Byte.class))
			return true;
		if (clzz.equals(Short.class))
			return true;
		if (clzz.equals(Integer.class))
			return true;
		if (clzz.equals(Long.class))
			return true;
		if (clzz.equals(Float.class))
			return true;
		if (clzz.equals(Double.class))
			return true;
		if (clzz.equals(Boolean.class))
			return true;
		if (clzz.equals(Character.class))
			return true;

		// 以上所有类型的数组类型
		if (clzz.equals(String[].class))
			return true;
		if (clzz.equals(boolean[].class))
			return true;
		if (clzz.equals(int[].class))
			return true;
		if (clzz.equals(float[].class))
			return true;
		if (clzz.equals(byte[].class))
			return true;
		if (clzz.equals(short[].class))
			return true;
		if (clzz.equals(long[].class))
			return true;
		if (clzz.equals(double[].class))
			return true;
		if (clzz.equals(char[].class))
			return true;
		// 八个基本数据类型的包装类型
		if (clzz.equals(Short[].class))
			return true;
		if (clzz.equals(Integer[].class))
			return true;
		if (clzz.equals(Long[].class))
			return true;
		if (clzz.equals(Float[].class))
			return true;
		if (clzz.equals(Double[].class))
			return true;
		if (clzz.equals(Boolean[].class))
			return true;
		if (clzz.equals(Character[].class))
			return true;
		return false;
	}
	/**
	 * 判断类是否为可支持的基本无包裹类型
	 * 
	 * @param clzz judge class
	 * @return whether 
	 */
	public static boolean isBaseUnwrapperType(Class<?> clzz) {
		if (clzz.equals(String.class))
			return true;
		if (clzz.equals(boolean.class))
			return true;
		if (clzz.equals(int.class))
			return true;
		if (clzz.equals(float.class))
			return true;
		if (clzz.equals(byte.class))
			return true;
		if (clzz.equals(short.class))
			return true;
		if (clzz.equals(long.class))
			return true;
		if (clzz.equals(double.class))
			return true;
		if (clzz.equals(char.class))
			return true;
		return false;
	}
	/**
	 * 将字符类型转换为目标类型
	 * 
	 * @param clzz target type
	 * @param arg argument string
	 * @param format the date format
	 * @return parse result
	 */
	public static Object parseBaseType(Class<?> clzz, String arg, String format) {
		// 匹配时应该考虑优先级 比如常用的String int boolean应该放在前面 其实 包装类型应该分开
		if (clzz.equals(String.class))
			return arg;
		// 8个基本数据类型及其包装类型
		if (clzz.equals(int.class))
			return StringUtil.isEmpty(arg) ? 0 : Integer.parseInt(arg);
		if (clzz.equals(Integer.class))
			return StringUtil.isEmpty(arg) ? null : Integer.valueOf(arg);

		if (clzz.equals(boolean.class))
			return StringUtil.isEmpty(arg) ? false : Boolean.parseBoolean(arg);
		if (clzz.equals(Boolean.class))
			return StringUtil.isEmpty(arg) ? null : Boolean.valueOf(arg);

		if (clzz.equals(float.class))
			return StringUtil.isEmpty(arg) ? 0.0f : Float.parseFloat(arg);
		if (clzz.equals(Float.class))
			return StringUtil.isEmpty(arg) ? null : Float.valueOf(arg);

		if (clzz.equals(short.class))
			return StringUtil.isEmpty(arg) ? 0 : Short.parseShort(arg);
		if (clzz.equals(Short.class))
			return StringUtil.isEmpty(arg) ? null : Short.valueOf(arg);

		if (clzz.equals(long.class))
			return StringUtil.isEmpty(arg) ? 0l : Long.parseLong(arg);
		if (clzz.equals(Long.class))
			return StringUtil.isEmpty(arg) ? null : Long.valueOf(arg);

		if (clzz.equals(double.class))
			return StringUtil.isEmpty(arg) ? 0.0f : Double.parseDouble(arg);
		if (clzz.equals(Double.class))
			return StringUtil.isEmpty(arg) ? null : Double.valueOf(arg);

		if (clzz.equals(char.class))
			return StringUtil.isEmpty(arg) ? null : arg.charAt(0);
		if (clzz.equals(Character.class))
			return StringUtil.isEmpty(arg) ? null : Character.valueOf(arg.charAt(0));

		if (clzz.equals(char[].class))
			return StringUtil.isEmpty(arg) ? null : arg.toCharArray();

		if (clzz.equals(byte.class) || clzz.equals(Byte.class))
			return StringUtil.isEmpty(arg) ? null : Byte.parseByte(arg);
		return arg;
	}
	/**
	 * get the base type base on argument's values
	 * @param args argument's
	 * @return base type array
	 */
	public static Class<?>[] getParameterTypes(Object... args) {
		Class<?>[] parmType = new Class[args.length];
		for (int i = 0; i < args.length; i++)
			parmType[i] = args[i]==null?null:args[i].getClass();
		return parmType;
	}
	/**
	 * get the base type base on argument's values
	 * @param args argument's
	 * @return base type array
	 */
	public static Class<?>[] getParameterBaseType(Object... args) {
		Class<?>[] parmType = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++)
			parmType[i] = patchBaseType(args[i]);
		return parmType;
	}
	/**
	 * 传入参数类型和目标参数类型匹配，主要用于判断方法的匹配
	 * 支持子类型判断，比如[string,int]--[object,int]匹配
	 * @param matchType match type
	 * @param parameterTypes parameter type
	 * @return boolean
	 */
	public static  boolean matchType(Class<?>[] matchType, Class<?>[] parameterTypes) {
		if(parameterTypes.length!=matchType.length)
			return false;
		for(int i = 0;i<parameterTypes.length;i++){
			if(parameterTypes[i]==null&&!isNotNullType(matchType[i]))
				continue;
			if(parameterTypes[i].equals(matchType[i]))
				continue;
			if(ReflectUtils.extendsOf(parameterTypes[i], matchType[i]))
				continue;
			if(ReflectUtils.implementsOf(parameterTypes[i], matchType[i]))
				continue;
			return false;
		}
		return true;
	}
	/**
	 * 类型传入类型是否为非空类型，主要用于某些值在初始化的时候不能为null
	 * @param type target type
	 * @return boolean
	 */
	public static boolean isNotNullType(Class<?> type) {
		return type.equals(int.class)||
			   type.equals(long.class)||
			   type.equals(float.class)||
			   type.equals(double.class)||
			   type.equals(short.class)||
			   type.equals(boolean.class)?true:false;
	}
	/**
	 * 获取field为List的泛型
	 * @param field the field
	 * @return the generic type
	 */
	public static Class<?> getListGenericType(Field field) {
		Type genericType = field.getGenericType(); 
		if(genericType != null && genericType instanceof ParameterizedType){   
			ParameterizedType pt = (ParameterizedType) genericType;
			//得到泛型里的class类型对象  
			Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0]; 
			return genericClazz;
		}   
		return null;
	}
	/**
	 * 获取Parameter为List的泛型
	 * @param parm the parameter
	 * @return generic type of the parameter
	 */
	public static Class<?> getListGenericType(Parameter parm) {
		Type genericType = parm.getParameterizedType(); 
		if(genericType != null && genericType instanceof ParameterizedType){   
			ParameterizedType pt = (ParameterizedType) genericType;
			//得到泛型里的class类型对象  
			Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0]; 
			return genericClazz;
		}   
		return null;
	}
}