package jrecorder;

import java.io.File;
import org.apache.log4j.Logger;

public class Record extends Operation
{

	final static Logger logger = Logger.getLogger("Record");

	public Record(String RecordExe, String messageFile, GuiInterface gui)
	{
		super(RecordExe, messageFile, gui, "Record");
	}

	public ProcMon Start(double f0, double Rate, double Gain, double bw, String Filename, long NumSamples) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{

			try
			{
				String vars[] =
				{ super.exe_file, "--mode", "record", "--freq", Double.toString(f0), "--rate", Double.toString(Rate),
						"--gain", Double.toString(Gain),/*"--bw", Double.toString(bw),*/  "--file", Filename, "--nsamps", Long.toString(NumSamples)
				};
				p = super.StartAction(vars);
			}
			catch (Exception ex)
			{
				throw ex;
			}
		}
		return p;
	}
}
