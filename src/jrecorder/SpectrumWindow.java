package jrecorder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;

public class SpectrumWindow 
{
	String filename;
	public SpectrumWindow(String BinFilename)
	{
		filename = BinFilename;
	}
	
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public void Show()
	{
		 final NumberAxis domainAxis = new NumberAxis("[MHz]");
	        domainAxis.setRange(0.00,1024*32);
	        
	        XYSeriesCollection dataset = new XYSeriesCollection();
	        XYSeries series1 = new XYSeries("");
	        for (int i = 0; i < (1024*32); i++)
	        {
	        	series1.add(i, i);
	        }
	        dataset.addSeries(series1);
	        
	        JFreeChart chart = ChartFactory.createXYLineChart(
	            "Spectrum",
	            "Frequncy [MHz]",
	            "db",
	            dataset
	        );
	        final XYPlot plot = chart.getXYPlot();
	        ValueAxis axis = plot.getDomainAxis();
	        axis.setRange(0,32000);
	        //axis.setFixedAutoRange(60000.0);
	        
	       
	        
	        JFrame frame = new JFrame("Specturm");
	        frame.addWindowListener(new WindowAdapter() {
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
	        //Suppose I add combo boxes and buttons here later

	        frame.pack();
	        frame.setVisible(true);
	        
	        try 
	        {
	            OutputStream out = new FileOutputStream("c:\\d\\graph.png");
	            ChartUtilities.writeChartAsPNG(out,
	            		chart,
	            		frame.getWidth(),
	            		frame.getHeight());

	        } catch (IOException ex) 
	        {
	            
	        }
	}
}
