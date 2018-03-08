package jrecorder;

import java.io.File;
import org.apache.log4j.Logger;

public class Record extends Operation
{
	final static Logger logger = Logger.getLogger("Record");

	public Record(String TransmitExe, String messageFile, GuiInterface gui)
	{
		super(TransmitExe, messageFile, gui, "Record");
	}

	public ProcMon Start(double f0, double Rate, double Gain, String Filename, double NumSamples) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{

			try
			{
				String vars[] =
				{ super.exe_file, "--mode", "record", "--freq",Double.toString(f0), " --rate",Double.toString(Rate), "--gain", Double.toString(Gain),
						"--file",Filename, "--nsamps", Double.toString(NumSamples) };
				p = super.StartAction(vars);
			} catch (Exception ex)
			{
				throw ex;
			}
		}
		return p;
	}
}
