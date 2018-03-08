package jrecorder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class CheckConnectivity implements Runnable
{
	final static Logger	logger	= Logger.getLogger("CheckConnectivity");
	GuiInterface		_gui	= null;
	InetAddress			inet;
	Boolean				_run	= true;

	public CheckConnectivity(GuiInterface gui, String host)
	{
		_gui = gui;
		try
		{
			inet = InetAddress.getByName(host);
		} catch (UnknownHostException e)
		{
			logger.error("Fiald in getting host by name", e);
		}

	}

	public void Stop()
	{
		_run = false;
	}

	@Override
	public void run()
	{
		_run = true;
		while (_run)
		{
			try
			{
				Boolean c = inet.isReachable(2000);
				_gui.onConnectionChange(c);
			} catch (IOException e)
			{
				logger.error("Fiald in pinging to host", e);
			}
			try
			{
				Thread.sleep(5000);
			} catch (InterruptedException e)
			{
				logger.error("Fiald in Thread.sleep", e);
			}
		}
	}
}
