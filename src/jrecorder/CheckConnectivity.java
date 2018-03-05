package jrecorder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class CheckConnectivity implements Runnable 
{
	GuiInterface _gui = null;
	InetAddress inet;
	Boolean _run = true;
	
	public CheckConnectivity(GuiInterface gui, String host)
	{
		_gui = gui;
		try {
			inet = InetAddress.getByName(host);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try 
			{
				Thread.sleep(5000);
			} 
			catch (InterruptedException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
