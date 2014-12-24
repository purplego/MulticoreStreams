package skyline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import skyline.model.MutaTuple;
import skyline.model.SkyTuple;

/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class Hpreprocess {
	
	private LinkedList<SkyTuple> tuple_list;
	
	//the constructor function
	public Hpreprocess(){
		this.tuple_list = new LinkedList<SkyTuple>();
	}
	
	//the setter and getter of the member variable
	public void setTuple_list(LinkedList<SkyTuple> tuple_list) {
		this.tuple_list = tuple_list;
	}
	public LinkedList<SkyTuple> getTuple_list() {
		return tuple_list;
	}
	
	/**
	 * buildStrList��������ȡԭʼ�����ļ�������һ����String���Ͷ���Ϊ�ڵ������
	 * @param fileDir 		���ԭʼ�����ļ���Ŀ¼
	 * @param filename		ԭʼ�����ļ�
	 * @return temp_list	�����õ�String����
	 */
	public LinkedList<String> buildStrList(String fileDir, String filename){
		LinkedList<String> temp_list = new LinkedList<String>();
		
		String filePathIn = fileDir + filename;
		File fileIn = new File(filePathIn);
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);	
			
			String str_line = "";					//�ֲ�����, ���ÿ�δ��ļ��ж�����һ���ַ���
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line == null){
					System.out.println("Error read! Null line!");
				}
				else{
					temp_list.add(str_line);		//����ȡ��String������뵽LinkedList��ȥ
				}
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
		return temp_list;
	}

	/**
	 * buildTupleList��������ȡԭʼ�����ļ�������һ����Tuple���Ͷ���Ϊ�ڵ������
	 * @param fileDir 		���ԭʼ�����ļ���Ŀ¼
	 * @param filePathIn	ԭʼ�����ļ���Ŀ¼
	 */
	public void buildTupleList(String fileDir, String filename){
		tuple_list.clear();
		String filePathIn = fileDir + filename;
		File fileIn = new File(filePathIn);
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);	
			
			//�ֲ�����
			SkyTuple tuple = null;					//����һ��Ԫ�����
			String str_line = "";					//���ÿ�δ��ļ��ж�����һ���ַ���
			
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line == null){
					System.out.println("Error read! Null line!");
				}
				else{
					tuple = buildTupleFromStr(str_line);
					tuple_list.add(tuple);			//������õ�tuple������뵽LinkedList��ȥ
				}
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * buildTupleFromStr����������һ���ַ��������Ϣ����Tuple����
	 * @param str ��������ַ���
	 * @return ����ɵ�Tuple����
	 */
	public SkyTuple buildTupleFromStr(String str){
		
		/*
		 * ���ա��������ַ���str�зֳ����ɶβ��洢������mark[]��
		 * ���е�һ��Ϊid��ʣ�����ɶ�Ϊ��ά������ֵ
		 */
		String[] mark = str.split(",");
		
		long id = Long.parseLong(mark[0]);
		double[] attrs = new double[Constants.DIMENSION];
		for(int i=1; i<mark.length; i++){
			attrs[i-1] = Double.parseDouble(mark[i]);
		}
		
		//���ݳ�ȡ����Ϣ������һ��Tuple����
		SkyTuple tuple = new SkyTuple(id, attrs);
		return tuple;
	}
	
	/**
	 * buildMutaTupleFromStr����������һ���ַ��������Ϣ����Tuple����
	 * @param str ��������ַ���
	 * @return ����ɵ�Tuple����
	 */
	public MutaTuple buildMutaTupleFromStr(String str){
		
		/*
		 *  ����" "(�ո�)���ַ���str�зֳ�����
		 *  ��һ��ΪSkyTuple���ַ������ڶ���ΪlatestDominateId
		 */
		String[] mark = str.split(" ");
		
		SkyTuple stuple = buildTupleFromStr(mark[0]);
		mark[1] = mark[1].replace("[", "");
		mark[1] = mark[1].replace("]", "");
		long latestDominateId = Long.parseLong(mark[1]);
		
		//���ݳ�ȡ����Ϣ������һ��MutaTuple����
		MutaTuple mutaTuple = new MutaTuple(stuple, latestDominateId);
		
		return mutaTuple;
	}
	
}
