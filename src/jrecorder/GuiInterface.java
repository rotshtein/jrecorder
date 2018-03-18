package jrecorder;

public interface GuiInterface
{

	void onConnectionChange(Boolean status);

	void UpdateStatus(final String status);
	
	void ShowSpectrumData(final byte[] data);
}
