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
import java.io.File;
import java.io.FileNotFoundException;

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
import javax.swing.SwingConstants;


public class MainWindow  implements GuiInterface
{
	private  JFrame f;
	private final JRadioButton rdbtnRecord = new JRadioButton("Record");
	private final JRadioButton rdbtnTransmit = new JRadioButton("Transmit");
	
	final String BANDWIDTH = " Bandwidrh [MHz]";
	final String LOW_FREQH = " Low       [MHz]";
	private final JButton btnSpectrum = new JButton("Spectrum");
	private final JButton btnRecord = new JButton("Record");
	private final JButton btnTransmit = new JButton("Transmit");
	private final JCheckBox chckbxLoop = new JCheckBox("Loop");
	private final JTextField txtStatus = new JTextField();
	private final JButton btnStop = new JButton("Stop");
	private final JPanel pnlFrequency = new JPanel();
	private final JComboBox cmbCenter = new JComboBox();
	private final JSpinner numCenter = new JSpinner();
	private final JSpinner numBAndwidth = new JSpinner();
	private final JTextPane txtBandwidth = new JTextPane();
	private final JPanel pnlAgc = new JPanel();
	private final JComboBox cmbAgc = new JComboBox();
	private final JSpinner numAgc = new JSpinner();
	private final JPanel pnlFileSize = new JPanel();
	private final JComboBox cmbFileSize = new JComboBox();
	private final JSpinner numFileSize = new JSpinner();
	private final JButton btnSpecifyFile = new JButton("Specify File");
	private final JTextField txtFileName = new JTextField();
	private Boolean _connectionStatus = false;
	private final JTextField txtIP = new JTextField();
	private ImageIcon Red_icon;
	private ImageIcon Green_icon;
	private JLabel lblLed;
	private final JPanel pnlLed = new JPanel();
	Parameters param;
	
	CheckConnectivity connectivityThread;
	public MainWindow()
	{
		txtStatus.setBackground(SystemColor.text);
		txtStatus.setForeground(SystemColor.textText);
		txtStatus.setSize(new Dimension(487, 444));
		txtStatus.setEditable(false);
		txtStatus.setFont(new Font("Arial", Font.PLAIN, 14));
		txtStatus.setBounds(0, 463, 491, 31);
		txtStatus.setColumns(10);
		 f = new JFrame("Main Windows");
		 f.addWindowListener(new WindowAdapter() {
		 	@Override
		 	public void windowOpened(WindowEvent arg0) 
		 	{
		 		 Init();
		 	}
		 	@Override
		 	public void windowClosing(WindowEvent arg0) 
		 	{
		 		Stop();
		 		System.exit(0);
		 	}
		 });
		 f.setSize(new Dimension(487, 523));
		 f.setType(Type.UTILITY);
		 f.setResizable(false);
		 f.setTitle("RF Recorder");
		 f.getContentPane().setFont(new Font("Arial", Font.PLAIN, 14));
		 f.getContentPane().setBackground(UIManager.getColor("scrollbar"));
		 f.getContentPane().setLayout(null);
		 
		 pnlAgc.setLayout(null);
		 pnlAgc.setName("AGC");
		 pnlAgc.setFont(new Font("Arial", Font.BOLD, 14));
		 pnlAgc.setBorder(BorderFactory.createTitledBorder( "AGC" ));
		 pnlAgc.setBounds(10, 181, 459, 66);
		 
		 f.getContentPane().add(pnlAgc);
		 cmbAgc.addItemListener(new ItemListener() {
		 	public void itemStateChanged(ItemEvent event) 
		 	{
		 		if (event.getItem().toString().equals(cmbAgc.getItemAt(0).toString()))
		 		{
		 			txtStatus.setText("Automatic AGC");
		 			numAgc.setEnabled(false);
		 		}
		 		else
		 		{
		 			txtStatus.setText("Manual AGC");		 			
		 			numAgc.setEnabled(true);
		 		}
		 	}
		 });
		 cmbAgc.setModel(new DefaultComboBoxModel(new String[] {"Automatic", "Manual"}));
		 cmbAgc.setFont(new Font("Arial", Font.PLAIN, 14));
		 cmbAgc.setBackground(Color.WHITE);
		 cmbAgc.setBounds(10, 28, 185, 23);
		 
		 pnlAgc.add(cmbAgc);
		 numAgc.setModel(new SpinnerNumberModel(0, 0, 31, 1));
		 numAgc.setEnabled(false);
		 numAgc.setFont(new Font("Arial", Font.PLAIN, 14));
		 numAgc.setBounds(303, 29, 143, 23);
		 
		 rdbtnRecord.setFont(new Font("Arial", Font.BOLD, 14));
		 rdbtnRecord.addItemListener(new ItemListener() {
		 	public void itemStateChanged(ItemEvent arg0) {
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
		 rdbtnTransmit.addItemListener(new ItemListener() {
		 	public void itemStateChanged(ItemEvent arg0) {
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
		 btnSpectrum.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent event) 
		 	{
		 		ShowSpectrumWindow();
		 	}
		 });
		 btnSpectrum.setFont(new Font("Arial", Font.BOLD, 14));
		 btnSpectrum.setBounds(10, 385, 125, 23);
		 
		 f.getContentPane().add(btnSpectrum);
		 btnRecord.setFont(new Font("Arial", Font.BOLD, 14));
		 btnRecord.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent arg0) {
		 	}
		 });
		 btnRecord.setBounds(145, 385, 103, 23);
		 
		 f.getContentPane().add(btnRecord);
		 btnTransmit.setEnabled(false);
		 btnTransmit.setFont(new Font("Arial", Font.BOLD, 14));
		 btnTransmit.setBounds(283, 384, 103, 23);
		 
		 f.getContentPane().add(btnTransmit);
		 chckbxLoop.setEnabled(false);
		 chckbxLoop.setFont(new Font("Arial", Font.PLAIN, 14));
		 chckbxLoop.setBounds(392, 385, 71, 23);
		 
		 f.getContentPane().add(chckbxLoop);
		 
		 f.getContentPane().add(txtStatus);
		 btnStop.setFont(new Font("Arial", Font.BOLD, 14));
		 btnStop.setBounds(366, 429, 103, 23);
		 
		 f.getContentPane().add(btnStop);
		 pnlFrequency.setFont(new Font("Arial", Font.BOLD, 14));
		 pnlFrequency.setBounds(10, 79, 459, 91);
		 
		 f.getContentPane().add(pnlFrequency);
		 pnlFrequency.setLayout(null);
		 pnlFrequency.setName("Frequency Specification");
		 pnlFrequency.setBorder( BorderFactory.createTitledBorder( "Frequency Specification" ) ) ;
		 cmbCenter.addItemListener(new ItemListener() {
		 	public void itemStateChanged(ItemEvent event) {
		 		if (event.getItem().toString().startsWith(cmbCenter.getItemAt(0).toString()))
		 		{
		 			txtBandwidth.setText(BANDWIDTH);
		 			numBAndwidth.setModel(new SpinnerNumberModel(50,1,150,1));
		 			onConnectionChange(true);
		 		}
		 		else
		 		{
		 			txtBandwidth.setText(LOW_FREQH);
		 			numBAndwidth.setModel(new SpinnerNumberModel(1000,950,2150,1));
		 			onConnectionChange(false);
		 		}
		 	}
		 });
		 cmbCenter.setModel(new DefaultComboBoxModel(new String[] {"Center [MHz]", "High    [MHz]"}));
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
		 pnlFileSize.setBorder(BorderFactory.createTitledBorder( "File Size" ));
		 pnlFileSize.setBounds(10, 258, 459, 106);
		 
		 f.getContentPane().add(pnlFileSize);
		 cmbFileSize.setModel(new DefaultComboBoxModel(new String[] {"Time [sec]", "Samples [x10^6]", "File Size [GB]"}));
		 cmbFileSize.setFont(new Font("Arial", Font.PLAIN, 14));
		 cmbFileSize.setBackground(Color.WHITE);
		 cmbFileSize.setBounds(10, 31, 185, 23);
		 
		 pnlFileSize.add(cmbFileSize);
		 numFileSize.setForeground(new Color(240, 240, 240));
		 numFileSize.setBackground(SystemColor.text);
		 numFileSize.setFont(new Font("Arial", Font.PLAIN, 14));
		 numFileSize.setBounds(303, 32, 143, 23);
		 
		 pnlFileSize.add(numFileSize);
		 btnSpecifyFile.addActionListener(new ActionListener() {
		 	public void actionPerformed(ActionEvent event) 
		 	{
		 		String s = "";
		 		int returnVal;
		 		JFileChooser jfc = new JFileChooser();
		 		if (rdbtnRecord.isSelected())
		 			returnVal = jfc.showSaveDialog(null);
		 		else
		 			returnVal = jfc.showOpenDialog(null);
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
		 txtFileName.setEditable(false);
		 txtFileName.setColumns(10);
		 txtFileName.setBounds(161, 65, 285, 23);
		 
		 pnlFileSize.add(txtFileName);
		 
		 Red_icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/jrecorder/red-led.png"))); 
		 Green_icon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/jrecorder/green-led.png")));
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
	     //f.getContentPane().add(pnlLed);
	     lblLed = new JLabel(); 
	     lblLed.setBounds(404, 13, 25, 25);
	     f.getContentPane().add(lblLed);
	     lblLed.setIcon(Red_icon);
	 }
	
	 private void Init() 
	 {
		 try {
				param = new Parameters("recorder.ini");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				return ;
				//e.printStackTrace();
			} 
			
	    txtIP.setText(param.Get("ettus_address", "127.0.0.1"));
			
		 
		 
		 connectivityThread = new CheckConnectivity(this, "127.0.0.1");
	     Thread thread = new Thread(connectivityThread);
	     thread.start();
	 }
	 
	 private void Stop()
	 {
		 connectivityThread.Stop();
	 }
	 
	 public void onConnectionChange(final Boolean status)
	 {
		 if (!SwingUtilities.isEventDispatchThread()) 
		 {
		     SwingUtilities.invokeLater(new Runnable() 
		     {
		       @Override
		       public void run() {
		    	   onConnectionChange(status);
		       }
		     });
		     return;
		 }
		 //Now edit your gui objects
		 if (status)
		 {
			 lblLed.setIcon(Green_icon);
			 _connectionStatus = true;
		 }
		 else
		 {
			 lblLed.setIcon(Red_icon);
			 _connectionStatus = false;
		 }
	 }
	 
	 public void UpdateStatus (final String status)
	 {
		 if (!SwingUtilities.isEventDispatchThread()) 
		 {
		     SwingUtilities.invokeLater(new Runnable() 
		     {
		       @Override
		       public void run() {
		    	   UpdateStatus(status);
		       }
		     });
		     return;
		 }
		 //Now edit your gui objects
		 txtStatus.setText(status);
	 }
	 
	 private void ShowSpectrumWindow()
	 {
		if (!_connectionStatus)
 		{
 			JOptionPane.showMessageDialog(f, "No connection with the server. Please check wiring and IP address","Spectrum" , JOptionPane.ERROR_MESSAGE);
 			return;
 		}
		
		if (cmbCenter.getSelectedIndex() != 0) // Center
		{
			JOptionPane.showMessageDialog(f, "Spectrum Measurement needs Central Frequency Specified","Spectrum" , JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		double CenterFrequncy = (double)((Integer)numCenter.getValue()) * 1e6;
		
		
		double Gain = -1; // Automatic;
		if (cmbAgc.getSelectedIndex() == 1) // Manual
		{
			Gain = (double) numAgc.getValue();
		}
		
		String SpectrumExe = param.Get("SpectrumExec", "./Spectrum");
		
		if (!new File(SpectrumExe).exists())
		{
			JOptionPane.showMessageDialog(f, "Spectrum exec not found. Please fix the configuration file","Spectrum" , JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		String SpectrumBin = param.Get("SpectrumBin", "Spectrum.dat");
	 	SpectrumWindow sw = new SpectrumWindow(SpectrumExe);
	 	double Rate = Double.parseDouble(param.Get("Rate", "100e6"));
	 	
	 	sw.GetMessurment(CenterFrequncy, Rate, Gain, SpectrumBin);
	 	try 
	 	{
			sw.Show(SpectrumBin);
			UpdateStatus("Showing spectrum ....");
		} 
	 	catch (FileNotFoundException e) 
	 	{
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(f, "Error while building spectrum.","Spectrum" , JOptionPane.ERROR_MESSAGE);
			UpdateStatus("Error while Showing spectrum");
		}
	 }
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.f.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
