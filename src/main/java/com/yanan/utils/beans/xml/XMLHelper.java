package com.yanan.utils.beans.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;


/**
 * xml解析工具，反向加载，通过java映射寻址xml，主要通过注解方式驱动，所有对象通过PlugsHandler代理
 * <p>Encode注解，用于提供读取xml的字符集 Attribute 可用于Field，标示该字段是一个节点的属性，用于基础类型 tip:基础类型</p>
 * <p>java八中基础类型与String类型以及对应的数组类型 Element 用于集合或其他pojo类型，如List，Map，或自定义类型，可以用于类声明</p>
 * <p>Value 用于Field，标示该字段是节点的文本值 AsXml 用于Field,标示该字段为该节点作为xml Ignore 不处理该标签 Type</p>
 * <p>用于指定聚合类的实现类 20180919</p>
 * <p>支持Encode注解，Attribute注解，Element注解，Value注解，AsXml注解，Ignore注解,Type注解</p>
 * <p>支持List，简单POJO以及基本数据类型</p>
 * <p>20181010 新增MappingGroup的支持，重构代码结构</p>
 * <p>20181011 FieldTypes{@link com.yanan.utils.beans.xml.FieldType}新增All类型，支持类所有Field的获取</p>
 * 
 * @author yanan
 *
 */
public class XMLHelper {
	private InputStream inputStream;
	private Class<?> mapping;
	// 字符集
	private String charset = "UTF-8";
	// 命名映射
	private Map<String, String> nameMapping = new HashMap<String, String>();
	// 用于存储结果集
	private List<Object> beanObjectList = new ArrayList<Object>();
	// remove mapping
	private List<String> removeNodes = new ArrayList<String>();
	// 节点名
	private String nodeName;
	// 集合映射
	private Map<String, Class<?>> mapMapping = new HashMap<String, Class<?>>();
	/**
	 * @param files xml文件
	 * @param wrappClass 需要转化的Class
	 */
	public XMLHelper(File files, Class<?> wrappClass) {
		this.setFile(files);
		this.setMapping(wrappClass);
	}

	public XMLHelper() {
	}

	public XMLHelper(InputStream inputStream, Class<?> wrappClass) {
		this.setInputStream(inputStream);
		this.setMapping(wrappClass);
	}
	
	public String getCharset() {
		return charset;
	}

	/**
	 * 设置读取xml的编码
	 * 
	 * @param charset 字符编码
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getNodeName() {
		return nodeName;
	}

	/**
	 * 添加命名映射，已弃用
	 * 
	 * @param name 名称
	 * @param field 属性
	 */
	@Deprecated
	public void addNameMaping(String name, String field) {
		this.nameMapping.put(name, field);

	}
	/**
	 * 添加名称映射
	 * @param name 节点名
	 * @param cls 类
	 */
	public void addMapMapping(String name, Class<?> cls) {
		this.mapMapping.put(name, cls);
	}

	/**
	 * 设置根节点路径
	 * 
	 * @param nodeName 节点路径
	 */
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * 设置映射类
	 * 
	 * @param mappingClass 映射类
	 */
	public void setMapping(Class<?> mappingClass) {
		this.mapping = mappingClass;
		if(this.nodeName == null || this.nodeName.equals("")) {
			this.nodeName = mapping.getName();
		}
		com.yanan.utils.beans.xml.Element element = mapping
				.getAnnotation(com.yanan.utils.beans.xml.Element.class);
		if (element != null)
			this.nodeName = element.name();
	}

	/**
	 * 设置文件
	 * 
	 * @param file 文件
	 */
	public void setFile(File file) {
		if (file == null || !file.exists())
			throw new RuntimeException("file \"" + file + "\"is not exists");
		if (!file.canRead())
			throw new RuntimeException("file \"" + file + "\" can not be read");
		try {
			this.inputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 读取xml文件，并将xml文件转化为目标聚合对像输出
	 * 
	 * @param <T> 实例类型
	 * @return 实例
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> read() {
		if (this.inputStream == null)
			throw new RuntimeException(
					"the xml inputStream is null,check to see if you forgot to add the input stream！");
		if (this.mapping == null)
			throw new RuntimeException("could not find mapping class");
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(inputStream, charset);
			if (this.nodeName == null)
				throw new RuntimeException("node name is null");
			List<?> pNode = document.selectNodes(nodeName);
			if (pNode == null)
				throw new RuntimeException("root node is null at node name" + nodeName);
			if (pNode.size() == 0)
				throw new RuntimeException("XML Path has not any content at : " + nodeName);
			// 遍历节点
			rootElement(pNode);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		return (List<T>) this.beanObjectList;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<?> clazz) {
		try {
			return (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("failed to instance class",e);
		}
	}

	/**
	 * 根节点的处理
	 * 
	 * @param nodeElement 节点元素
	 */
	public void rootElement(List<?> nodeElement) {
		Iterator<?> eIterator = nodeElement.iterator();
		while (eIterator.hasNext()) {
			Object obj = newInstance(this.mapping);
			Node node = (Node) eIterator.next();
			Field[] fileds = ReflectUtils.getAllFields(mapping);
			for (Field field : fileds) {
				// 交给Field处理
				try {
					Object object = this.processField(node, field);
					if (object != null) {
						ReflectUtils.setFieldValue(field, obj, object);
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
					throw new RuntimeException(e);
				}
			}
			this.beanObjectList.add(obj);
		}
	}
	

	/**
	 * Field的处理
	 * 
	 * @param node 当前节点
	 * @param field java映射的Field
	 * @param classHelper 需要一个ClassHelper来获取反射信息
	 * @param level 当前扫描层次
	 * @return 对象 
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	public Object processField(Node node, Field field)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		Class<?> fieldType = field.getType();
		Object object = null;
		if (field.getAnnotation(Ignore.class) != null)
			return null;
		if (field.getAnnotation(NodeName.class) != null)
			return node.getName();
		if (field.getAnnotation(NodePath.class) != null)
			return node.getPath();
		if (field.getAnnotation(NodeUniquePath.class) != null)
			return node.getUniquePath();
		// get the node info from Element annotation
		com.yanan.utils.beans.xml.Element element = field
				.getAnnotation(com.yanan.utils.beans.xml.Element.class);
		String nodeName = this.getNodeName(field, element);
		if (ParameterUtils.isBaseType(fieldType)) {
			// if the field is base java data array or String array , need another method to proccess
			if (fieldType.isArray()) {
				// get the array's origin type
				Class<?> arrayType = fieldType.getComponentType();
				// get the node from document
				List<?> nodes = node.selectNodes(nodeName);
				// call nodes array method to process the field
				object = getNodesValues(nodes, arrayType);
			} else {
				com.yanan.utils.beans.xml.Attribute attribute = field
						.getAnnotation(com.yanan.utils.beans.xml.Attribute.class);
				if (attribute != null) {
					if (!attribute.name().trim().equals(""))
						nodeName = attribute.name();
					if(node!=null)
						object = ((Element) node).attributeValue(nodeName);
					// if (object == null)
					// throw new RuntimeException(
					// "node attribute is null;\r\nat node " + node.getPath() +
					// "\r\nat nodeName " + nodeName);
				} else if (element != null) {
					Element signleNode = (org.dom4j.Element) node.selectSingleNode(nodeName);
					if (signleNode != null)
						object = signleNode.getTextTrim();
				} else {
					Value value = field.getAnnotation(Value.class);
					if (value != null) {
						object = node.getText();
					} else {
						AsXml as = field.getAnnotation(AsXml.class);
						if (as != null) {
							object = node.asXML();
						} else {
							object = ((Element) node).attributeValue(nodeName);
							if (object == null) {
								Element signleNode = (org.dom4j.Element) node.selectSingleNode(nodeName);
								if (signleNode != null)
									object = signleNode.getTextTrim();
							}
						}
					}
				}
			}
		} else {
			// rename node name
			if (ReflectUtils.implementsOf(fieldType, List.class)) {
				//process List node
				//if the node is multiple mapping node ,use reverse scan document node 
				MappingGroup groups = field.getAnnotation(MappingGroup.class);
				if (groups == null)
					object = this.buildListNode(field, node, nodeName);
				else
					object = this.buildGroupListNode(field, node, groups);
			} else if (ReflectUtils.implementsOf(fieldType, Map.class)) {
				//process Map node
				MappingGroup groups = field.getAnnotation(MappingGroup.class);
				if (groups == null)
					object = this.buildMapNode(field, node, nodeName);
				else
					object = this.buildGroupMapNode(field, node, groups);
			} else if (fieldType.isArray()) {
				//process pojo array
				object = this.buildPojoArrayNode(field, node, element, fieldType, nodeName);
			} else {
				//process simple pojo
				object = this.buildPojoNode(field, node, fieldType, nodeName);
			}
		}
		return object;
	}

	/**
	 * 构建带有MappingGroup的节点
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param mappGroup mapping
	 * @return 对象
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object buildGroupListNode(Field field, Node node, MappingGroup mappGroup)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		// get type annotation
		com.yanan.utils.beans.xml.Type typeAnno = field.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		Class<?> listClass;
		// get the set object
		listClass = typeAnno != null ? typeAnno.value() : ArrayList.class;
		List realList = newInstance(listClass);
		// get all tag
		if(node==null)
			return realList;
		Iterator<?> elementIterator = ((Element) node).elementIterator();
		while (elementIterator.hasNext()) {
			Node childNode = (Node) elementIterator.next();
			String nodeName = childNode.getName();
			if (nodeName == null)
				continue;
			boolean found = false;
			for (Mapping mapping : mappGroup.value()) {
				if (mapping.node().equals(nodeName)) {
					Class<?> realClass = mapping.target();
					Object realObject =  newInstance(realClass);
					processObject(childNode, realClass, realObject);
					realList.add(realObject);
					found = true;
				}
			}
		}
		return realList;
	}

	public void processObject(Node childNode, Class<?> realClass, Object realObject)
			throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		Field[] fields =ReflectUtils.getAllFields(realClass);
		Object tempObject = null;
		for (Field f : fields) {
			tempObject = processField((Node) childNode, f);
			if (f != null) {
				ReflectUtils.setFieldValue(f, realObject, tempObject);
			}
		}
	}

	/**
	 * 构建具有Group的Map集合
	 * 
	 * @param helper helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param mappGroup mappGroup
	 * @return 对象
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object buildGroupMapNode(Field field, Node node, MappingGroup mappGroup)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		com.yanan.utils.beans.xml.Type typeAnno = field.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		Class<?> mapClass = typeAnno != null ? typeAnno.value() : HashMap.class;
		Map realMap = newInstance(mapClass);
		Iterator<?> elementIterator = ((Element) node).elementIterator();
		while (elementIterator.hasNext()) {
			Node childNode = (Node) elementIterator.next();
			String nodeName = childNode.getName();
			if (nodeName == null)
				continue;
			for (Mapping mapping : mappGroup.value()) {
				if (mapping.node().equals(nodeName)) {
					Class<?> realClass = mapping.target();
					Object realObject = newInstance(realClass);
					Field[] fields = ReflectUtils.getAllFields(realClass);
					Object tempObject = null;
					for (Field f : fields) {
						tempObject = processField((Node) childNode, f);
						if (f != null) {
							ReflectUtils.setFieldValue(field, realObject, tempObject);
						}
					}
					Field key = this.getMapKey(realClass);
					Object mapKey = ReflectUtils.getFieldValue(key, realObject);
					realMap.put(mapKey, realObject);
				}
			}
		}
		return realMap;
	}

	/**
	 * Map中获取Key值
	 * 
	 * @param helper field helper
	 * @param realClass class
	 * @return 属性
	 */
	private Field getMapKey( Class<?> realClass) {
		Field key = null;
		// 如果Field有Key属性，从Field中获取,否则从实体类中的查找，找不到则取第一Field的值
		try {
			for(Field field : ReflectUtils.getAllFields(realClass)) {
				if(field.getAnnotation(Key.class) != null)
					return field;
			}
			if (realClass.getAnnotation(Key.class) != null) {
				key = realClass.getDeclaredField(realClass.getAnnotation(Key.class).value());
			} else {
				key = realClass.getDeclaredFields()[0];
			}
		} catch (NoSuchFieldException | SecurityException e) {
			throw new RuntimeException("could not found key "+key,e);
		}
		return key;
	}
	/**
	 * 获取节点名称
	 * @param field 属性
	 * @param element 元素
	 * @return 节点名
	 */
	private String getNodeName(Field field, com.yanan.utils.beans.xml.Element element) {
		String nodeName = (element != null && !element.name().trim().equals("")) ? 
				element.name() : field.getName();
		return nodeName;
	}

	/**
	 * 构建pojo对象
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param fieldType field type
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	private Object buildPojoNode(Field field, Node node, Class<?> fieldType,
			String nodeName) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		Object object =  newInstance(fieldType);
		Field[] fields = ReflectUtils.getAllFields(fieldType);
		Object tempObject = null;
		Node childNode = node.selectSingleNode(nodeName);
		for (Field f : fields) {
			tempObject = processField(childNode, f);
			if (f != null) {
				ReflectUtils.setFieldValue(f, object, tempObject);
			}
		}
		return object;
	}

	/**
	 * 构建pojo数据
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level 
	 * @param element element
	 * @param fieldType field type
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	private Object buildPojoArrayNode( Field field, Node node,
			com.yanan.utils.beans.xml.Element element, Class<?> fieldType, String nodeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		// 获取数组的类型
		Class<?> arrayType = fieldType.getComponentType();
		// 获取节点的数据
		List<?> nodes = node.selectNodes(nodeName);
		// 获取数组数据
		Object tempArray = Array.newInstance(arrayType, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Object realObject =  newInstance(arrayType);
			Field[] fields = ReflectUtils.getAllFields(arrayType);
			Object tempObject = null;
			for (Field f : fields) {
				tempObject = processField((Node) nodes.get(i), f);
				if (tempObject != null) {
					ReflectUtils.setFieldValue(f, realObject, tempObject);
				}
			}
			Array.set(tempArray, i, realObject);
		}
		return tempArray;
	}

	/**
	 * 构建Map节点数据
	 * 
	 * @param helper filed helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object buildMapNode(Field field, Node node, String nodeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		com.yanan.utils.beans.xml.Type typeAnno = field.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		// MAP的处理
		Map realMap = null;
		Class<?> mapClass;
		mapClass = typeAnno != null ? typeAnno.value() : HashMap.class;
		realMap = newInstance(mapClass);
		// 获取泛型类型
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			// 得到泛型里的class类型对象
			Class<?> realClass = (Class<?>) pt.getActualTypeArguments()[1];
			List<?> nodes = node.selectNodes(nodeName);
			for (Object childs : nodes) {
				Object realObject = newInstance(realClass);
				Field[] fields = ReflectUtils.getAllFields(realClass);
				Object tempObject = null;
				for (Field f : fields) {
					tempObject = processField((Node) childs, f);
					if (f != null) {
						ReflectUtils.setFieldValue(f, realObject, tempObject);
					}
				}
				Field key = this.getMapKey(realClass);
				Object mapKey = ReflectUtils.getFieldValue(key, realObject);
				realMap.put(mapKey, realObject);
			}
		}
		return realMap;
	}

	/**
	 * 构造List节点数据
	 * 
	 * @param helper field helper
	 * @param field field
	 * @param node node
	 * @param level level
	 * @param nodeName node name
	 * @return object
	 * @throws IllegalAccessException ex
	 * @throws IllegalArgumentException ex
	 * @throws InvocationTargetException ex
	 * @throws NoSuchMethodException ex
	 * @throws SecurityException ex
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object buildListNode( Field field, Node node, String nodeName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		// 获取Type注解
		com.yanan.utils.beans.xml.Type typeAnno = field.getAnnotation(com.yanan.utils.beans.xml.Type.class);
		Class<?> listClass;
		// 获取集合的实例
		listClass = typeAnno != null ? typeAnno.value() : ArrayList.class;
		List realList =  newInstance(listClass);
		// 获取泛型类型
		Type type = field.getGenericType();
		if (type instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) type;
			// 得到泛型里的class类型对象
			Class<?> realClass = (Class<?>) pt.getActualTypeArguments()[0];
			if(node!=null){
				List<?> nodes = node.selectNodes(nodeName);
				for (Object childs : nodes) {
					Object realObject =  newInstance(realClass);
					Field[] fields = ReflectUtils.getAllFields(realClass);
					Object tempObject = null;
					for (Field f : fields) {
						tempObject = processField((Node) childs, f);
						if (f != null) {
							ReflectUtils.setFieldValue(f, realObject, tempObject);
						}
					}
					realList.add(realObject);
				}
			}
		}
		return realList;
	}

//	public void addField(Object object, Field field) {
//		try {
//			ReflectUtils.set(field, object);
//		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
//				| SecurityException e) {
//			throw new RuntimeException(e);
//		}
//	}

	/**
	 * 获取节点的数据
	 * 
	 * @param nodes 节点集合
	 * @param targetType 目标类型
	 * @return 实例
	 */
	protected Object getNodesValues(List<?> nodes, Class<?> targetType) {
		Object tempArrayList = Array.newInstance(targetType, nodes.size());
		for (int i = 0; i < nodes.size(); i++) {
			Element element = (org.dom4j.Element) nodes.get(i);
			Array.set(tempArrayList, i, ParameterUtils.castType(element.getText(), targetType));
		}
		return tempArrayList;
	}

	@SuppressWarnings("deprecation")
	public static Object castType(Object orgin, Class<?> targetType) {
		// 整形
		if (targetType.equals(int.class))
			return Integer.parseInt(("" + orgin).equals("") ? "0" : "" + orgin);
		if (targetType.equals(short.class))
			return Short.parseShort((String) orgin);
		if (targetType.equals(long.class))
			return Long.parseLong((String) orgin);
		if (targetType.equals(byte.class))
			return Byte.parseByte((String) orgin);
		// 浮点
		if (targetType.equals(float.class))
			return Float.parseFloat("" + orgin);
		if (targetType.equals(double.class))
			return Double.parseDouble((String) orgin);
		// 日期
		if (targetType.equals(Date.class))
			return new Date(orgin + "");
		// 布尔型
		if (targetType.equals(boolean.class))
			return Boolean.parseBoolean((String) orgin);
		// char
		if (targetType.equals(char.class))
			return (char) orgin;
		// 没有匹配到返回源数据
		return orgin;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public Map<String, String> getNameMapping() {
		return nameMapping;
	}

	public void setNameMapping(Map<String, String> nameMapping) {
		this.nameMapping = nameMapping;
	}

	public List<Object> getBeanObjectList() {
		return beanObjectList;
	}

	public void setBeanObjectList(List<Object> beanObjectList) {
		this.beanObjectList = beanObjectList;
	}

	public List<String> getRemoveNodes() {
		return removeNodes;
	}

	public void setRemoveNodes(List<String> removeNodes) {
		this.removeNodes = removeNodes;
	}

	public Map<String, Class<?>> getMapMapping() {
		return mapMapping;
	}

	public void setMapMapping(Map<String, Class<?>> mapMapping) {
		this.mapMapping = mapMapping;
	}

	public Class<?> getMapping() {
		return mapping;
	}
}
