package jrecorder;

import java.io.File;

import org.apache.log4j.Logger;

public abstract class Operation implements Runnable
{

	final static Logger logger = Logger.getLogger("Operation");

	String			exe_file	= "";
	Process			p			= null;
	ProcMon			procMon;
	String			messageFile;
	GuiInterface	gui;
	String			operation;
	Thread			procMonThread;
	Thread			feedbackFileThread;
	Boolean			stopReadingFeedbackFile = false;

	public Operation(String Exe, String messageFile, GuiInterface gui, String Operation)
	{
		exe_file = Exe;
		this.messageFile = messageFile;
		this.gui = gui;
		operation = Operation;
	}

	public ProcMon StartAction(String vars[]) throws Exception
	{
		if (new File(exe_file).exists())
		{
			try
			{
				ProcessBuilder builder = new ProcessBuilder(vars);
				builder.redirectOutput(new File(messageFile));
				builder.redirectError(new File(messageFile));
				p = builder.start(); // may throw IOException
				String CommandLine = "";
				for(String var : vars)
				{
					CommandLine += var +" ";
				}
				
				logger.info("Starting " + operation + ": " +CommandLine);
				// while (!p.isAlive());

				procMon = new ProcMon(p, operation);
				procMonThread = new Thread(procMon, operation + "procMon");
				procMonThread.start();

				feedbackFileThread = new Thread(this, operation + "feedback reader");
				feedbackFileThread.start();
			}
			catch (Exception ex)
			{
				logger.error("Failed to start " + operation + " process", ex);
				return null;
			}
			return procMon;
		}
		throw new Exception(exe_file + "Not fount. Please check the configuration file");
	}

	public boolean isComplete()
	{
		if (procMon != null)
		{
			return procMon.isComplete();
		}
		return false;
	}

	public void Stop()
	{
		logger.debug("Request to exit Feedback reader using Stop()");
		stopReadingFeedbackFile = true;
	}
	
	@Override
	public void run()
	{
		//stopReadingFeedbackFile = false;
		FeedbackFile ff = null;
		String message;

		if (messageFile != null)
		{
			

			for (int i = 0; i < 120; i++)
			{
				if (stopReadingFeedbackFile) 
				{
					logger.debug("Exiting Feedback reader ofter Stop()");
					return;
				}
				
				try
				{
					ff = new FeedbackFile(messageFile);
					break;
				}
				catch (Exception e)
				{
					logger.debug("No feedback file yet");
				}
				
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e1)
				{
					//logger.debug("Error in Thread.Sleep", e1);
				}

			}
			
			if (ff == null)
			{
				gui.UpdateStatus("Faild in open feedback file");
				logger.error("Faild in open feedback file");
				return;
			}
			
			while (p.isAlive() | stopReadingFeedbackFile)
			{
				// Read feedback file
				while ((message = ff.GetNext()) != null)
				{
					// Update status bar
					gui.UpdateStatus(message);
				}
	
				// call complete and exit when ended
				if (isComplete())
				{
					gui.UpdateStatus("End transmitting file");
					logger.info("End transmitting file");
					break;
				}
	
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e)
				{
					logger.error("Failed to sleep",e);
				}
			}
		}
		
		while ((message = ff.GetNext()) != null)
		{
			// Update status bar
			gui.UpdateStatus(message);
		}
		logger.info("Exiting feednbak file thread");

	}
}
