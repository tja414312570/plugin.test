package com.yanan.utils.beans.xml;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * TypeToken 用于获取泛型的具体类型
 * 
 * @author yanan
 *
 * @param <T> the generic type
 */
public abstract class TypeToken<T> {
	private Type type;

	public TypeToken() {
		Type genericSuperclass = getClass().getGenericSuperclass();
		if (genericSuperclass instanceof Class) {
			throw new RuntimeException("Missing type parameter.");
		}
		ParameterizedType parameterizedType = (ParameterizedType) genericSuperclass;
		Type[] typeArguments = parameterizedType.getActualTypeArguments();
		type = typeArguments[0];
		if (type instanceof ParameterizedType) {
			type = ((ParameterizedType) type).getRawType();
		}
	}

	/**
	 * 获取类型
	 * 
	 * @return 泛型第一个类型
	 */
	public Type getType() {
		return type;
	}

	/**
	 * 泛型的类描述
	 * 
	 * @return 泛型一个参数类型描述
	 */
	@SuppressWarnings("unchecked")
	public Class<T> getTypeClass() {
		return (Class<T>) getType();
	}
}
