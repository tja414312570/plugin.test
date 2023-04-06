package test.yanan.ai.core;

import lombok.Data;

@Data
public class Message {
	private MessageType type;
	private byte[] message;
	
}
