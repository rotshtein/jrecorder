package jrecorder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class CheckConnectivity implements Runnable
{

	final static Logger	logger	= Logger.getLogger("CheckConnectivity");
	ConnectionInterface	_gui	= null;
	InetAddress			inet;
	Boolean				_run	= true;
	String host = null;

	public CheckConnectivity(ConnectionInterface gui, String host)
	{
		_gui = gui;
		this.host = host;
		try
		{
			inet = InetAddress.getByName(host);
		}
		catch (UnknownHostException e)
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
				 Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 " + host);
				 int returnVal = p1.waitFor();
				 Boolean reachable = (returnVal==0);
				 //Boolean c = inet.isReachable(2000);
				_gui.onConnectionChange(reachable);
			}
			catch (Exception e)
			{
				logger.error("Fiald in pinging to host", e);
			}
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				logger.error("Fiald in Thread.sleep", e);
			}
		}
	}
}
