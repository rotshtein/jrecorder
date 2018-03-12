package jrecorder;

import org.java_websocket.WebSocket;

public interface ManagementInfo
{

	public enum MANAGMENT_STATUS
	{
		RUNING, STOP, ERROR, WARN
	}

	void setConnection(WebSocket conn);

	void getStatus(MANAGMENT_STATUS s);

	void setError(Boolean e);

	void setErrorMessage(String e);

	void setWarn(Boolean w);

	void setWarnMessage(String s);

	void setPlayedSamples(double p);

	void setRecordedSamples(double r);

	void setErrorSamples(double r);
}
