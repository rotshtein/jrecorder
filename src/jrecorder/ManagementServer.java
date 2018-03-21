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

import recorder_proto.Recorder.ConnectionStatus;
import recorder_proto.Recorder.Header;
import recorder_proto.Recorder.OPCODE;

public class ManagementServer extends WebSocketServer implements ConnectionInterface
{

	static Logger												logger				= Logger
			.getLogger("ManagementServer");
	ManagmentParser												parser				= null;
	CheckConnectivity											connectivityThread;
	BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>>	queue				= null;
	Boolean														connectionStatus	= false;

	public ManagementServer(InetSocketAddress address, String EttusAddress)
	{
		super(address);
		connectivityThread = new CheckConnectivity(this, EttusAddress);
		Thread thread = new Thread(connectivityThread);
		thread.start();
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

	public void dispose()
	{
		Stop();
	}
	
	public void Stop()
	{
		logger.info("Clossing connections");
		try
		{
			this.stop(1);
			for (WebSocket conn : this.connections())
			{
				logger.info("\tClossing connection " + conn.getLocalSocketAddress().getHostString());
				conn.close();
			}
		}
		catch (Exception e)
		{
			logger.error("Error while closing Web socket server", e);
		}
		connectivityThread.Stop();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake)
	{
		// conn.send("Welcome to the server!"); // This method sends a message to the
		// new client
		// broadcast( "new connection: " + handshake.getResourceDescriptor() ); //This
		// method sends a message to all clients connected
		logger.info("new connection to " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote)
	{
		logger.info(
				"closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
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

		// conn.send("received ByteBuffer from " + conn.getRemoteSocketAddress());
		// parser.Parse(message.array(), conn);
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
		logger.error("an error occured on connection ", ex);
	}

	@Override
	public void onStart()
	{
		logger.info("server started successfully");
	}

	@Override
	public void onConnectionChange(Boolean Status)
	{
		connectionStatus = Status;
		for (WebSocket conn : this.connections())
		{
			SendConnectionStatus(Status, conn);
		}
	}

	public void SendConnectionStatus(Boolean status, WebSocket conn)
	{
		ConnectionStatus cs = ConnectionStatus.newBuilder().setStatus(status).build();

		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.CONNECTION_STATUS)
				.setMessageData(cs.toByteString()).build();

		conn.send(h.toByteArray());
	}
}
