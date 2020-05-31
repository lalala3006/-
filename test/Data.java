package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Data {
	
	//声明Connection对象
	public static Connection con;	
	
	public Data(){
		
		/* 连接数据库 */
		
		//驱动程序名
		String driver="com.mysql.jdbc.Driver";
		String url="jdbc:mysql://localhost:3306/areacode";
		String username="root";
		String password="123456";
		
		try {
			//加载驱动程序
			Class.forName(driver);
			//首先，getConnection（）方法，连接MySQL数据库
			con = DriverManager.getConnection(url,username,password);
			if(!con.isClosed())
				System.out.println("恭喜你成功连接数据库！");
		} catch(ClassNotFoundException e) {
			//数据库驱动类异常处理
			System.out.println("Sorry,can't find the Driver!");
			e.printStackTrace();
		} catch(SQLException e) {
			//数据库连接失败异常处理
			e.printStackTrace();
		}				
	}
	
	public static ResultSet getData (int num) {		
		//查询
		try {			
			//创建statement类对象，用来执行SQL语句
			Statement statement=con.createStatement();
			//从DB中选择num条数据
//			String sql="select zone,areazone from areazone limit " + num;
			String sql="select areazone from areazone limit " + num;
//			String sql="select areazone from areazone";
			//设置Resultset类，用来存放获取的结果集
			ResultSet rs=statement.executeQuery(sql);		
			return rs;
			
			// ？没有关闭rs？怎么关？
			// 一般不会直接返回一个ResultSet？
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void finalize(){
		try {
			con.close();
			System.out.println("数据库连接已断开！");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}





