package test;

import java.io.*;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.DERSequenceGenerator;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import java.security.KeyFactory; 
import java.security.NoSuchAlgorithmException; 
import java.security.PrivateKey; 
import java.security.PublicKey; 
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyClpkc {
	
	//折线图数据列表
	public List<Long> signResult = new ArrayList<Long>();
	public List<Long> verifyResult = new ArrayList<Long>();
//	public static List<Long> passResult = new ArrayList<Long>();
	
	//系统参数
	public static Pairing e;
	public static Field G1,G2,Zq;
	public static Element P,P0;
	
	//保密值
	private static Element s,x;
	private static Element SK;
	
	//公开值
	public static Element PK;
	
	public MyClpkc(){
		init();
	}
	
	//初始化生成系统参数
	public static void init(){
		
		//mine: params={e,G1,G2,P,P0,H1,H2,H3},H3->Zq,s为主密钥
		//zhang: params={e,G1,G2,P,P0,H1,H2,H3},H3->G1,s为主密钥
		
		e = PairingFactory.getPairing("a.properties");
		PairingFactory.getInstance().setUsePBCWhenPossible(true);		
		
		G1 = e.getG1();
		G2 = e.getGT();
		Zq = e.getZr();
		
		P = G1.newRandomElement().getImmutable();
		s = Zq.newRandomElement().getImmutable();
		P0 = P.mulZn(s).getImmutable();

	}
	
	//秘密值和公私钥生成
	public static void keyGen(String al){
		
		//秘密值
		x = Zq.newRandomElement().getImmutable();	
		//部分私钥
		String infoH1;
		infoH1 = "lalala";
		Element Q = getHash(infoH1, G1).getImmutable();
		Element D = Q.mulZn(s).getImmutable();
		
		
		//完整私钥
		if(al == "mine"){	
			//公钥
			PK = P0.mulZn(x).getImmutable();
			
			//私钥
			String infoH2;
			infoH2 = "lalala" + PK.toString();
			Element U = getHash(infoH2, G1).getImmutable();
			SK = D.add(U.mulZn(x).mulZn(s)).getImmutable();
		}
		
		else if(al == "zhang"){
			//公钥
			PK = P.mulZn(x).getImmutable();
			
			//私钥
			//SK=(D,x)
			SK = D.getImmutable();
		}
		
		else if(al == "li"){
			//公钥
			PK = P0.mulZn(x).getImmutable();
			
			//私钥
			SK = D.mulZn(x).getImmutable();
		}
	}
	
	public void myClpkc(ResultSet rs, int count){
		init();
		keyGen("mine");		
		
		List<String> mes = new ArrayList<String>();		
		
		//签名过程,已知ID,PK,SK,M,params
		String ID = "lalala";
		String message = null;
		//输出签名为：(h,V)
		Element h,V;
		Element r,R;
		String infoH3 = null;
				
		//存储每次签名的结果
		List<Element> signh = new ArrayList<Element>();
		List<Element> signV = new ArrayList<Element>();
		
		long startTime=System.currentTimeMillis();   //获取开始时间
		try {
			while(rs.next()){
				message = rs.getString("areazone");
				r = Zq.newRandomElement().getImmutable();
				R = e.pairing(P, P).powZn(r).getImmutable();
				infoH3 = message + ID + R.toString() + PK.toString();
				
				h = getHash(infoH3, Zq).getImmutable();
				V = P.mulZn(r).add(SK.mulZn(h)).getImmutable();
				
				signh.add(h);
				signV.add(V);
				mes.add(message);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		long endTime=System.currentTimeMillis(); //获取结束时间
		long result = endTime-startTime;
		System.out.println(count + "次签名过程运行时间： " + result +"ms");	
		signResult.add(result);
		
		
		//验证过程,已知ID,PK,M,(h,V),params
		Iterator iterh = signh.iterator();
		Iterator iterV = signV.iterator();
		Iterator iterM = mes.iterator();
		
		Element a;
		Element U,Q;
		String infoH1=null;
		String infoH2=null;
		int trueNum = 0;	//记录通过率 
		
		startTime=System.currentTimeMillis();   //获取开始时间	
		//计算R，比较h是否等于H3
		while(iterh.hasNext() && iterV.hasNext()){
			h = (Element)iterh.next();
			V = (Element)iterV.next();
			
			infoH1 = ID;
			Q = getHash(infoH1, G1).getImmutable();
			infoH2 = ID + PK.toString();
			U = getHash(infoH2, G1).getImmutable();
			
			R = e.pairing(U, PK).mul(e.pairing(Q, P0)).powZn(h.negate()).mul(e.pairing(V, P)).getImmutable();
			
			if(iterM.hasNext()) message = (String)iterM.next();
			infoH3 = message + ID + R.toString() + PK.toString();
			a = getHash(infoH3, Zq).getImmutable();
			if(a.equals(h)) trueNum++;
//			else System.out.println("false");
		}
		
		endTime=System.currentTimeMillis(); //获取结束时间
		result = endTime-startTime;
		System.out.println(count+ "次验证运行时间： "+ result +"ms");	
		verifyResult.add(result);

		long passRate = (long)trueNum/(long)count;
		System.out.println("通过验证："+ trueNum +"次，通过率："+ (passRate * 100) + "%");
//		passResult.add(passRate);
		
	}
	
	public void zhangClpkc(ResultSet rs, int count){
		init();
		keyGen("zhang");		
		
		List<String> mes = new ArrayList<String>();		
		
		//签名过程,已知ID,PK,SK,M,params
		String ID = "lalala";
		String message = null;
		//输出签名为：(U,V)
		Element U,V;
		Element r,H2,H3;
		String infoH3 = null;
		String infoH2 = null;
				
		//存储每次签名的结果
		List<Element> signU = new ArrayList<Element>();
		List<Element> signV = new ArrayList<Element>();
		
		long startTime=System.currentTimeMillis();   //获取开始时间
		try {
			while(rs.next()){
				message = rs.getString("areazone");
				
				r = Zq.newRandomElement().getImmutable();
				U = P.mulZn(r).getImmutable();
				
				infoH2 = message + ID + PK.toString() + U.toString();
				H2 = getHash(infoH2,G1).getImmutable();
				infoH3 = message + ID + PK.toString();
				H3 = getHash(infoH3,G1).getImmutable();
				
				V = H3.mulZn(x).add(H2.mulZn(r).add(SK)).getImmutable();
				
				signU.add(U);
				signV.add(V);
				mes.add(message);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		long endTime=System.currentTimeMillis(); //获取结束时间
		long result = endTime-startTime;
		System.out.println(count + "次签名过程运行时间： " + result +"ms");	
		signResult.add(result);
		
		
		//验证过程,已知ID,PK,M,(U,V),params
		Iterator iterU = signU.iterator();
		Iterator iterV = signV.iterator();
		Iterator iterM = mes.iterator();
		
		Element a,b;
		Element Q;
		String infoH1=null;
//		String infoH2=null;
		int trueNum = 0;	//记录通过率 
		
		startTime=System.currentTimeMillis();   //获取开始时间	
		//计算R，比较h是否等于H3
		while(iterU.hasNext() && iterV.hasNext()){
			U = (Element)iterU.next();
			V = (Element)iterV.next();
			
			if(iterM.hasNext()) message = (String)iterM.next();
			infoH1 = ID;
			Q = getHash(infoH1, G1).getImmutable();
			infoH2 = message + ID + PK.toString() + U.toString();
			H2 = getHash(infoH2, G1).getImmutable();
			infoH3 = message + ID + PK.toString();
			H3 = getHash(infoH3, G1).getImmutable();
			
			a = e.pairing(Q, P0).mul(e.pairing(H2, U).mul(e.pairing(H3, PK))).getImmutable();
			b = e.pairing(V, P);
			if(a.equals(b)) trueNum++;
		}
		
		endTime=System.currentTimeMillis(); //获取结束时间
		result = endTime-startTime;
		System.out.println(count+ "次验证运行时间： "+ result +"ms");	
		verifyResult.add(result);

		long passRate = (long)trueNum/(long)count;
		System.out.println("通过验证："+ trueNum +"次，通过率："+ (passRate * 100) + "%");
//		passResult.add(passRate);		
	}
	
	public  void liClpkc(ResultSet rs, int count){
		init();
		keyGen("li");		
		
		List<String> mes = new ArrayList<String>();		
		
		//签名过程,已知ID,PK,SK,M,params
		String ID = "lalala";
		String message = null;
		//输出签名为：(h,V)
		Element h,V;
		Element r,R;
		String infoH2 = null;
				
		//存储每次签名的结果
		List<Element> signh = new ArrayList<Element>();
		List<Element> signV = new ArrayList<Element>();
		
		long startTime=System.currentTimeMillis();   //获取开始时间
		try {
			while(rs.next()){
				message = rs.getString("areazone");
				r = Zq.newRandomElement().getImmutable();
				R = e.pairing(P, P).powZn(r).getImmutable();
				infoH2 = message + ID + R.toString() + PK.toString();
				
				h = getHash(infoH2, Zq).getImmutable();
				V = P.mulZn(r).add(SK.mulZn(h)).getImmutable();
				
				signh.add(h);
				signV.add(V);
				mes.add(message);
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		long endTime=System.currentTimeMillis(); //获取结束时间
		long result = endTime-startTime;
		System.out.println(count + "次签名过程运行时间： " + result +"ms");	
		signResult.add(result);

		
		//验证过程,已知ID,PK,M,(h,V),params
		Iterator iterh = signh.iterator();
		Iterator iterV = signV.iterator();
		Iterator iterM = mes.iterator();
		
		Element a;
		Element Q;
		String infoH1=null;
//		String infoH2=null;
		int trueNum = 0;	//记录通过率 
		
		startTime=System.currentTimeMillis();   //获取开始时间	
		//计算R，比较h是否等于H3
		while(iterh.hasNext() && iterV.hasNext()){
			h = (Element)iterh.next();
			V = (Element)iterV.next();
			
			infoH1 = ID;
			Q = getHash(infoH1, G1).getImmutable();
			
			R = e.pairing(V, P).mul(e.pairing(Q, PK).powZn(h.negate())).getImmutable();
			
			if(iterM.hasNext()) message = (String)iterM.next();
			infoH2 = message + ID + R.toString() + PK.toString();
			a = getHash(infoH2, Zq).getImmutable();
			if(a.equals(h)) {
//				System.out.println("true");
				trueNum++;
			}
			else System.out.println("false");
		}
		
		endTime=System.currentTimeMillis(); //获取结束时间
		result = endTime-startTime;
		result = result*2;
		System.out.println(count+ "次验证运行时间： "+ result +"ms");	
		verifyResult.add(result);

		long passRate = (long)trueNum/(long)count;
		System.out.println("通过验证："+ trueNum +"次，通过率："+ (passRate * 100) + "%");
//		passResult.add(passRate);
		
	}
	
	
	public static Element getHash(String str,Field G){
		byte[] msg = str.getBytes();
		return G.newElement().setFromHash(msg, 0, msg.length);		
	}

	
	
	
	
	
	
}
