package test.yanan.ai.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.yanan.framework.plugin.PlugsFactory;

/**
 * ����ģ����Ȩ��֪����ģ��
 * ����ģ��Ӧ�ò�ͣ������,����ѭ��
 * �������ģ��ֹͣ����ô���е�����ģ�鶼û��������
 * ����ģ��Ӧ���ܿ�������ģ��
 * ����ģ��Ӧ����һЩ��ʿ����ģ��
 * ����ģ��Ӧ���н���
 * ����ģ��Ӧ�����Լ�����Ȥ�Ĵ̼���ÿ�ζ�ѭ��������������ģ�鷴������Ϣ
 * ����ģ��Ӧ���ܿ��ٵ�ȡĳЩ��Ϣ
 * @author tja41
 *
 */
public class Policy implements Runnable{
	
	//���ڽ��ս��յ�����Ϣ
	private Stack<Message> messageQueue = new Stack<>();
	
	//���޵Ļ�����Ϣ
	private Map<MessageType,Stack<Message>> tempMessageQueue = new HashMap<>();
	@Override
	public void run() {
		while(true){
			System.err.println("ִ����");
			Message message = messageQueue.pop();
			MessageType messageType = message.getType();
			MessageProcess messageProcess = PlugsFactory.getPluginsInstanceByAttribute(MessageProcess.class, messageType.name()+"_process");
			messageProcess.
		}
	}   
	
}
