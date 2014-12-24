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
	 * buildStrList方法，读取原始数据文件，构造一个以String类型对象为节点的链表
	 * @param fileDir 		存放原始数据文件的目录
	 * @param filename		原始数据文件
	 * @return temp_list	构建好的String链表
	 */
	public LinkedList<String> buildStrList(String fileDir, String filename){
		LinkedList<String> temp_list = new LinkedList<String>();
		
		String filePathIn = fileDir + filename;
		File fileIn = new File(filePathIn);
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);	
			
			String str_line = "";					//局部变量, 存放每次从文件中读出的一行字符串
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line == null){
					System.out.println("Error read! Null line!");
				}
				else{
					temp_list.add(str_line);		//将读取的String对象加入到LinkedList中去
				}
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
		return temp_list;
	}

	/**
	 * buildTupleList方法，读取原始数据文件，构造一个以Tuple类型对象为节点的链表
	 * @param fileDir 		存放原始数据文件的目录
	 * @param filePathIn	原始数据文件的目录
	 */
	public void buildTupleList(String fileDir, String filename){
		tuple_list.clear();
		String filePathIn = fileDir + filename;
		File fileIn = new File(filePathIn);
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);	
			
			//局部变量
			SkyTuple tuple = null;					//声明一个元组对象
			String str_line = "";					//存放每次从文件中读出的一行字符串
			
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line == null){
					System.out.println("Error read! Null line!");
				}
				else{
					tuple = buildTupleFromStr(str_line);
					tuple_list.add(tuple);			//将构造好的tuple对象加入到LinkedList中去
				}
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * buildTupleFromStr方法，根据一串字符串拆分信息构造Tuple对象
	 * @param str 待处理的字符串
	 * @return 构造成的Tuple对象
	 */
	public SkyTuple buildTupleFromStr(String str){
		
		/*
		 * 按照“，”将字符串str切分成若干段并存储在数组mark[]中
		 * 其中第一段为id，剩余若干段为各维度属性值
		 */
		String[] mark = str.split(",");
		
		long id = Long.parseLong(mark[0]);
		double[] attrs = new double[Constants.DIMENSION];
		for(int i=1; i<mark.length; i++){
			attrs[i-1] = Double.parseDouble(mark[i]);
		}
		
		//根据抽取的信息，构造一个Tuple对象
		SkyTuple tuple = new SkyTuple(id, attrs);
		return tuple;
	}
	
	/**
	 * buildMutaTupleFromStr方法，根据一串字符串拆分信息构造Tuple对象
	 * @param str 待处理的字符串
	 * @return 构造成的Tuple对象
	 */
	public MutaTuple buildMutaTupleFromStr(String str){
		
		/*
		 *  按照" "(空格)把字符串str切分成两段
		 *  第一段为SkyTuple的字符串，第二段为latestDominateId
		 */
		String[] mark = str.split(" ");
		
		SkyTuple stuple = buildTupleFromStr(mark[0]);
		mark[1] = mark[1].replace("[", "");
		mark[1] = mark[1].replace("]", "");
		long latestDominateId = Long.parseLong(mark[1]);
		
		//根据抽取的信息，构造一个MutaTuple对象
		MutaTuple mutaTuple = new MutaTuple(stuple, latestDominateId);
		
		return mutaTuple;
	}
	
}
