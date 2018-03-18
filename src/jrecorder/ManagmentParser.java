package jrecorder;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import recorder_proto.Recorder.ConnectionStatus;
import recorder_proto.Recorder.Header;
import recorder_proto.Recorder.OPCODE;
import recorder_proto.Recorder.PlayCommand;
import recorder_proto.Recorder.RecordCommand;
import recorder_proto.Recorder.STATUS;
import recorder_proto.Recorder.SpectrumCommand;
import recorder_proto.Recorder.SpectrumData;
import recorder_proto.Recorder.StatusMessage;
import recorder_proto.Recorder.StatusReplay;

public class ManagmentParser extends Thread implements GuiInterface
{

	Logger														logger				= Logger
			.getLogger("ManagmentParser");
	ManagementServer											server				= null;
	Parameters													param				= null;
	ProcMon														procMon				= null;
	Boolean														connectionStatus	= false;
	BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>>	queue				= null;
	

	public ManagmentParser(String ParametersFile, BlockingQueue<AbstractMap.SimpleEntry<byte[], WebSocket>> queue,
			ManagementServer server) throws Exception
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
			AbstractMap.SimpleEntry<byte[], WebSocket> request = null;
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

	public void Parse(byte[] buffer, WebSocket conn)
	{
		Header h = getHeader(buffer);
		if (h == null)
		{
			SendNck(h, conn);
			return;
		}
		String FeedbackFile = param.Get("FeedBackFile", "/home/x300/Statuses.txt");
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
			
			try
			{
				new File(s.getFilename()).delete();
			}
			catch (Exception e) {}
			Kill();
			
			Spectrum st = new Spectrum(SpectrumExe,FeedbackFile, this);
			
			try 
			{
				procMon = st.Start(s.getFrequency(), s.getRate(), s.getGain(), s.getFilename());
				SendAck(h, conn);
				
				File file = new File(s.getFilename());

				int i = 0;
				while (!file.exists())
				{
					Thread.sleep(100);
					if (i++ > 600)// || procMon.isComplete())
					{
						logger.error("Failed to get spectrum data");
						OperationCompleted("Failed to get spectrum data");
						return;
					}
				}

				FileInputStream fis = new FileInputStream(file);
				byte[] data = new byte[(int) file.length()];
				fis.read(data);
				fis.close();
				
				SpectrumData sd = SpectrumData.newBuilder().setMessageData(ByteString.copyFrom(data)).build();
				Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.SPECTRUM_DATA).setMessageData(sd.toByteString()).build();

				conn.send(hh.toByteArray());
				
			} 
			catch (Exception e1) 
			{
				logger.error("Failed to start Spectrum capture", e1);
				UpdateStatus("Failed to start Spectrum capture");
				SendNck(h, conn);
			}
			
			
			OperationStarted("Specturm process started");

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

			if (!new File(r.getApplicationExecute()).exists())
			{
				OperationCompleted("Recorder exec not found. Please fix the configuration file", conn);
				SendNck(h, conn);
				return;
			}
			
			Record rec = new Record(r.getApplicationExecute(), FeedbackFile, this);
			try
			{
				Kill();
				procMon = rec.Start(r.getFrequency(), r.getRate(), r.getGain(), r.getFilename(),
						r.getNumberOfSamples());
				OperationStarted(procMon.description + " Started");
				logger.info("Starting to record");
				
				SendAck(h, conn);
			}
			catch (Exception e)
			{
				OperationCompleted("Record exec not found. Please fix the configuration file", conn);
				logger.error("Spectrum exec not found. Please fix the configuration file,e");
				SendNck(h, conn);
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
				SendNck(h, conn);
			}

			if (!new File(p.getApplicationExecute()).exists())
			{
				OperationCompleted("Transmit exec not found. Please fix the configuration file", conn);
				SendNck(h, conn);
				return;
			}

			if (p.getFilename() == "" | !(new File(p.getFilename()).exists()))
			{
				OperationCompleted("Transmit data file not found. Please spcify filename", conn);
				SendNck(h, conn);
				return;
			}
			
			Transmit tx = new Transmit(p.getApplicationExecute(), FeedbackFile, this);
			try
			{
				Kill();
				procMon = tx.Start(p.getFrequency(), p.getRate(), p.getGain(), p.getFilename(), p.getLoop());
				UpdateStatus("Starting to transmirt " + p.getFilename());
				logger.info("Starting to record");
				OperationStarted(procMon.description + " Started");
			}
			catch (Exception e)
			{
				OperationCompleted("Spectrum exec not found. Please fix the configuration file", conn);
				logger.error("Spectrum exec not found. Please fix the configuration file,e");
				SendNck(h, conn);
			}

			break;

		case STOP_CMD:
			SendAck(h, conn);
			if (procMon == null)
			{
				SendStatusMessage("Process not running", conn);
				return;
			}

			if (!procMon.isComplete())
			{
				logger.warn("Killing process. [ " + procMon.description + " ]");
				UpdateStatus("Killing process. [ " + procMon.description + " ]");
				procMon.kill();
				OperationCompleted(procMon.description + " stoped by operator");
			}
			else
			{
				OperationCompleted("Process not running", conn);
			}
			break;

		case STATUS_REQUEST:
			StatusReplay sr = null;
			if (procMon != null)
			{
				sr = StatusReplay.newBuilder().setError(false).setErrorMMessage("").setWarning(false)
						.setWarningMessage("").setPlayedSamples(10).setReceivedSamples(20).setErrorSamples(0)
						.setStatus(procMon.isComplete() ? STATUS.STOP : STATUS.RUN).build();
			}
			else
			{
				sr = StatusReplay.newBuilder().setStatus(STATUS.STOP).build();
			}
			Header hh = Header.newBuilder().setSequence(h.getSequence()).setMessageData(sr.getErrorMMessageBytes())
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
		Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.ACK).build();
		conn.send(hh.toByteArray());

	}

	private void SendNck(Header h, WebSocket conn)
	{
		Header hh = Header.newBuilder().setSequence(h.getSequence()).setOpcode(OPCODE.NACK).build();
		conn.send(hh.toByteArray());

	}

	private Header getHeader(byte[] buffer)
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
			StatusMessage s = StatusMessage.newBuilder().setMessage(message).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_MESSAGE)
					.setMessageData(s.toByteString()).build();

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
		if (procMon == null)
		{
			return;
		}

		if (!procMon.isComplete())
		{
			procMon.kill();
		}
	}

	@Override
	public void onConnectionChange(Boolean status)
	{
		if (!connectionStatus.equals(status))
		{
			ConnectionStatus s = ConnectionStatus.newBuilder().setStatus(status).build();
			Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.CONNECTION_STATUS)
					.setMessageData(s.toByteString()).build();

			connectionStatus = status;
			logger.info(" Connection Status changes to " + (status ? "Connected" : "Disconnected"));
			for (WebSocket conn : server.connections())
			{
				conn.send(h.toByteArray());
			}
		}
	}

	
	public void OperationCompleted(String message)
	{
		for (WebSocket conn : server.connections())
		{
			OperationCompleted(message, conn);
		}
	}

	public void OperationCompleted(String message, WebSocket conn)
	{
		StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.STOP).setStatusDescription(message).build();
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
				.build();

		conn.send(h.toByteArray());
	}
	
	public void SendStatusReplay(StatusReplay s)
	{
		for (WebSocket conn : server.connections())
		{
			SendStatusReplay(s, conn);
		}
	}
	
	public void SendStatusReplay(StatusReplay s, WebSocket conn)
	{
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
				.build();

		conn.send(h.toByteArray());
	}
	

	public void OperationStarted(String message)
	{
		for (WebSocket conn : server.connections())
		{
			OperationStarted(message, conn);
		}
	}

	public void OperationStarted(String message, WebSocket conn)
	{
		StatusReplay s = StatusReplay.newBuilder().setStatus(STATUS.RUN).setStatusDescription(message).build();
		Header h = Header.newBuilder().setSequence(0).setOpcode(OPCODE.STATUS_REPLAY).setMessageData(s.toByteString())
				.build();

		conn.send(h.toByteArray());
	}
	
	@Override
	public void UpdateStatus(String status)
	{
		for (WebSocket conn : server.connections())
		{
			SendStatusMessage(status, conn);
		}
	}

	@Override
	public void ShowSpectrumData(byte[] data) 
	{
		logger.error("Should got to here");
		
	}

}
