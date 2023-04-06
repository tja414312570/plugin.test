package test.plugin.mvc;

import lombok.extern.slf4j.Slf4j;
import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author WLZ
 */
//@Slf4j
public class MessageConvertXmlToObj {

	private String node = "//Message//Resp";
//    @Override
//    public Object execute(BaseContext context) {
//        Document doc = xmlToObj(context.get(ContextKey.Msg));
//        //选择节点
//        return doc.asXML();
//    }
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<?> clazz) {
		try {
			return (T) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			//抛异常
			throw new IllegalStateException("failed to instalce class "+clazz, e);
		}
	}
	public <T> List<T> parseXml(String content,Class<?> clazz,String node){
		SAXReader reader = new SAXReader();
		Document document;
		try {
			document = reader.read(content);
			if (node == null)
				throw new RuntimeException("node name is null");
			List<?> pNode = document.selectNodes(node);
			if (pNode == null || pNode.size() == 0)
				throw new RuntimeException("node path not found " + node);
			// 遍历节点
			rootElement(pNode);
	}

    private <T>T xmlToObj(Class<?> clazz){
        T t = null;
       return  t;
    }
    /**
     * 对象转成 xml 元素 （注意是节点，不是document）
     * @return
     */
    private Object eleToObj(Object ... obj) {
        //
        return null;
    }

    /**
     * 判断字段是否是基本类型
     * @param field
     * @return 是基本类型返回true，否则返回 false
     */
    private boolean isPrimaryType(Field field) {
        final Type genericType = field.getGenericType();
        if ( genericType.equals(String.class) ||

                genericType.equals(char.class) ||

                genericType.equals(Byte.class) ||
                genericType.equals(byte.class) ||

                genericType.equals(Short.class) ||
                genericType.equals(short.class) ||

                genericType.equals(Integer.class) ||
                genericType.equals(int.class) ||

                genericType.equals(Long.class) ||
                genericType.equals(long.class) ||

                genericType.equals(Float.class) ||
                genericType.equals(float.class) ||

                genericType.equals(Double.class) ||
                genericType.equals(double.class) ||

                genericType.equals(Boolean.class) ||
                genericType.equals(boolean.class) ||

                genericType.equals(Number.class)
            ) {
                return true;
            }
        return false;
    }


}
