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

	
	public Process Start(double f0, double Rate, double Gain, String Filename, double NumSamples) throws Exception
    {
		Process p = null;
        if ( new File(super.exe_file).exists())
        {
        	
            try
            {
            	String vars[] = {super.exe_file,"--mode", "record","--freq " + f0, " --rate " + Rate, "--gain " + Gain, "--file " +  Filename, "--nsamps " + NumSamples};
            	p =  super.StartAction(vars);
            }
            catch (Exception ex)
            {
                throw ex;
            }
        }
        return p;
    }
}