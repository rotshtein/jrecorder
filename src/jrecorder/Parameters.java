package jrecorder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Parameters
{

	final static Logger	logger		= Logger.getLogger("Parameters");
	File				configFile	= null;
	Properties			props;

	public Parameters(String Filename) throws Exception
	{
		configFile = new File(Filename);
		if (!configFile.exists())
		{
			if (!configFile.createNewFile())
			{
				logger.error("No configuration file");
				throw (new Exception("No configuration file"));
			}

		}
		FileReader config = new FileReader(configFile);
		props = new Properties();
		props.load(config);
		config.close();
	}

	public String Get(String name)
	{
		return Get(name, "");
	}

	public String Get(String name, String defaultValue)
	{
		String value = "";
		try
		{
			value = props.getProperty(name);
		}
		catch (Exception e)
		{
			logger.error("Failed to get parameter value", e);
		}

		if (value == null)
		{
			try
			{
				Set(name, defaultValue);
				value = defaultValue;
			}
			catch (IOException e)
			{
				logger.error("Failed to set default parameter value", e);
			}
		}
		return value;
	}

	public boolean Set(String name, String Value) throws IOException
	{
		props.setProperty(name, Value);
		FileWriter writer = new FileWriter(configFile);
		props.store(writer, "rf recorder settings");
		writer.close();
		return true;
	}
}
