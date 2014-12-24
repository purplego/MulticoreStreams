package skyline.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;


import skyline.algorithms.BNL;
import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.Hpreprocess;
import skyline.util.Print;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		LinkedList<Long> long_list = new LinkedList<Long>();
		Iterator<Long> it = long_list.listIterator(0);
		
//		while(it.hasNext()){
//			System.out.println(it.next());
//		}
		for(long item: long_list){
			long i = 1000l;
			i += item;
			System.out.println(i);
		}
		
		/*System.out.println(Constants.GlobalWindowSize);
		System.out.println(Constants.LocalWindowSize);
		System.out.println();
		Constants.GlobalWindowSize = 50;
		Constants.NumPsExecutor = 25;
		System.out.println(Constants.GlobalWindowSize);
		System.out.println(Constants.GlobalWindowSize / Constants.NumPsExecutor);*/
		
		/*
		String fileDir = Constants.DATA_DIRECTORY;
		String infilename = "rawdata.txt";
		
		LinkedList<SkyTuple> window_list = new LinkedList<SkyTuple>();
		File fileIn = new File(fileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			long countLine = 0;						// 控制从读取数据多少行
			Hpreprocess hp = new Hpreprocess();		// 数据预处理
			SkyTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				if(countLine < 5){
					str_line = bReader.readLine();
					if(str_line != null){
//						tuple = hp.buildTupleFromStr(str_line);
//						window_list.offer(tuple);	// 将读取的数据插入globalSlidingWindow中
						System.out.println(str_line);
						countLine ++;
					}
				}else{
					break;	// 滑动窗口灌满，跳出并结束循环
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			System.err.println("This is the end of the file.");
		}*/
		
//		Print.printList(window_list);
//		System.out.println("-------------------------------------------------------------------");
//		LinkedList<SkyTuple> gsp_list = BNL.bnlQuery(window_list);
//		Print.printList(gsp_list);
//		Collections.sort(gsp_list);
//		
//		System.out.println("-------------------------------------------------------------------");
//		Print.printList(gsp_list);
	}

}
