package test;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.bouncycastle.jce.provider.BouncyCastleProvider; 

import it.unisa.dia.gas.jpbc.*;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import javafx.scene.chart.BarChart;

import java.lang.reflect.Proxy;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.FileOutputStream;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;

 
public class Main {
	
	public static Data da = new Data();
	public static List<Integer> size = new ArrayList<Integer>();
	
	public static void main(String[] args) {
////		size.add(5);
//		size.add(10);
//		size.add(50);
//		size.add(100);
//		size.add(200);
//		size.add(300);
//		size.add(400);
//		size.add(500);
//		size.add(1000);
		
		for(int i=1;i<10;i++)
			size.add(i*100);
		
		MyClpkc myCl = new MyClpkc();
		MyClpkc zhangCl = new MyClpkc();
		MyClpkc liCl = new MyClpkc();
		
		for(Integer i:size){
			//从数据库中获取明文数据
			int count = i;
			ResultSet rs1 = da.getData(count);
			ResultSet rs2 = da.getData(count);
			ResultSet rs3 = da.getData(count);
			myCl.myClpkc(rs1, count);
			zhangCl.zhangClpkc(rs2, count);
			liCl.liClpkc(rs3, count);
		}		
		
		Iterator iter1Mine = myCl.signResult.iterator();
		Iterator iter1Zhang = zhangCl.signResult.iterator();	
		Iterator iter1Li = liCl.signResult.iterator();
	    makeCharts(iter1Mine, iter1Zhang, iter1Li, "签名耗时比较");
		Iterator iter2Mine = myCl.verifyResult.iterator();
		Iterator iter2Zhang = zhangCl.verifyResult.iterator();	
		Iterator iter2Li = liCl.verifyResult.iterator();
	    makeCharts(iter2Mine, iter2Zhang, iter2Li, "验证耗时比较");
	    
	}
		
	
	public static void makeCharts(Iterator iter1, Iterator iter2, Iterator iter3, String name){
	    
		//设置折线图样式
		StandardChartTheme mChartTheme = new StandardChartTheme("CN");
	    mChartTheme.setLargeFont(new Font("黑体", Font.BOLD, 20));
	    mChartTheme.setExtraLargeFont(new Font("宋体", Font.PLAIN, 15));
	    mChartTheme.setRegularFont(new Font("宋体", Font.PLAIN, 15));
	    ChartFactory.setChartTheme(mChartTheme);
	        
	    
	    //设置数据集
//		DefaultCategoryDataset mDataset = new DefaultCategoryDataset();
		DefaultCategoryDataset mmDataset = new DefaultCategoryDataset();

		for(Integer i:size){
			if(iter1.hasNext())	mmDataset.addValue((long)iter1.next(), "新方案", i.toString());
			if(iter2.hasNext())	mmDataset.addValue((long)iter2.next(), "Zhang方案", i.toString());
			if(iter3.hasNext())	mmDataset.addValue((long)iter3.next(), "Li方案", i.toString());	
		}	

		
			
		//画图
	    String yName="耗时(ms)";
	    if(name == "通过率比较") yName="通过率";
	    JFreeChart mChart = ChartFactory.createLineChart(
	        name,//图名字
	        "数据量",//横坐标
	        yName,//纵坐标
	        mmDataset,//数据集
	        PlotOrientation.VERTICAL,
	        true, // 显示图例
	        true, // 采用标准生成器 
	        false);// 是否生成超链接
	    
	    CategoryPlot mPlot = (CategoryPlot)mChart.getPlot();
	    mPlot.setBackgroundPaint(Color.WHITE);
	    mPlot.setRangeGridlinePaint(new Color(190,190,190));//背景底部横虚线
	    mPlot.setOutlinePaint(Color.WHITE);//边界线
	    LineAndShapeRenderer renderer = (LineAndShapeRenderer) mPlot.getRenderer();	// 能够得到renderer对象，就可以使用此方法设置颜色
	    renderer.setSeriesPaint(0, new Color(141,238,238)); // 设置第一条折线 的颜色，以此类推。
	    renderer.setSeriesPaint(1, new Color(192,255,62));
	    renderer.setSeriesPaint(2, new Color(255,246,143));
	    renderer.setStroke(new BasicStroke(1.5F));	//设置线条加粗
	    renderer.setBaseShapesVisible(true);	//显示拐点 
	    renderer.setSeriesOutlineStroke(0, new BasicStroke(2.5F));
	    renderer.setSeriesOutlineStroke(1, new BasicStroke(2.5F));
	    renderer.setSeriesOutlineStroke(2, new BasicStroke(2.5F));
	    CategoryAxis categoryAxis = mPlot.getDomainAxis();	//X轴
	    categoryAxis.setAxisLinePaint(new Color(230,230,250));
	    NumberAxis numberAxis = (NumberAxis) mPlot.getRangeAxis();
	    numberAxis.setAxisLinePaint(new Color(230,230,250));
	    
	    
	    
	    
	    ChartFrame mChartFrame = new ChartFrame("折线统计图", mChart);
	    mChartFrame.pack();
	    mChartFrame.setVisible(true);
		
	    
		//输出 jpg保存
	    FileOutputStream fos_jpg = null;
	    try {
	    	fos_jpg=new FileOutputStream("E:\\design\\论文2.0\\" + name + ".jpg");
	    	ChartUtilities.writeChartAsJPEG(fos_jpg,0.7f,mChart,640,480,null);
	    	fos_jpg.close();
	    } catch (Exception e) {
	    }		
	}
	
	
	
	
	
}
