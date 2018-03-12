package jrecorder;

import java.io.File;

import org.apache.log4j.Logger;

public class Transmit extends Operation
{
	final static Logger logger = Logger.getLogger("Transmit");

	public Transmit(String TransmitExe, String messageFile, GuiInterface gui)
	{
		super(TransmitExe, messageFile, gui, "Transmit");
	}

	public ProcMon Start(double f0, double Rate, double Gain, String Filename, Boolean Loop) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{
			try
			{
				String LoopMode = Loop ? "--loop" : "";
				String vars[] =
				{ super.exe_file, "--mode", "play", "--freq",Double.toString(f0), " --rate", Double.toString(Rate), "--gain", Double.toString(Gain),
						"--file", Filename, LoopMode, "--exe", super.exe_file };
				p = super.StartAction(vars);
			} catch (Exception ex)
			{
				throw ex;
			}
		}
		return p;
	}
	
	
}
