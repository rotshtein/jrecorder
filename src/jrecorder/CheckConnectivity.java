package jrecorder;

import org.apache.log4j.Logger;

public class CheckConnectivity implements Runnable
{

	final static Logger	logger	= Logger.getLogger("CheckConnectivity");
	ConnectionInterface	_gui	= null;
	Boolean				_run	= true;
	String host = null;

	public CheckConnectivity(ConnectionInterface gui, String host)
	{
		_gui = gui;
		this.host = host;
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
