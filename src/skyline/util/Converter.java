package skyline.util;

import java.util.LinkedList;

import skyline.model.SkyTuple;

/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class Converter {
	
	/**
	 * arrayToString方法，将array对象转换成String类型，且每个维度之间用","隔开
	 * @param array 待转换的数组，为double类型
	 * @return 返回值为转换后的结果，为String类型
	 */
	public static String arrayToString(double[] array){
		int len=array.length;
		String str = "";
		for(int i=0; i<len; i++){
			str+=array[i];
			if(i<len-1)
				str+=",";
		}
		return str;
	}
	
	/**
	 * strToArray方法，将读入的一行数据元组按","分割开，并将每一个划分存入String数组中。
	 * 最后，将String数组转换成double数组
	 * @param str 待转换的字符串
	 * @return
	 */
	public static double[] strToArray(String str){
		String[] mark =  str.split(",");
		int dim = mark.length;
		double[] array = new double[dim];
		for(int i=0; i<mark.length; i++){
			array[i] = Double.parseDouble(mark[i]);
			//System.out.println(mark[i]);
		}
		return array;
	}
	
	/**
	 * To convert a linkedlist to a string
	 * @return
	 */
	public static StringBuilder list2str(LinkedList<SkyTuple> tuple_list){
		StringBuilder str_builder = new StringBuilder();
		while(tuple_list.size() > 1){
			str_builder.append(tuple_list.removeFirst().toStringTuple());
			str_builder.append(";");
		}
		str_builder.append(tuple_list.removeFirst().toStringTuple());
		return str_builder;
	}
	
	/**
	 * To convert a long string builder to a sequence of tuples
	 */
	public static LinkedList<SkyTuple> str2list(String str){
		LinkedList<SkyTuple> tuple_list = new LinkedList<SkyTuple>();
		Hpreprocess hp = new Hpreprocess();
		String[] mark_semicolon = str.split(";");
		for(String str_tuple: mark_semicolon){
			tuple_list.addLast(hp.buildTupleFromStr(str_tuple));
		}
		 
		return tuple_list;
		
	}
	
}
