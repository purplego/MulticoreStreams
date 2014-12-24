package skyline.util;

import java.util.LinkedList;

import skyline.model.SkyTuple;

/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class Converter {
	
	/**
	 * arrayToString��������array����ת����String���ͣ���ÿ��ά��֮����","����
	 * @param array ��ת�������飬Ϊdouble����
	 * @return ����ֵΪת����Ľ����ΪString����
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
	 * strToArray�������������һ������Ԫ�鰴","�ָ������ÿһ�����ִ���String�����С�
	 * ��󣬽�String����ת����double����
	 * @param str ��ת�����ַ���
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
