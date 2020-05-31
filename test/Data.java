package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Data {
	
	//����Connection����
	public static Connection con;	
	
	public Data(){
		
		/* �������ݿ� */
		
		//����������
		String driver="com.mysql.jdbc.Driver";
		String url="jdbc:mysql://localhost:3306/areacode";
		String username="root";
		String password="123456";
		
		try {
			//������������
			Class.forName(driver);
			//���ȣ�getConnection��������������MySQL���ݿ�
			con = DriverManager.getConnection(url,username,password);
			if(!con.isClosed())
				System.out.println("��ϲ��ɹ��������ݿ⣡");
		} catch(ClassNotFoundException e) {
			//���ݿ��������쳣����
			System.out.println("Sorry,can't find the Driver!");
			e.printStackTrace();
		} catch(SQLException e) {
			//���ݿ�����ʧ���쳣����
			e.printStackTrace();
		}				
	}
	
	public static ResultSet getData (int num) {		
		//��ѯ
		try {			
			//����statement���������ִ��SQL���
			Statement statement=con.createStatement();
			//��DB��ѡ��num������
//			String sql="select zone,areazone from areazone limit " + num;
			String sql="select areazone from areazone limit " + num;
//			String sql="select areazone from areazone";
			//����Resultset�࣬������Ż�ȡ�Ľ����
			ResultSet rs=statement.executeQuery(sql);		
			return rs;
			
			// ��û�йر�rs����ô�أ�
			// һ�㲻��ֱ�ӷ���һ��ResultSet��
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void finalize(){
		try {
			con.close();
			System.out.println("���ݿ������ѶϿ���");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

}





