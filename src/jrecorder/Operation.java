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
	Thread 			procMonThread;
	Thread 			feedbackFileThread; 

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
				p = Runtime.getRuntime().exec(vars, null);
				procMon = new ProcMon(p, operation);
				procMonThread = new Thread(procMon,operation + "procMon");
				procMonThread.start();
				feedbackFileThread = new Thread(this, operation + "feedback reader");
				feedbackFileThread.start();
			} catch (Exception ex)
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

	@Override
	public void run()
	{
		FeedbackFile ff = null;
		String message;

		for (int i = 0; i < 10; i++)
		{
			try
			{
				ff = new FeedbackFile("/media/sf_share/spectrum.txt");// (messageFile);
			} catch (Exception e)
			{
				try
				{
					Thread.sleep(50);
				} catch (InterruptedException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}
		if (ff == null)
		{
			gui.UpdateStatus("Faild in open feedback file");
			logger.error("Faild in open feedback file");
			return;
		}
		while (true)
		{
			// Read feedback file
			if ((message = ff.GetNext()) != null)
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
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
