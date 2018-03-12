package jrecorder;

import java.net.URI;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import recorder_proto.Recorder.ConnectionStatus;
import recorder_proto.Recorder.Header;
import recorder_proto.Recorder.OPCODE;
import recorder_proto.Recorder.PlayCommand;
import recorder_proto.Recorder.RecordCommand;
import recorder_proto.Recorder.SpectrumCommand;
import recorder_proto.Recorder.StatusMessage;
import recorder_proto.Recorder.StatusReplay;


class ManagementClient extends WebSocketClient 
{
	static Logger logger = Logger.getLogger("ManagementClient");
	GuiInterface gui = null;
	Boolean connectionStatus = false;
	ManagementClient conn;
	Boolean gotAck = false;
	Boolean gotNck = false;
	
	public ManagementClient(URI serverUri, GuiInterface gui)
	{
		super(serverUri);
		this.gui = gui;
		this.connect();
	}

	@Override
	public void onOpen(ServerHandshake handshakedata)
	{
		logger.info("Connected");

	}

	@Override
	public void onMessage(String message)
	{
		logger.info("got message: " + message);
	}
	@Override
	public void onMessage(ByteBuffer buffer)
	{
		Header h = null;
		try
		{
			h = Header.parseFrom(buffer);
			
			if (h != null)
			{
				logger.info("Got header. Command = " + h.getOpcode());
			}
			//int i = h.getOpcodeValue();
			switch (h.getOpcode())
			{
			case HEADER:
				logger.error("Got header only");
				break;
			case SPECTRUM:
				logger.error("Got spectrum");
				
			case RECORD:
				logger.error("Got spectrum");
				break;

			case PLAY_CMD:
				logger.error("Got play");
			
			case STOP_CMD:
				logger.error("Got Stop command");
				break;
				
			case STATUS_REQUEST:
				logger.error("Got Status request");
				break;
				
			case ACK:
				gotAck = true;
				break;
				
			case NACK:
				gotNck = true;
				break;
				
			case STATUS_REPLAY:
				StatusReplay sr = StatusReplay.parseFrom(h.getMessageData()); 
				
				gui.UpdateStatus(sr.getStatusDescription());
				
				if (sr.getWarning())
				{
					gui.UpdateStatus(sr.getWarningMessage());
				}
				
				if (sr.getError())
				{
					gui.UpdateStatus(sr.getErrorMMessage());
				}
				break;
				
			case STATUS_MESSAGE:
				StatusMessage sm = StatusMessage.parseFrom(h.getMessageData()); 
				gui.UpdateStatus(sm.getMessage());
				break;
				
			case CONNECTION_STATUS:
				ConnectionStatus cs = ConnectionStatus.parseFrom(h.getMessageData());
				connectionStatus = cs.getStatus();
				gui.onConnectionChange(connectionStatus);
				break;

			
			default:
				logger.error("Unknown command.");
				break;
			}
			
			
		}
		catch (InvalidProtocolBufferException e)
		{
			logger.error("Protocol buffer Header parsing error",e);
		}
		
	}

	@Override
	public void onClose(int code, String reason, boolean remote)
	{
		logger.info("Disconnected");
	}

	@Override
	public void onError(Exception ex)
	{
		logger.error("Wensocket error",ex);
	}

	public Boolean SendRecordCommand()
	{
		return false;
	}
	
	
	public Boolean SendSpectrumCommand(double CenterFrequncy,double Rate, double Gain, String SpectrumBin, String SpectrumExe)
	{
		SpectrumCommand s = SpectrumCommand.newBuilder()
				.setApplicationExecute(SpectrumExe)
				.setFilename(SpectrumBin)
				.setGain(Gain)
				.setFrequency(CenterFrequncy)
				.setRate(Rate)
				.build();
		Header h = Header.newBuilder()
				.setSequence(0)
				.setOpcode(OPCODE.SPECTRUM)
				.setMessageData(s.toByteString())
				.build();
		
		this.send(h.toByteArray());
		return true;
	}
	
	public Boolean SendPlayCommand(double CenterFrequncy,double Rate, double Gain, Boolean Loop, String Filename, String PlayExe)
	{
		PlayCommand p = PlayCommand.newBuilder()
				.setFrequency(CenterFrequncy)
				.setRate(Rate)
				.setGain(Gain)
				.setFilename(Filename)
				.setApplicationExecute(PlayExe)
				.setLoop(Loop)
				.build();
		Header h = Header.newBuilder()
				.setSequence(0)
				.setOpcode(OPCODE.SPECTRUM)
				.setMessageData(p.toByteString())
				.build();
		
		this.send(h.toByteArray());
		return true;
	}
	
	public Boolean SendRecordCommand(double CenterFrequncy,double Rate, double Gain, String Filename, double NumberOfSampels, String RecordExe)
	{
		RecordCommand s = RecordCommand.newBuilder()
				.setFrequency(CenterFrequncy)
				.setRate(Rate)
				.setGain(Gain)
				.setFilename(Filename)
				.setNumberOfSamples(NumberOfSampels)
				.setApplicationExecute(RecordExe)
				.build();
		
		Header h = Header.newBuilder()
				.setSequence(0)
				.setOpcode(OPCODE.RECORD)
				.setMessageData(s.toByteString())
				.build();
		
		this.send(h.toByteArray());
		return true;
	}
	
	public Boolean SendStopCommand()
	{
		Header h = Header.newBuilder()
				.setSequence(0)
				.setOpcode(OPCODE.STOP_CMD)
				.build();
		
		this.send(h.toByteArray());
		return true;
	}
	
	public void Send(int Sequence, OPCODE opcode, ByteString data)
	{
		gotAck = false;
		gotNck = false;
		
		Header h = Header.newBuilder()
				.setSequence(Sequence)
				.setOpcode(opcode)
				.setMessageData(data)
				.build();
		
		this.send(h.toByteArray());
	}
	
	public Boolean WaitForAck(long milliseconds)
	{
		long Start = System.currentTimeMillis();  
		
		while (gotAck != true)
		{
			if (System.currentTimeMillis() - Start > milliseconds)
			{
				logger.warn("Timeout in getting Ack");
				return false;
			}
		}
		return true;
	}
	
	public Boolean isAck()
	{
		return gotAck;
	}
}