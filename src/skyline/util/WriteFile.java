package skyline.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import skyline.model.MutaTuple;
import skyline.model.RandGenerator;
import skyline.model.SkyTuple;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class WriteFile {
	
	/**
	 * writeRandomToFile�����������ɵ������Ԫ��д���ļ������Ұ�����distrType�Ĳ�ͬ�ֱ����ɶ�������ء����������
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename	�������ɵ������Ԫ��
	 * @param fileSize	����Ԫ��ĸ������������ƵĹ�ģ
	 * @param dataType	����������ķֲ����ͣ�0�����ɶ����ֲ������ݣ�1������������ݣ�2�����ɷ��������
	 */
	public static void writeRandomToFile(String fileDir, String filename,long cardinality, int dim, int dataType){
		
		switch(dataType){
		case 0:
			writeIndepToFile(fileDir, filename, cardinality, dim);
			break;
		case 1:
			writeCorrToFile(fileDir, filename, cardinality, dim);
			break;
		case 2:
			writeAntiToFile(fileDir, filename, cardinality, dim);
			break;
		default:
			System.out.println("Error distribution type!");
			break;
		}
	}
	
	/**
	 * writeIndepToFile,����һ����ģ�Ķ����ֲ�Ԫ�飬���������ָ���ļ���
	 * @param fileDir 		��������ļ���Ŀ¼
	 * @param filePathOut 	��������ֲ����ݵ��ļ�
	 * @param fileSize 		����Ԫ��ĸ������������ƵĹ�ģ
	 */
	public static void writeIndepToFile(String fileDir, String filename,long cardinality, int dim){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);	
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			double[] arrayTemp = new double[dim];
			for(int i=0; i<cardinality; i++){
				String str = "";
				str += i;											//the id of the random data
				str += ",";
				arrayTemp = RandGenerator.generate_indep(dim);		//the attrbutes of the random data
				str += Converter.arrayToString(arrayTemp);
				
				bWriter.write(str);
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writeCorrToFile,����һ����ģ�����Ԫ�飬���������ָ���ļ���
	 * @param fileDir 		��������ļ���Ŀ¼
	 * @param filePathOut 	����������ݵ��ļ�
	 * @param fileSize 		����Ԫ��ĸ��������������ƵĹ�ģ
	 */
	public static void writeCorrToFile(String fileDir, String filename,long cardinality, int dim){
		
		String filePathOut = fileDir + filename;
		File fileOut=new File(filePathOut);	
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			double[] arrayTemp = new double[dim];
			for(int i=0; i<cardinality; i++){
				String str = "";
				str += i;											//the id of the random data
				str += ",";
				arrayTemp = RandGenerator.generate_corr(dim);		//the attrbutes of the random data
				str += Converter.arrayToString(arrayTemp);
				
				bWriter.write(str);
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writeAntiToFile,����һ����ģ�ķ����Ԫ�飬���������ָ���ļ���
	 * @param fileDir 		���ԭʼ�����ļ���Ŀ¼
	 * @param filePathOut 	���淴������ݵ��ļ�
	 * @param fileSize 		����Ԫ��ĸ������������ƵĹ�ģ
	 */
	public static void writeAntiToFile(String fileDir, String filename,long cardinality, int dim){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);	
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			double[] arrayTemp = new double[dim];
			for(int i=0; i<cardinality; i++){
				String str = "";
				str += i;											//the id of the random data
				str += ",";
				arrayTemp = RandGenerator.generate_anti(dim);		//the attrbutes of the random data
				str += Converter.arrayToString(arrayTemp);
				
				bWriter.write(str);
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * **************************************************************************************************************************
	 * **************************************************************************************************************************
	 */
	
	/**
	 * writeList��������һ���ڵ�����ΪSkyTuple��LinkedList�Խڵ�Ϊ��λд���ļ���
	 * @param list 		�����������
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename 	��д����ļ���
	 */
	public static void writeListToFile(LinkedList<SkyTuple> list, String fileDir, String filename){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);
		
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			for(SkyTuple tuple: list){
				bWriter.write(tuple.toStringTuple());
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writeList��������һ���ڵ�����ΪMutaTuple��LinkedList�Խڵ�Ϊ��λд���ļ���
	 * @param list 		�����������
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename 	��д����ļ���
	 */
	public static void writeListToFile2(LinkedList<MutaTuple> list, String fileDir, String filename){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);
		
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			for(MutaTuple tuple: list){
				bWriter.write(tuple.toStringTuple());
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writeMapToFile��������һ���ڵ�����Ϊ<Long, SkyTuple>��Map�Խڵ�Ϊ��λд���ļ���
	 * @param map_tuple �������map
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename	��д����ļ���
	 */
	public static void writeMapToFile(Map<Long, SkyTuple> map_tuple, String fileDir, String filename){
		
		Collection<SkyTuple> tuple_collection = map_tuple.values();
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);
		
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			for(SkyTuple tuple: tuple_collection){
				bWriter.write(tuple.toStringTuple());
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writeMapToFile2��������һ���ڵ�����Ϊ<Long, double[]>��Map�Խڵ�Ϊ��λд���ļ���
	 * @param map_tuple �������map
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename	��д����ļ���
	 */
	public static void writeMapToFile2(Map<Long, double[]> map_tuple, String fileDir, String filename){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);
		
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			Set<Long> key_set = map_tuple.keySet();
			Iterator<Long> it = key_set.iterator();
			while(it.hasNext()){
				long key = it.next();
				double[] attrs = map_tuple.get(key);
				String str = key + "," + Converter.arrayToString(attrs);
				bWriter.write(str);
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * writeMapToFile3��������һ���ڵ�����Ϊ<Long, Double>��Map�Խڵ�Ϊ��λд���ļ���
	 * @param map_tuple �������map
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename	��д����ļ���
	 */
	public static void writeMapToFile3(Map<Long, Double> map, String fileDir, String filename){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);
		
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			//���ó���Iterator��Collection֮��ķ�������Map.Entry����Map�����л�����
			for(Map.Entry<Long, Double> m_entry: map.entrySet()){
				long key = m_entry.getKey();
				double value = m_entry.getValue();
				
				String str = key + "," + value;
				bWriter.write(str);
				bWriter.newLine();
				bWriter.flush();
			}
			bWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * appendStr to add a string to the end of an existed file
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename	the file to be appended
	 * @param str the	content to be appended
	 */
	public static void appendStr(String fileDir, String filename, String str){
		//��׷��д�ķ�ʽд�뵽�ļ�
		String str_temp = str + "\r\n";
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);	
		try{
			FileWriter fWriter = new FileWriter(fileOut, true);
			fWriter.write(str_temp);
			fWriter.flush();
			fWriter.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}
