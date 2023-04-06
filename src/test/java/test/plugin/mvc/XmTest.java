package test.plugin.mvc;

import java.io.File;

import com.sun.javafx.collections.MappingChange.Map;
import com.yanan.utils.beans.xml.TypeToken;
import com.yanan.utils.beans.xml.XMLHelper;

public class XmTest {
	
	
	public static void main(String[] args) {
		XMLHelper xmlHelp = new XMLHelper(new File("C:\\Users\\tja41\\Desktop\\test.xml"),Message.class);
		System.err.println(xmlHelp.read());
		xmlHelp = new XMLHelper(new File("C:\\Users\\tja41\\Desktop\\test.xml"),MessageBody.class);
		xmlHelp.setNodeName("//Message//Resp");
		System.err.println(xmlHelp.read());
	}
}
