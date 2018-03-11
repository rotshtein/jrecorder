package jrecorder;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;
import javax.swing.JScrollPane;

public class SpectrumWindow
{
	final static Logger	logger	= Logger.getLogger("SpectrumWindow");
	String				filename;
	String				spectrun_exe;
	Process				spectrumProcess;
	ProcMon				procMon;

	public SpectrumWindow(String SpectrunExe)
	{
		spectrun_exe = SpectrunExe;
	}

	/**
	 * @throws FileNotFoundException
	 * @wbp.parser.entryPoint
	 */
	public void Show(String Filename) throws FileNotFoundException
	{
		final NumberAxis domainAxis = new NumberAxis("[MHz]");
		domainAxis.setRange(0.00, 1024 * 32);

		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series1 = new XYSeries("RF");
		try
		{
			File file = new File(Filename);

			int i = 0;
			while (!file.exists())
			{
				Thread.sleep(100);
				if (i++ > 20)
				{
					series1.add(1, 2);
					series1.add(2, 2);
					return;
				}
			}
			
			InputStream insputStream = new FileInputStream(file);
			DataInputStream dIn = new DataInputStream(insputStream);
			float x, y;

			while (dIn.available() > 0)
			{

				x = dIn.readFloat();
				y = dIn.readFloat();
				series1.add(x, y);
			}
			dIn.close();
			insputStream.close();

			/*
			 * for (int i = 0; i < (1024*32); i++) { series1.add(i, i); }
			 */
		} catch (Exception e)
		{
			logger.error("Failed getting spectrum info", e);
			return;
		}
		dataset.addSeries(series1);

		JFreeChart chart = ChartFactory.createXYLineChart("Spectrum", "Frequncy [MHz]", "db", dataset);
		final XYPlot plot = chart.getXYPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setRange(0, 32000);
		// axis.setFixedAutoRange(60000.0);

		final JFrame frame = new JFrame("Specturm");
		frame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				frame.setVisible(false);
			}
		});
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		ChartPanel label = new ChartPanel(chart);
		frame.getContentPane().add(label);
		label.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JScrollPane scrollPane = new JScrollPane();
		label.add(scrollPane);
		// Suppose I add combo boxes and buttons here later

		frame.pack();
		frame.setVisible(true);

		try
		{
			OutputStream out = new FileOutputStream("c:\\d\\graph.png");
			ChartUtilities.writeChartAsPNG(out, chart, frame.getWidth(), frame.getHeight());

		} catch (IOException ex)
		{
			logger.error("Failed saving spectrum picture", ex);
		}
	}

	public ProcMon GetMessurment(double f0, double Rate, double Gain, String Filename)
	{

		if (new File(spectrun_exe).exists())
		{
			try
			{
				String vars[] =
				{ spectrun_exe, "--mode spec", "--freq " + f0, " --rate " + Rate, " --file " + Filename };
				spectrumProcess = Runtime.getRuntime().exec(vars, null);

				procMon = new ProcMon(spectrumProcess, "Show Spectrum");
				Thread t = new Thread(procMon);
				t.start();

			} catch (Exception ex)
			{
				logger.error("Failed to start spectrum process", ex);
				return null;
			}
			return procMon;
		}
		return null;
	}

}
