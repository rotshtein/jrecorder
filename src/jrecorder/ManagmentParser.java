package jrecorder;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.google.protobuf.InvalidProtocolBufferException;

import recorder_proto.Recorder.Ack;
import recorder_proto.Recorder.Header;
import recorder_proto.Recorder.OPCODE;
import recorder_proto.Recorder.PlayCommand;
import recorder_proto.Recorder.RecordCommand;
import recorder_proto.Recorder.STATUS;
import recorder_proto.Recorder.SpectrumCommand;
import recorder_proto.Recorder.StatusReplay;

public class ManagmentParser implements Runnable , ManagementInfo
{
	Logger logger = Logger.getLogger("ManagmentParser");
	ManagementServer server = null;
	
	public ManagmentParser()
	{
		
	}
	
	@Override
	public void run()
	{
		// TODO Auto-generated method stub
		
	}
	
	private void Parse(byte [] buffer)
	{
		Header h = getHeader(buffer);
		if (h == null)
		{
			return;
		}
		
		Header hh = Header.newBuilder().setSequence(h.getSequence()).build();
		
		switch (h.getOpcode())
		{
		case HEADER:
			break;
			
		case SPECTRUM:
			SpectrumCommand s = null;
			try
			{
				s = SpectrumCommand.parseFrom(h.getMessageData());
			}
			catch (InvalidProtocolBufferException e)
			{
				logger.error("Failed to parse SpectrumCommand", e);
			}
		break;
		
		case RECORD:
			RecordCommand r = null;
			try
			{
				r = RecordCommand.parseFrom(h.getMessageData());
			}
			catch (InvalidProtocolBufferException e)
			{
				logger.error("Failed to parse RecordCommand", e);
			}
		break;
		
		case PLAY_CMD:
			PlayCommand p = null;
			try
			{
				p = PlayCommand.parseFrom(h.getMessageData());
			}
			catch (InvalidProtocolBufferException e)
			{
				logger.error("Failed to parse PlayCommand", e);
			}
			break;
			
		case STOP_CMD:
			break;
			
		case STATUS_REQUEST:
			StatusReplay sr = StatusReplay.newBuilder()
				.setError(errorCondition)
				.setErrorMMessage(errorMessahe)
				.setWarning(warnCondition)
				.setWarningMessage(warnMessahe)
				.setPlayedSamples(playedSamples)
				.setReceivedSamples(recordedSamples)
				.setErrorSamples(errorSamples)
				.setStatus(STATUS.RUN)
				.build();
			
			break;
			
		default:
			break;
		}
	}

	private void SendAck(Header h)
	{
		Header hh = Header.newBuilder()
				.setSequence(h.getSequence())
				.setOpcode(OPCODE.ACK)
				.build();
	}
	private Header getHeader(byte [] buffer)
	{
		Header h = null;
		try
		{
			h = Header.parseFrom(buffer);
		}
		catch (InvalidProtocolBufferException e)
		{
			logger.error("Failed to parse message header", e);
		}
		
		return h;
	}
	
	MANAGMENT_STATUS last_status;
	Boolean errorCondition = false;
	String errorMessahe = "";
	Boolean warnCondition = false;
	String warnMessahe = "";
	double playedSamples = 0;
	double recordedSamples = 0;
	double errorSamples = 0;
	WebSocket webConnection = null;
	
	
	public void setConnection(WebSocket conn)
	{
		webConnection = conn;
	}
	
	@Override
	public void getStatus(MANAGMENT_STATUS s)
	{
		last_status = s;
	}

	@Override
	public void setError(Boolean e)
	{
		errorCondition = e;
	}

	@Override
	public void setErrorMessage(String e)
	{
		errorMessahe = e;
	}

	@Override
	public void setWarn(Boolean w)
	{
		warnCondition = w;
	}

	@Override
	public void setWarnMessage(String w)
	{
		warnMessahe = w;
	}

	@Override
	public void setPlayedSamples(double p)
	{
		playedSamples = p;
	}

	@Override
	public void setRecordedSamples(double r)
	{
		recordedSamples = r;
	}

	@Override
	public void setErrorSamples(double e)
	{
		errorSamples = e;
	}

}
