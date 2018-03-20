package jrecorder;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.log4j.Logger;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;


import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;


public class MainWindow implements GuiInterface
{

	final static Logger			logger			= Logger.getLogger("MainWindow");
	private JFrame				f;
	private final JRadioButton	rdbtnRecord		= new JRadioButton("Record");
	private final JRadioButton	rdbtnTransmit	= new JRadioButton("Transmit");

	final String				BANDWIDTH			= " Bandwidrh [MHz]";
	final String				LOW_FREQH			= " Low       [MHz]";
	private final JButton		btnSpectrum			= new JButton("Spectrum");
	private final JButton		btnRecord			= new JButton("Record");
	private final JButton		btnTransmit			= new JButton("Transmit");
	private final JCheckBox		chckbxLoop			= new JCheckBox("Loop");
	private final JButton		btnStop				= new JButton("Stop");
	private final JPanel		pnlFrequency		= new JPanel();
	@SuppressWarnings("rawtypes")
	private final JComboBox		cmbCenter			= new JComboBox();
	private final JSpinner		numCenter			= new JSpinner();
	private final JSpinner		numBAndwidth		= new JSpinner();
	private final JTextPane		txtBandwidth		= new JTextPane();
	private final JPanel		pnlAgc				= new JPanel();
	@SuppressWarnings("rawtypes")
	private final JComboBox		cmbAgc				= new JComboBox();
	private final JSpinner		numAgc				= new JSpinner();
	private final JPanel		pnlFileSize			= new JPanel();
	@SuppressWarnings("rawtypes")
	private final JComboBox		cmbFileSize			= new JComboBox();
	private final JSpinner		numFileSize			= new JSpinner();
	private final JButton		btnSpecifyFile		= new JButton("Specify File");
	private final JTextField	txtFileName			= new JTextField();
	private Boolean				_connectionStatus	= false;
	private final JTextField	txtIP				= new JTextField();
	private ImageIcon			Red_icon;
	private ImageIcon			Green_icon;
	private JLabel				lblLed;
	private final JPanel		pnlLed				= new JPanel();
	private Parameters			param;
	private ManagementServer	server;
	private ManagementClient	client;
	private final JPanel pnlRate = new JPanel();
	@SuppressWarnings("rawtypes")
	private final JComboBox cmbRate = new JComboBox();
	private final JLabel txtStatus = new JLabel("Statusbar");
	private final JPanel panel = new JPanel();
	
	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public MainWindow()
	{

		f = new JFrame("Main Windows");
		f.getContentPane().setLocation(0, -208);
		f.addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowOpened(WindowEvent arg0)
			{
				Init();
				logger.debug("Init spectrum");
			}

			@Override
			public void windowClosing(WindowEvent arg0)
			{
				if (server != null)
				{
					server = null;
				}
				logger.debug("Exit");
				Stop();
				System.exit(0);
			}
		});
		f.setSize(new Dimension(487, 641));
		f.setType(Type.UTILITY);
		f.setResizable(false);
		f.setTitle("RF Recorder");
		f.getContentPane().setFont(new Font("Arial", Font.PLAIN, 14));
		f.getContentPane().setBackground(UIManager.getColor("scrollbar"));
		f.getContentPane().setLayout(null);

		pnlAgc.setLayout(null);
		pnlAgc.setName("AGC");
		pnlAgc.setFont(new Font("Arial", Font.BOLD, 14));
		pnlAgc.setBorder(BorderFactory.createTitledBorder("AGC"));
		pnlAgc.setBounds(10, 181, 459, 66);

		f.getContentPane().add(pnlAgc);
		cmbAgc.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent event)
			{
				if (event.getItem().toString().equals(cmbAgc.getItemAt(0).toString()))
				{
					//txtStatus.setText("Automatic AGC");
					numAgc.setEnabled(false);
				}
				else
				{
					//txtStatus.setText("Manual AGC");
					numAgc.setEnabled(true);
				}
			}
		});
		cmbAgc.setModel(new DefaultComboBoxModel(new String[]
		{ "Automatic", "Manual" }));
		cmbAgc.setFont(new Font("Arial", Font.PLAIN, 14));
		cmbAgc.setBackground(Color.WHITE);
		cmbAgc.setBounds(10, 28, 185, 23);

		pnlAgc.add(cmbAgc);
		numAgc.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		numAgc.setEnabled(false);
		numAgc.setFont(new Font("Arial", Font.PLAIN, 14));
		numAgc.setBounds(303, 29, 143, 23);

		rdbtnRecord.setFont(new Font("Arial", Font.BOLD, 14));
		rdbtnRecord.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent arg0)
			{
				if (rdbtnRecord.isSelected())
				{
					rdbtnTransmit.setSelected(false);

					chckbxLoop.setEnabled(false);
					btnTransmit.setEnabled(false);
					btnSpectrum.setEnabled(true);
					btnRecord.setEnabled(true);
					cmbAgc.setSelectedIndex(0);
					numFileSize.setEnabled(true);
					cmbFileSize.setEnabled(true);
				}
			}
		});
		rdbtnRecord.setBackground(UIManager.getColor("scrollbar"));
		rdbtnRecord.setBounds(61, 15, 85, 23);

		rdbtnRecord.setSelected(true);
		f.getContentPane().add(rdbtnRecord);
		rdbtnTransmit.setFont(new Font("Arial", Font.BOLD, 14));
		rdbtnTransmit.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent arg0)
			{
				if (rdbtnTransmit.isSelected())
				{
					rdbtnRecord.setSelected(false);

					chckbxLoop.setEnabled(true);
					btnTransmit.setEnabled(true);
					btnSpectrum.setEnabled(false);
					btnRecord.setEnabled(false);
					cmbAgc.setSelectedIndex(1);
					numFileSize.setEnabled(false);
					cmbFileSize.setEnabled(false);

				}

			}
		});
		rdbtnTransmit.setBackground(UIManager.getColor("scrollbar"));
		rdbtnTransmit.setBounds(229, 15, 109, 23);

		f.getContentPane().add(rdbtnTransmit);
		btnSpectrum.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent event)
			{
				ShowSpectrumWindow();
			}
		});
		btnSpectrum.setFont(new Font("Arial", Font.BOLD, 14));
		btnSpectrum.setBounds(10, 475, 125, 23);

		f.getContentPane().add(btnSpectrum);
		btnRecord.setFont(new Font("Arial", Font.BOLD, 14));
		btnRecord.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent arg0)
			{

				Record();
			}
		});
		btnRecord.setBounds(147, 475, 103, 23);

		f.getContentPane().add(btnRecord);
		btnTransmit.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				Transmit();
			}
		});
		btnTransmit.setEnabled(false);
		btnTransmit.setFont(new Font("Arial", Font.BOLD, 14));
		btnTransmit.setBounds(289, 475, 103, 23);

		f.getContentPane().add(btnTransmit);
		chckbxLoop.setEnabled(false);
		chckbxLoop.setFont(new Font("Arial", Font.PLAIN, 14));
		chckbxLoop.setBounds(398, 475, 71, 23);

		f.getContentPane().add(chckbxLoop);
		btnStop.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				StopCurrentProcess();
			}
		});

		btnStop.setFont(new Font("Arial", Font.BOLD, 14));
		btnStop.setBounds(366, 505, 103, 23);

		f.getContentPane().add(btnStop);
		pnlFrequency.setFont(new Font("Arial", Font.BOLD, 14));
		pnlFrequency.setBounds(10, 79, 459, 91);

		f.getContentPane().add(pnlFrequency);
		pnlFrequency.setLayout(null);
		pnlFrequency.setName("Frequency Specification");
		pnlFrequency.setBorder(BorderFactory.createTitledBorder("Frequency Specification"));
		cmbCenter.addItemListener(new ItemListener()
		{

			public void itemStateChanged(ItemEvent event)
			{
				if (event.getItem().toString().startsWith(cmbCenter.getItemAt(0).toString()))
				{
					txtBandwidth.setText(BANDWIDTH);
					numBAndwidth.setModel(new SpinnerNumberModel(50, 1, 150, 1));
				}
				else
				{
					txtBandwidth.setText(LOW_FREQH);
					numBAndwidth.setModel(new SpinnerNumberModel(1000, 950, 2150, 1));
				}
			}
		});
		cmbCenter.setModel(new DefaultComboBoxModel(new String[]
		{ "Center [MHz]", "High    [MHz]" }));
		cmbCenter.setToolTipText("Select Center  and Bandwidth or High/Low");
		cmbCenter.setFont(new Font("Arial", Font.PLAIN, 14));
		cmbCenter.setBackground(Color.WHITE);
		cmbCenter.setBounds(10, 23, 185, 23);

		pnlFrequency.add(cmbCenter);
		numCenter.setModel(new SpinnerNumberModel(1000, 950, 2150, 1));
		numCenter.setFont(new Font("Arial", Font.PLAIN, 14));
		numCenter.setBounds(303, 23, 143, 23);

		pnlFrequency.add(numCenter);
		numBAndwidth.setModel(new SpinnerNumberModel(10, 1, 150, 1));
		numBAndwidth.setFont(new Font("Arial", Font.PLAIN, 14));
		numBAndwidth.setBounds(303, 57, 143, 23);

		pnlFrequency.add(numBAndwidth);
		txtBandwidth.setText("Bandwidth   [MHz]");
		txtBandwidth.setFont(new Font("Arial", Font.PLAIN, 14));
		txtBandwidth.setEditable(false);
		txtBandwidth.setBackground(SystemColor.scrollbar);
		txtBandwidth.setBounds(10, 57, 185, 23);

		pnlFrequency.add(txtBandwidth);

		pnlAgc.add(numAgc);
		pnlFileSize.setForeground(SystemColor.menu);
		pnlFileSize.setLayout(null);
		pnlFileSize.setName("AGC");
		pnlFileSize.setFont(new Font("Arial", Font.BOLD, 14));
		pnlFileSize.setBorder(BorderFactory.createTitledBorder("File Size"));
		pnlFileSize.setBounds(10, 335, 459, 106);

		f.getContentPane().add(pnlFileSize);
		cmbFileSize.setModel(new DefaultComboBoxModel(new String[]
		{ "Time [sec]", "Samples [x10^6]", "File Size [GB]" }));
		cmbFileSize.setFont(new Font("Arial", Font.PLAIN, 14));
		cmbFileSize.setBackground(Color.WHITE);
		cmbFileSize.setBounds(10, 31, 185, 23);

		pnlFileSize.add(cmbFileSize);
		numFileSize.setForeground(new Color(240, 240, 240));
		numFileSize.setBackground(SystemColor.text);
		numFileSize.setFont(new Font("Arial", Font.PLAIN, 14));
		numFileSize.setBounds(303, 32, 143, 23);

		pnlFileSize.add(numFileSize);
		btnSpecifyFile.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent event)
			{
				int returnVal;
				JFileChooser jfc = new JFileChooser();
				if (rdbtnRecord.isSelected()) returnVal = jfc.showSaveDialog(null);
				else returnVal = jfc.showOpenDialog(null);
				if (returnVal == JFileChooser.APPROVE_OPTION)
				{
					File file = jfc.getSelectedFile();
					txtFileName.setText(file.getAbsolutePath());
				}
			}
		});
		btnSpecifyFile.setFont(new Font("Arial", Font.BOLD, 14));
		btnSpecifyFile.setBounds(10, 65, 141, 23);

		pnlFileSize.add(btnSpecifyFile);
		txtFileName.setForeground(SystemColor.textText);
		txtFileName.setBackground(SystemColor.text);
		txtFileName.setToolTipText("Filename to record to or transmit from");
		txtFileName.setSize(new Dimension(0, 0));
		txtFileName.setFont(new Font("Arial", Font.PLAIN, 14));
		txtFileName.setColumns(10);
		txtFileName.setBounds(161, 65, 285, 23);

		pnlFileSize.add(txtFileName);

		Red_icon = new ImageIcon(
				Toolkit.getDefaultToolkit().getImage(getClass().getResource("/jrecorder/red-led.png")));
		Green_icon = new ImageIcon(
				Toolkit.getDefaultToolkit().getImage(getClass().getResource("/jrecorder/green-led.png")));
		txtIP.setHorizontalAlignment(SwingConstants.CENTER);
		txtIP.setBounds(353, 49, 116, 23);
		f.getContentPane().add(txtIP);
		txtIP.setToolTipText("Ettus IP address");
		txtIP.setForeground(Color.BLACK);
		txtIP.setFont(new Font("Arial", Font.PLAIN, 14));
		txtIP.setEditable(false);
		txtIP.setColumns(10);
		txtIP.setBackground(SystemColor.activeCaptionBorder);
		pnlLed.setBounds(288, -1, 20, 23);
		// f.getContentPane().add(pnlLed);
		lblLed = new JLabel();
		lblLed.setBounds(404, 13, 25, 25);
		f.getContentPane().add(lblLed);
		lblLed.setIcon(Red_icon);
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Status Bar", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel.setBounds(10, 555, 461, 55);
		
		f.getContentPane().add(panel);
		panel.setLayout(null);
		txtStatus.setBounds(6, 16, 443, 33);
		panel.add(txtStatus);
		txtStatus.setHorizontalAlignment(SwingConstants.LEFT);
		txtStatus.setToolTipText("hello");
		txtStatus.setVerticalAlignment(SwingConstants.BOTTOM);
		pnlRate.setLayout(null);
		pnlRate.setName("");
		pnlRate.setFont(new Font("Arial", Font.BOLD, 14));
		pnlRate.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Rate [MHz]", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		pnlRate.setBounds(10, 258, 459, 66);
		
		f.getContentPane().add(pnlRate);
		cmbRate.setBounds(10, 32, 181, 23);
		pnlRate.add(cmbRate);
		cmbRate.setModel(new DefaultComboBoxModel(new String[] {"200", "184.32", "100", "92.16", "50", "46.08", "25", "23.04"}));
		cmbRate.setToolTipText("Set the sampling rate in MHz");
		cmbRate.setFont(new Font("Arial", Font.PLAIN, 14));
		cmbRate.setBackground(Color.WHITE);
		
		f.setTitle(f.getTitle() + " - Ver 1.0");
		
		//statusBar = new StatusBar();
		//f.getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
		
		//statusBar.setText("Hello statusbar");
	}

	private void Init()
	{
		
		try
		{
			param = new Parameters("recorder.ini");
		}
		catch (Exception e)
		{
			logger.error("Can't read configuration file", e);
			return;
		}

		txtIP.setText(param.Get("ettus_address", "127.0.0.1"));

		String host = param.Get("ListenAddress", "0.0.0.0");
		int port = Integer.parseInt(param.Get("ListenPort", "8887"));

		server = new ManagementServer(new InetSocketAddress(host, port), txtIP.getText());
		logger.info("Server is listning to " + host + ":" + port);
		if (param.Get("UseSecuredSocket", "no").equalsIgnoreCase("yes"))
		{
			try 
		        {
		        	String STORETYPE = "JKS";
		    		String KEYSTORE = "server.jks";
		    		String STOREPASSWORD = "12345678";
		    		String KEYPASSWORD = "12345678";
	
		    		KeyStore ks = KeyStore.getInstance( STORETYPE );
		    		File kf = new File( KEYSTORE );
		    		ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );
	
					KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
		    		kmf.init( ks, KEYPASSWORD.toCharArray() );
		    		TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
		    		tmf.init( ks );
	
		    		SSLContext sslContext = null;
		    		sslContext = SSLContext.getInstance( "TLS" );
		    		sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
		    		//sslContext.init( null, null, null ); // will use java's default key and trust store which is sufficient unless you deal with self-signed certificates
	
		    		//SSLSocketFactory factory = sslContext.getSocketFactory();// (SSLSocketFactory) SSLSocketFactory.getDefault();
		    		server.setWebSocketFactory( new DefaultSSLWebSocketServerFactory( sslContext ) );
	
		        } 
		        catch (Exception e) 
		        {
		            logger.error("Could not initialize SSL connection", e);
		        }
		}
		server.start();

		try
		{
			String Uri = param.Get("ServerUri","ws://127.0.0.1:8887");
			logger.info("Server Uri is: " + Uri );
			client = new ManagementClient(new URI(Uri), this, "client.jks");
		}
		catch (URISyntaxException e)
		{
			logger.error("Wrong URI", e);
		}
	}

	private void Stop()
	{
		StopCurrentProcess();
	}

	private void StopCurrentProcess()
	{
		client.SendStopCommand();
	}

	public void onConnectionChange(final Boolean status)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					onConnectionChange(status);
				}
			});
			return;
		}
		// Now edit your gui objects
		if (status)
		{
			lblLed.setIcon(Green_icon);
			if (!_connectionStatus)
			{
				UpdateStatus("Ettus connected to the server");
				_connectionStatus = true;
			}
		}
		else
		{
			lblLed.setIcon(Red_icon);
			if (_connectionStatus)
			{
				UpdateStatus("Ettus disconnected from the server");
				_connectionStatus = false;
			}
		}
	}

	public void UpdateStatus(final String status)
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					UpdateStatus(status);
				}
			});
			return;
		}
		// Now edit your gui objects
		txtStatus.setText(status);
		logger.debug(status);
	}

	private void ShowSpectrumWindow()
	{
		if (!_connectionStatus)
		{
			JOptionPane.showMessageDialog(f, "No connection with the server. Please check wiring and IP address",
					"Spectrum", JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (cmbCenter.getSelectedIndex() != 0) // Center
		{
			JOptionPane.showMessageDialog(f, "Spectrum Measurement needs Central Frequency Specified", "Spectrum",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		double CenterFrequncy = (double) ((Double) numCenter.getValue()) * 1e6;

		double Gain = -1; // Automatic;
		if (cmbAgc.getSelectedIndex() == 1) // Manual
		{
			Gain = (double) numAgc.getValue();
		}

		String SpectrumExe = param.Get("SpectrumExec", "./Spectrum");

		if (!new File(SpectrumExe).exists())
		{
			JOptionPane.showMessageDialog(f, "Spectrum exec not found. Please fix the configuration file", "Spectrum",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String SpectrumBin = param.Get("SpectrumBin", "/home/x300/spectrum.dat");

		
		client.SendSpectrumCommand(CenterFrequncy, getRate(), Gain, getBW(), SpectrumBin, SpectrumExe);

		/*if (client.WaitForAck(60000) == false)
		{
			UpdateStatus("Communication timeout");
			return;
		}*/
	}

	void Record()
	{
		if (!_connectionStatus)
		{
			JOptionPane.showMessageDialog(f, "No connection with the server. Please check wiring and IP address",
					"Record", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		double Rate = getRate();
		int dNumSamples = 0;
		double Val = (double) (Integer) numFileSize.getValue();
		switch (cmbFileSize.getSelectedIndex())
		{
		case 0: // Time
			dNumSamples = (int)Math.ceil(Val * Rate);
			break;

		case 1:
			dNumSamples = (int)(Val * 1e6);
			break;

		case 2:
			dNumSamples = (int)(1e9/8 * Val);//(int)(1073741824 * Val * 0.25);
			break;
		}

		String RecorderExe = param.Get("RecorderExec", "./Spectrum");

		if (getFilename() == "")
		{
			JOptionPane.showMessageDialog(f, "Recorder data file not found. Please spcify filename", "Rercord",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		double CentralFreq = 0;
		try
		{
			CentralFreq = getF0();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(f, "Low frequency is higher than High frequency.", "Rercord",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		client.SendRecordCommand(CentralFreq, Rate, getGain(), getBW(), getFilename(), dNumSamples, RecorderExe);

	}

	private void Transmit()
	{
		if (!_connectionStatus)
		{
			JOptionPane.showMessageDialog(f, "No connection with the server. Please check wiring and IP address",
					"Transmit", JOptionPane.ERROR_MESSAGE);
			return;
		}

		
		String TransmitExe = param.Get("TransmitExec", "./Spectrum");
		String DataFile = getFilename();

		double CentralFreq = 0;
		try
		{
			CentralFreq = getF0(DataFile);
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(f, "Low frequency is higher than High frequency.", "Rercord",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		Boolean Loop = chckbxLoop.isSelected();
		
		double Rate = getRate(DataFile);
		
		
		client.SendPlayCommand(CentralFreq, Rate, getGain(DataFile), getBW(), Loop, DataFile, TransmitExe);
	}

	public void OperationCompleted()
	{

	}

	private double getF0() throws Exception
	{
		double f0;
		if (cmbCenter.getSelectedIndex() == 0) // Center
		{
			f0 = (double) ((double) numCenter.getValue()) * 1e6;
		}
		else
		{
			if ((Integer) numBAndwidth.getValue() > (Integer) numCenter.getValue())
			{
				JOptionPane.showMessageDialog(f, "Upper and Lower Frequency mismatch", "Record",
						JOptionPane.ERROR_MESSAGE);
				throw new Exception("LowFreq > HighFreq");
			}

			double bw = (Integer) numCenter.getValue() - (Integer) numBAndwidth.getValue();
			f0 = (double) ((Integer) numCenter.getValue() - (bw / 2)) * 1e6;
		}
		return f0;
	}
	
	private double getF0(String Filename)
	{
		double F0 = 1500e6;
		try 
		{
			F0 = getF0();
		} 
		catch (Exception e1) 
		{
			logger.error("Couldn't get Frequency from GUI. Default value is 1500", e1);
		}
		try
		{
			FileInputStream is = new FileInputStream (Filename);
			DataInputStream din = new DataInputStream(is);
			
			// Check magic key / Header
			byte [] MagicBytes = new byte[8];
			din.read(MagicBytes);
			String MagicKey = MagicBytes.toString();
			
			if (MagicKey.equals("RECOrder"))
			{
				/*
				
				fwrite( &version,1,8,outfile);
				fwrite( &Rate,1,8,outfile);
				fwrite( &Freq,1,8,outfile);
				fwrite( &Gain,1,8,outfile);
				*/
			// Read the rate as double (64 bit)
				din.readDouble(); // Version
				din.readDouble(); // Rate
				F0 = din.readDouble(); // Freq
			}
			
			din.close();
			is.close();
		}
		catch (Exception e)		
		{
			logger.error("Error extracting Rate from the samples transmit file",e);
		}
		cmbCenter.setSelectedIndex(0);
		numCenter.setValue(F0/1e6);
		return F0;
	}
	
	private double getBW()
	{
		double bw = 50e6;
		if (cmbCenter.getSelectedIndex() == 0) // Center
		{
			bw = (double) ((Integer) numBAndwidth.getValue()) * 1e6;
		}
		else
		{
			if ((Integer) numBAndwidth.getValue() > (Integer) numCenter.getValue())
			{
				JOptionPane.showMessageDialog(f, "Upper and Lower Frequency mismatch", "Record",
						JOptionPane.ERROR_MESSAGE);
			}
			else
			{
				bw = ((Integer) numCenter.getValue() - (Integer) numBAndwidth.getValue()) * 1e6;
			}
			
		}
		return bw;
	}

	private double getGain()
	{
		double Gain = -1; // Automatic;
		if (cmbAgc.getSelectedIndex() == 1) // Manual
		{
			Gain = (double) ((Integer) numAgc.getValue());
		}
		return Gain;
	}
	
	private double getGain(String Filename)
	{
		double Gain = 0;
		try
		{
			FileInputStream is = new FileInputStream (Filename);
			DataInputStream din = new DataInputStream(is);
			
			// Check magic key / Header
			byte [] MagicBytes = new byte[8];
			din.read(MagicBytes);
			String MagicKey = MagicBytes.toString();
			
			if (MagicKey.equals("RECOrder"))
			{
				/*
				
				fwrite( &version,1,8,outfile);
				fwrite( &Rate,1,8,outfile);
				fwrite( &Freq,1,8,outfile);
				fwrite( &Gain,1,8,outfile);
				*/
			// Read the rate as double (64 bit)
				din.readDouble(); // Version
				din.readDouble(); // Rate
				din.readDouble(); // Freq
				Gain = din.readDouble(); // Gain
			}
			din.close();
			is.close();
		}
		catch (Exception e)		
		{
			logger.error("Error extracting Rate from the samples transmit file",e);
		}
		cmbAgc.setSelectedIndex(1);
		numAgc.setValue(Gain);
		return Gain;
	}
	
	private double getRate()
	{
		double Rate = Double.parseDouble(cmbRate.getSelectedItem().toString());
		Rate = Rate * 1e6;
		try
		{
			param.Set("Rate", Double.toString(Rate));
		}
		catch (IOException e)
		{
			logger.error("Error while converting Rate to string to save in param file",e);
		}
		return Rate;
	}

	private double getRate(String Filename)
	{
		double Rate = getRate();
		try
		{
			FileInputStream is = new FileInputStream (Filename);
			DataInputStream din = new DataInputStream(is);
			
			// Check magic key / Header
			byte [] MagicBytes = new byte[8];
			din.read(MagicBytes);
			String MagicKey = MagicBytes.toString();
			
			if (MagicKey.equals("RECOrder"))
			{
				/*
				
				fwrite( &version,1,8,outfile);
				fwrite( &Rate,1,8,outfile);
				fwrite( &Freq,1,8,outfile);
				fwrite( &Gain,1,8,outfile);
				*/
			// Read the rate as double (64 bit)
				din.readDouble(); // Version
				Rate = din.readDouble(); // Version
			Rate = din.readDouble();
			}
			din.close();
			is.close();
		}
		catch (Exception e)		
		{
			logger.error("Error extracting Rate from the samples transmit file",e);
		}
		for (int i = 0; i < cmbRate.getItemCount(); i++)
		{
			if ((Rate/1e6) == Double.parseDouble(cmbRate.getItemAt(i).toString()))
			{
				cmbRate.setSelectedIndex(i); 
				break;
			}	
		}

		return Rate;
	}
	
	@Override
	public void ShowSpectrumData(final byte[] data) 
	{
		if (!SwingUtilities.isEventDispatchThread())
		{
			SwingUtilities.invokeLater(new Runnable()
			{

				@Override
				public void run()
				{
					ShowSpectrumData(data);
				}
			});
			return;
		}
		SpectrumWindow sw = new SpectrumWindow();

		try
		{
			sw.Show(data);
			UpdateStatus("Showing spectrum ....");
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(f, "Error while building spectrum.", "Spectrum", JOptionPane.ERROR_MESSAGE);
			UpdateStatus("Error while Showing spectrum");
			logger.error("Error while Showing spectrum");
		}

		
		
	}
	
	private String getFilename()
	{
		return txtFileName.getText();
	}

	public static void main(String[] args)
	{
		EventQueue.invokeLater(new Runnable()
		{

			public void run()
			{
				try
				{
					MainWindow window = new MainWindow();
					window.f.setVisible(true);
				}
				catch (Exception e)
				{
					logger.error("Failed openinng main window", e);
				}
			}
		});
	}
}
