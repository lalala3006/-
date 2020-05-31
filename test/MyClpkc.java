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
	
	//����ͼ�����б�
	public List<Long> signResult = new ArrayList<Long>();
	public List<Long> verifyResult = new ArrayList<Long>();
//	public static List<Long> passResult = new ArrayList<Long>();
	
	//ϵͳ����
	public static Pairing e;
	public static Field G1,G2,Zq;
	public static Element P,P0;
	
	//����ֵ
	private static Element s,x;
	private static Element SK;
	
	//����ֵ
	public static Element PK;
	
	public MyClpkc(){
		init();
	}
	
	//��ʼ������ϵͳ����
	public static void init(){
		
		//mine: params={e,G1,G2,P,P0,H1,H2,H3},H3->Zq,sΪ����Կ
		//zhang: params={e,G1,G2,P,P0,H1,H2,H3},H3->G1,sΪ����Կ
		
		e = PairingFactory.getPairing("a.properties");
		PairingFactory.getInstance().setUsePBCWhenPossible(true);		
		
		G1 = e.getG1();
		G2 = e.getGT();
		Zq = e.getZr();
		
		P = G1.newRandomElement().getImmutable();
		s = Zq.newRandomElement().getImmutable();
		P0 = P.mulZn(s).getImmutable();

	}
	
	//����ֵ�͹�˽Կ����
	public static void keyGen(String al){
		
		//����ֵ
		x = Zq.newRandomElement().getImmutable();	
		//����˽Կ
		String infoH1;
		infoH1 = "lalala";
		Element Q = getHash(infoH1, G1).getImmutable();
		Element D = Q.mulZn(s).getImmutable();
		
		
		//����˽Կ
		if(al == "mine"){	
			//��Կ
			PK = P0.mulZn(x).getImmutable();
			
			//˽Կ
			String infoH2;
			infoH2 = "lalala" + PK.toString();
			Element U = getHash(infoH2, G1).getImmutable();
			SK = D.add(U.mulZn(x).mulZn(s)).getImmutable();
		}
		
		else if(al == "zhang"){
			//��Կ
			PK = P.mulZn(x).getImmutable();
			
			//˽Կ
			//SK=(D,x)
			SK = D.getImmutable();
		}
		
		else if(al == "li"){
			//��Կ
			PK = P0.mulZn(x).getImmutable();
			
			//˽Կ
			SK = D.mulZn(x).getImmutable();
		}
	}
	
	public void myClpkc(ResultSet rs, int count){
		init();
		keyGen("mine");		
		
		List<String> mes = new ArrayList<String>();		
		
		//ǩ������,��֪ID,PK,SK,M,params
		String ID = "lalala";
		String message = null;
		//���ǩ��Ϊ��(h,V)
		Element h,V;
		Element r,R;
		String infoH3 = null;
				
		//�洢ÿ��ǩ���Ľ��
		List<Element> signh = new ArrayList<Element>();
		List<Element> signV = new ArrayList<Element>();
		
		long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
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
		
		long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		long result = endTime-startTime;
		System.out.println(count + "��ǩ����������ʱ�䣺 " + result +"ms");	
		signResult.add(result);
		
		
		//��֤����,��֪ID,PK,M,(h,V),params
		Iterator iterh = signh.iterator();
		Iterator iterV = signV.iterator();
		Iterator iterM = mes.iterator();
		
		Element a;
		Element U,Q;
		String infoH1=null;
		String infoH2=null;
		int trueNum = 0;	//��¼ͨ���� 
		
		startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��	
		//����R���Ƚ�h�Ƿ����H3
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
		
		endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		result = endTime-startTime;
		System.out.println(count+ "����֤����ʱ�䣺 "+ result +"ms");	
		verifyResult.add(result);

		long passRate = (long)trueNum/(long)count;
		System.out.println("ͨ����֤��"+ trueNum +"�Σ�ͨ���ʣ�"+ (passRate * 100) + "%");
//		passResult.add(passRate);
		
	}
	
	public void zhangClpkc(ResultSet rs, int count){
		init();
		keyGen("zhang");		
		
		List<String> mes = new ArrayList<String>();		
		
		//ǩ������,��֪ID,PK,SK,M,params
		String ID = "lalala";
		String message = null;
		//���ǩ��Ϊ��(U,V)
		Element U,V;
		Element r,H2,H3;
		String infoH3 = null;
		String infoH2 = null;
				
		//�洢ÿ��ǩ���Ľ��
		List<Element> signU = new ArrayList<Element>();
		List<Element> signV = new ArrayList<Element>();
		
		long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
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
		
		long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		long result = endTime-startTime;
		System.out.println(count + "��ǩ����������ʱ�䣺 " + result +"ms");	
		signResult.add(result);
		
		
		//��֤����,��֪ID,PK,M,(U,V),params
		Iterator iterU = signU.iterator();
		Iterator iterV = signV.iterator();
		Iterator iterM = mes.iterator();
		
		Element a,b;
		Element Q;
		String infoH1=null;
//		String infoH2=null;
		int trueNum = 0;	//��¼ͨ���� 
		
		startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��	
		//����R���Ƚ�h�Ƿ����H3
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
		
		endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		result = endTime-startTime;
		System.out.println(count+ "����֤����ʱ�䣺 "+ result +"ms");	
		verifyResult.add(result);

		long passRate = (long)trueNum/(long)count;
		System.out.println("ͨ����֤��"+ trueNum +"�Σ�ͨ���ʣ�"+ (passRate * 100) + "%");
//		passResult.add(passRate);		
	}
	
	public  void liClpkc(ResultSet rs, int count){
		init();
		keyGen("li");		
		
		List<String> mes = new ArrayList<String>();		
		
		//ǩ������,��֪ID,PK,SK,M,params
		String ID = "lalala";
		String message = null;
		//���ǩ��Ϊ��(h,V)
		Element h,V;
		Element r,R;
		String infoH2 = null;
				
		//�洢ÿ��ǩ���Ľ��
		List<Element> signh = new ArrayList<Element>();
		List<Element> signV = new ArrayList<Element>();
		
		long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��
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
		
		long endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		long result = endTime-startTime;
		System.out.println(count + "��ǩ����������ʱ�䣺 " + result +"ms");	
		signResult.add(result);

		
		//��֤����,��֪ID,PK,M,(h,V),params
		Iterator iterh = signh.iterator();
		Iterator iterV = signV.iterator();
		Iterator iterM = mes.iterator();
		
		Element a;
		Element Q;
		String infoH1=null;
//		String infoH2=null;
		int trueNum = 0;	//��¼ͨ���� 
		
		startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��	
		//����R���Ƚ�h�Ƿ����H3
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
		
		endTime=System.currentTimeMillis(); //��ȡ����ʱ��
		result = endTime-startTime;
		result = result*2;
		System.out.println(count+ "����֤����ʱ�䣺 "+ result +"ms");	
		verifyResult.add(result);

		long passRate = (long)trueNum/(long)count;
		System.out.println("ͨ����֤��"+ trueNum +"�Σ�ͨ���ʣ�"+ (passRate * 100) + "%");
//		passResult.add(passRate);
		
	}
	
	
	public static Element getHash(String str,Field G){
		byte[] msg = str.getBytes();
		return G.newElement().setFromHash(msg, 0, msg.length);		
	}

	
	
	
	
	
	
}
