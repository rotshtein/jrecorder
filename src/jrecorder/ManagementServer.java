package jrecorder;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class ManagementServer extends WebSocketServer
{
	static Logger logger = Logger.getLogger("ManagementServer");
	GuiInterface gui = null;
	ManagmentParser parser = null;
	BlockingQueue<AbstractMap.SimpleEntry<byte [], WebSocket>> queue = null;
	 
	public ManagementServer(InetSocketAddress address, GuiInterface gui)
	{
		super(address);
		this.gui = gui;
		
		try
		{
			queue = new ArrayBlockingQueue<SimpleEntry<byte[], WebSocket>>(1);
			parser = new ManagmentParser("./recorder.ini", queue, this);
			parser.start();
		}
		catch (Exception e)
		{
			logger.error("Parse error", e);
		}
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		//conn.send("Welcome to the server!"); // This method sends a message to the new client
		// broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This
		// method sends a message to all clients connected
		logger.info("new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		logger.info("closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message)
	{
		logger.warn("received message from " + conn.getRemoteSocketAddress() + ": " + message);
		conn.send("Message should be sent as byte array");
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message)
	{
		
		//conn.send("received ByteBuffer from " + conn.getRemoteSocketAddress());
		//parser.Parse(message.array(), conn);
		try
		{
			queue.put(new SimpleEntry<byte[], WebSocket>(message.array(), conn));
		}
		catch (Exception e)
		{
			logger.error("Error putting message in parser queue", e);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex)
	{
		System.err.println("an error occured on connection " + conn.getRemoteSocketAddress() + ":" + ex);
	}

	@Override
	public void onStart()
	{
		System.out.println("server started successfully");
	}
}
