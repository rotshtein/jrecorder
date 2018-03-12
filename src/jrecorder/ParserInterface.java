package jrecorder;

import org.java_websocket.WebSocket;

public interface ParserInterface
{

	void Parse(byte[] buffer, WebSocket conn);

	void onConnectionChange(Boolean status);

	void OperationCompleted();
}
