package jrecorder;

import java.io.File;
import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;
import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import com.google.protobuf.InvalidProtocolBufferException;

import recorder_proto.Recorder.ConnectionStatus;
import recorder_proto.Recorder.Header;
import recorder_proto.Recorder.OPCODE;
import recorder_proto.Recorder.PlayCommand;
import recorder_proto.Recorder.RecordCommand;
import recorder_proto.Recorder.STATUS;
import recorder_proto.Recorder.SpectrumCommand;
import recorder_proto.Recorder.StatusMessage;
import recorder_proto.Recorder.StatusReplay;

public class ManagmentParser extends Thread implements GuiInterface 
{
	Logger logger = Logger.getLogger("ManagmentParser");
	ManagementServer server = null;
	Parameters param = null;
	ProcMon procMon = null;
	Boolean connectionStatus = false;
	BlockingQueue<AbstractMap.SimpleEntry<byte [], WebSocket>> queue = null;
	
	
	public ManagmentParser(String ParametersFile, BlockingQueue<AbstractMap.SimpleEntry<byte [], WebSocket>> queue, ManagementServer server) throws Exception 
	{
		param = new Parameters(ParametersFile);
		this.queue = queue;
		this.server = server;
	}
	
	@Override
	public void run()
	{

		while (true)
		{
			AbstractMap.SimpleEntry<byte [], WebSocket> request = null;
			try
			{
				request = queue.take();
			}
			catch (InterruptedException e)
			{
				logger.error("Error getting request from queue", e);
				continue;
			}
			Parse(request.getKey(), request.getValue());
		}
	}

	public void Parse(byte [] buffer, WebSocket conn)
	{
		Header h = getHeader(buffer);
		if (h == null)
		{
			SendNck(h, conn);
			return;
		}
		
		switch (h.getOpcode())
		{
		case HEADER:
			SendNck(h, conn);
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
				SendNck(h, conn);
				return;
			}
			// run Spectrum
			
			String SpectrumExe = s.getApplicationExecute();

			if (!new File(SpectrumExe).exists())
			{
				SendStatusMessage("Spectrum exec not found. Please fix the configuration file", conn);
				SendNck(h, conn);
				return;
			}

			SpectrumWindow sw = new SpectrumWindow(SpectrumExe);

			procMon = sw.GetMessurment(s.getFrequency(), s.getRate(), s.getGain(), s.getFilename());
			/*
			try
			{
				sw.Show(s.getFilename());
				SendStatusMessage("Showing spectrum ....", conn);
			} catch (FileNotFoundException e)
			{
				//JOptionPane.showMessageDialog(f, "Error while building spectrum.", "Spectrum", JOptionPane.ERROR_MESSAGE);
				SendStatusMessage("Error while Showing spectrum",conn);
				logger.error("Error while Showing spectrum");
				SendNck(h, conn);
			}*/
			SendAck(h,conn);
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
				SendNck(h, conn);
			}
			//super.exe_file, "--mode", "record", "--freq",Double.toString(f0), " --rate",Double.toString(Rate), "--gain", Double.toString(Gain),
			//"--file",Filename, "--nsamps", Double.toString(NumSamples) };
			
			Record rec = new Record(r.getApplicationExecute(), "./spectrum.txt", this);
			try
			{
				procMon = rec.Start(r.getFrequency(), r.getRate(), r.getGain(), r.getFilename(), r.getNumberOfSamples());
				SendStatusMessage("Spectrum exec not found. Please fix the configuration file", conn);
				logger.info("Starting to record");
				SendAck(h,conn);
			} catch (Exception e)
			{
				SendStatusMessage("Spectrum exec not found. Please fix the configuration file", conn);
				logger.error("Spectrum exec not found. Please fix the configuration file,e");
			}
			SendNck(h,conn);
			
			
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
				SendNck(h, conn);
			}
			
			Transmit tx = new Transmit(p.getApplicationExecute(), "./spectrum.txt", this);
			try
			{
				//double f0, double Rate, double Gain, String Filename, Boolean Loop
				procMon = tx.Start(p.getFrequency(), p.getRate(), p.getGain(), p.getFilename(),p.getLoop());
				SendStatusMessage("Spectrum exec not found. Please fix the configuration file", conn);
				logger.info("Starting to record");
				SendAck(h,conn);
			} catch (Exception e)
			{
				SendStatusMessage("Spectrum exec not found. Please fix the configuration file", conn);
				logger.error("Spectrum exec not found. Please fix the configuration file,e");
			}
			SendNck(h,conn);
			break;
			
		case STOP_CMD:
			SendAck(h,conn);
			break;
			
		case STATUS_REQUEST:
			StatusReplay sr = null;
			if (procMon != null)
			{
				sr = StatusReplay.newBuilder()
				.setError(false)
				.setErrorMMessage("")
				.setWarning(false)
				.setWarningMessage("")
				.setPlayedSamples(10)
				.setReceivedSamples(20)
				.setErrorSamples(0)
				.setStatus(procMon.isComplete() ? STATUS.STOP : STATUS.RUN)
				.build();
			}
			else
			{
				sr = StatusReplay.newBuilder()
						.setStatus(STATUS.STOP)
						.build();
			}
			Header hh = Header.newBuilder()
					.setSequence(h.getSequence())
					.setMessageData(sr.getErrorMMessageBytes())
					.build();
			conn.send(hh.toByteArray());
			break;
			
		default:
			SendNck(h, conn);
			break;
		}
	}

	private void SendAck(Header h, WebSocket conn)
	{
		Header hh = Header.newBuilder()
				.setSequence(h.getSequence())
				.setOpcode(OPCODE.ACK)
				.build();
		conn.send(hh.toByteArray());
		
	}
	
	private void SendNck(Header h, WebSocket conn)
	{
		Header hh = Header.newBuilder()
				.setSequence(h.getSequence())
				.setOpcode(OPCODE.NACK)
				.build();
		conn.send(hh.toByteArray());
		
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
	
	private void SendStatusMessage(String message, WebSocket conn)
	{
		if (conn != null)
		{
			StatusMessage s = StatusMessage.newBuilder()
					.setMessage(message)
					.build();
			Header h = Header.newBuilder()
				.setSequence(0)
				.setOpcode(OPCODE.STATUS_MESSAGE)
				.setMessageData(s.toByteString())
				.build();

			conn.send(h.toByteArray());
		}
	}
	
	public Boolean isConnected()
	{
		return connectionStatus;
	}
	
	public Boolean isRunning()
	{
		if (procMon == null)
		{
			return false;
		}
		
		return !procMon.isComplete();
	}
	
	public void Kill()
	{
		
	}
	/*
	MANAGMENT_STATUS last_status;
	Boolean errorCondition = false;
	String errorMessahe = "";
	Boolean warnCondition = false;
	String warnMessahe = "";
	double playedSamples = 0;
	double recordedSamples = 0;
	double errorSamples = 0;
	WebSocket webConnection = null;
	*/
	@Override
	public void onConnectionChange(Boolean status)
	{
		if (!connectionStatus.equals(status))
		{
			ConnectionStatus s = ConnectionStatus.newBuilder().setStatus(status).build();
			Header h = Header.newBuilder()
					.setSequence(0)
					.setOpcode(OPCODE.CONNECTION_STATUS)
					.setMessageData(s.toByteString())
					.build();
			
			connectionStatus = status;
			logger.info(" Connection Status changes to " + (status ? "Connected" : "Disconnected"));
			for (WebSocket conn : server.connections())
			{
				conn.send(h.toByteArray());
			}
		}
	}

	@Override
	public void OperationCompleted()
	{
		StatusReplay s = StatusReplay.newBuilder()
				.setStatus(STATUS.STOP)
				.build();
		Header h = Header.newBuilder()
				.setSequence(0)
				.setOpcode(OPCODE.STATUS_REPLAY)
				.setMessageData(s.toByteString())
				.build();
		
		for (WebSocket conn : server.connections())
		{
			conn.send(h.toByteArray());
		}
	}
	/*
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
*/
	@Override
	public void UpdateStatus(String status)
	{
		for (WebSocket conn : server.connections())
		{
			SendStatusMessage(status, conn);
		}
	}

	

}
