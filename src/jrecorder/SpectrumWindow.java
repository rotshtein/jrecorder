package jrecorder;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.HistogramDataset;
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
	double centerFrequency;

	public SpectrumWindow(double CenterFrequency)
	{
		centerFrequency = CenterFrequency;
	}

	/**
	 * @throws FileNotFoundException
	 * @wbp.parser.entryPoint
	 */
	//public void Show(String Filename) throws FileNotFoundException
	public void Show(byte [] rawdata)// throws FileNotFoundException
	{
		final NumberAxis domainAxis = new NumberAxis("[MHz]");
		domainAxis.setRange(0.00, 1024 * 32);

		XYSeriesCollection dataset = new XYSeriesCollection();
		//DefaultCategoryDataset dataset = new DefaultCategoryDataset( );
		XYSeries series1 = new XYSeries("RF");
		
		
		
		try
		{
			ByteArrayInputStream  insputStream =  new ByteArrayInputStream(rawdata);
			DataInputStream dIn = new DataInputStream(insputStream);
			double y;
			double x = (centerFrequency/1e6) - (0xFFFF /2);
			
			while (dIn.available() > 0)
			{
				y = dIn.readFloat();
				//y = dIn.readFloat();
				series1.add(x++, y);
				//dataset.addValue(x, "RF", Float.toString(y));
			}
			dIn.close();
			insputStream.close();
			/*
			 * for (int i = 0; i < (1024*32); i++) { series1.add(i, i); }
			 */
		}
		catch (Exception e)
		{
			logger.error("Failed getting spectrum info", e);
			return;
		}
		dataset.addSeries(series1);
		
		JFreeChart chart = ChartFactory.createXYLineChart("Spectrum", "Frequncy [MHz]", "db", dataset);
		//JFreeChart chart = ChartFactory.createLineChart("Spectrum", "Frequncy [MHz]", "db", dataset,PlotOrientation.VERTICAL, true,true,false);
		//final XYPlot plot = chart.getXYPlot();
		//ValueAxis axis = plot.getDomainAxis();
		//axis.setRange(0, 32000);
		

		//axis.setFixedAutoRange(60000.0);

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
/*
		try
		{
			OutputStream out = new FileOutputStream("c:\\d\\graph.png");
			ChartUtilities.writeChartAsPNG(out, chart, frame.getWidth(), frame.getHeight());

		}
		catch (IOException ex)
		{
			logger.error("Failed saving spectrum picture", ex);
		}*/
	}

	public void Show1(byte [] rawdata)// throws FileNotFoundException
	{
		final NumberAxis domainAxis = new NumberAxis("[MHz]");
		domainAxis.setRange(0.00, 1024 * 32);

		//XYSeriesCollection dataset = new XYSeriesCollection();
		
		//XYSeries series1 = new XYSeries("RF");
		
		HistogramDataset dataSet = new HistogramDataset();
		
		try
		{
			ByteArrayInputStream  insputStream =  new ByteArrayInputStream(rawdata);
			DataInputStream dIn = new DataInputStream(insputStream);
			double [] list = new double[dIn.available()/8];
			double i = 0;
			while (dIn.available() > 0)
			{
				list[(int)i] = dIn.readDouble();
			}
			dIn.close();
			insputStream.close();
			//Comparable key, double[] values, int bins,    double minimum, double maximum)
			dataSet.addSeries("Hist", list, list.length,0,list[(list.length-1)]);
			/*
			 * for (int i = 0; i < (1024*32); i++) { series1.add(i, i); }
			 */
		}
		catch (Exception e)
		{
			logger.error("Failed getting spectrum info", e);
			return;
		}
		//dataset.addSeries(series1);
		
		JFreeChart chart = ChartFactory.createHistogram("", null, null, dataSet,  PlotOrientation.VERTICAL, 
				true, true, false);
		final ChartFrame frame = new ChartFrame("Spatial Data Quality", chart);
/*
		JFreeChart chart = ChartFactory.createXYLineChart("Spectrum", "Frequncy [MHz]", "db", dataset);
		final XYPlot plot = chart.getXYPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setRange(0, 32000);
		
		JFreeChart chart = ChartFactory.createXYLineChart("Spectrum", "Frequncy [MHz]", "db", dataset);
		final XYPlot plot = chart.getXYPlot();
		ValueAxis axis = plot.getDomainAxis();
		axis.setRange(0, 32000);*/
		// axis.setFixedAutoRange(60000.0);

		//final JFrame frame = new JFrame("Specturm");
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

			}
			catch (Exception ex)
			{
				logger.error("Failed to start spectrum process", ex);
				return null;
			}
			return procMon;
		}
		return null;
	}

}
