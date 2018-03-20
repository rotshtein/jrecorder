package jrecorder;

import java.io.File;

import org.apache.log4j.Logger;


public class Spectrum extends Operation
{

	final static Logger logger = Logger.getLogger("Record");

	public Spectrum(String SpectrumExe, String messageFile, GuiInterface gui)
	{
		super(SpectrumExe, null, gui, "Spectrum");
	}

	public ProcMon Start(double f0, double Rate, double Gain, String Filename) throws Exception
	{
		ProcMon p = null;
		if (new File(super.exe_file).exists())
		{

			try
			{
				String vars[] =
				{ super.exe_file, "--mode", "spec", "--freq", Double.toString(f0), " --rate", Double.toString(Rate),
						/*"--gain", Double.toString(Gain),*/ "--file", Filename
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
