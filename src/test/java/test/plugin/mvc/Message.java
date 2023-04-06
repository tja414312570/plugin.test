package test.plugin.mvc;

import com.yanan.utils.beans.xml.AsXml;
import com.yanan.utils.beans.xml.Element;

import lombok.Data;

@Data
@Element(name="//Message//OSYS")
public class Message {
	private String ServiceCode;
	private String SceneCode;
	private String SvrNodeId;
	private String GloSno;
	private String RetCode;
	private String RetMsg;
	@AsXml
	private String xml;
}
