package test.yanan.ai.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.yanan.framework.plugin.PlugsFactory;

/**
 * 决策模块有权感知其他模块
 * 决策模块应该不停的运行,无限循环
 * 如果决策模块停止，那么所有的其他模块都没有意义了
 * 决策模块应该能控制其他模块
 * 决策模块应该有一些隐士控制模块
 * 决策模块应该有焦点
 * 决策模块应该有自己感兴趣的刺激，每次都循环接收所有其他模块反馈的信息
 * 决策模块应该能快速调取某些信息
 * @author tja41
 *
 */
public class Policy implements Runnable{
	
	//用于接收接收到的消息
	private Stack<Message> messageQueue = new Stack<>();
	
	//有限的缓存消息
	private Map<MessageType,Stack<Message>> tempMessageQueue = new HashMap<>();
	@Override
	public void run() {
		while(true){
			System.err.println("执行中");
			Message message = messageQueue.pop();
			MessageType messageType = message.getType();
			MessageProcess messageProcess = PlugsFactory.getPluginsInstanceByAttribute(MessageProcess.class, messageType.name()+"_process");
			messageProcess.
		}
	}   
	
}
