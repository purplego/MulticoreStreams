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
	 * writeRandomToFile方法，将生成的随机数元组写入文件，并且按参数distrType的不同分别生成独立、相关、反相关数据
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename	保存生成的随机数元组
	 * @param fileSize	生成元组的个数，即集的势的规模
	 * @param dataType	生成随机数的分布类型，0：生成独立分布的数据；1：生成相关数据；2：生成反相关数据
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
	 * writeIndepToFile,生成一定规模的独立分布元组，并将其存入指定文件中
	 * @param fileDir 		存放数据文件的目录
	 * @param filePathOut 	保存独立分布数据的文件
	 * @param fileSize 		生成元组的个数，即集的势的规模
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
	 * writeCorrToFile,生成一定规模的相关元组，并将其存入指定文件中
	 * @param fileDir 		存放数据文件的目录
	 * @param filePathOut 	保存相关数据的文件
	 * @param fileSize 		生成元组的个数，即及集的势的规模
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
	 * writeAntiToFile,生成一定规模的反相关元组，并将其存入指定文件中
	 * @param fileDir 		存放原始数据文件的目录
	 * @param filePathOut 	保存反相关数据的文件
	 * @param fileSize 		生成元组的个数，即集的势的规模
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
	 * writeList方法，将一个节点类型为SkyTuple的LinkedList以节点为单位写入文件中
	 * @param list 		待处理的链表
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename 	待写入的文件名
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
	 * writeList方法，将一个节点类型为MutaTuple的LinkedList以节点为单位写入文件中
	 * @param list 		待处理的链表
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename 	待写入的文件名
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
	 * writeMapToFile方法，将一个节点类型为<Long, SkyTuple>的Map以节点为单位写入文件中
	 * @param map_tuple 待处理的map
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename	待写入的文件名
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
	 * writeMapToFile2方法，将一个节点类型为<Long, double[]>的Map以节点为单位写入文件中
	 * @param map_tuple 待处理的map
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename	待写入的文件名
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
	 * writeMapToFile3方法，将一个节点类型为<Long, Double>的Map以节点为单位写入文件中
	 * @param map_tuple 待处理的map
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename	待写入的文件名
	 */
	public static void writeMapToFile3(Map<Long, Double> map, String fileDir, String filename){
		
		String filePathOut = fileDir + filename;
		File fileOut = new File(filePathOut);
		
		try{
			FileWriter fWriter = new FileWriter(fileOut);
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			
			//采用除了Iterator和Collection之外的方法，即Map.Entry来对Map做序列化动作
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
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename	the file to be appended
	 * @param str the	content to be appended
	 */
	public static void appendStr(String fileDir, String filename, String str){
		//以追加写的方式写入到文件
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
