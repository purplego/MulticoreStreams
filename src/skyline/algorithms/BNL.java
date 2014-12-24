package skyline.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import skyline.model.SkyTuple;
import skyline.util.Hpreprocess;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class BNL {
	
	/**
	 * bnlSkyline方法，从文件inFilename中读取原始数据集，利用BNL算法求得skyline点
	 * 所得的spList按照SkyTuple的自然序组织
	 * 
	 * @param fileDir 		存放原始数据文件的目录	
	 * @param inFilename	存放原始数据集的文件
	 * @return spList		返回存放局部skyline的LinkedList即spList
	 */
	public static LinkedList<SkyTuple> bnlQuery(String fileDir, String inFilename){
		
		String filePathIn = fileDir + inFilename;
		File fileIn=new File(filePathIn);
		
		//定义一个节点为数组类型的链表，用于存放候选的Skyline点及最终结果
		LinkedList<SkyTuple> spList = new LinkedList<SkyTuple>();
		Hpreprocess h_process = new Hpreprocess();
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);
			
			while(bReader.ready()){
				String str_line = bReader.readLine();
				SkyTuple tuple = h_process.buildTupleFromStr(str_line);
				if(spList.isEmpty())
					spList.add(tuple);
				else{
					int i = 0;
					boolean flag = true;
					while(flag){
						//调用支配关系测试函数，测试两个对象之间的支配关系
						int isDominate = IsDominate.dominateBetweenTuples(spList.get(i), tuple);
						switch(isDominate){
						case 0:										//tuple被支配，直接删去该对象
							flag = false;							//跳出while循环，读下一个对象
							break;
						case 1:										//tuple支配spList中的第i个对象，删去该对象，继续往后比较，进入下一次循环
							spList.remove(i);
							break;
						case 2:										//tuple与spList中的第i个对象互不支配，继续往后比较，进入下一次循环
							i++;									//比较对象变为链表中的下一个对象
							break;
						default:
							break;
						}
						if(i==spList.size()){						//如果比较到spList的最后一个对象而tuple仍然未被支配，则将其加入该链表的表尾
							spList.addLast(tuple);
							flag = false;							//结束while循环，读文件中的下一行
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return spList;
	}
	
	
	/**
	 * bnlSkyline方法，从链表tuple_list中读取原始数据集，利用BNL算法求得skyline点
	 * 所得的spList按照SkyTuple的自然序的逆序组织
	 * 
	 * @param tuple_list，存放原始数据集的链表
	 * @return 返回存放局部skyline的LinkedList即spList, spList是按SkyTuple的自然序的逆序排序的，即id较大的元组排在链表头
	 */
	public static LinkedList<SkyTuple> bnlQuery(LinkedList<SkyTuple> tuple_list){
		
		//定义一个节点为数组类型的链表，用于存放候选的Skyline点及最终结果
		LinkedList<SkyTuple> sp_list = new LinkedList<SkyTuple>();
//		int countRemove = 0;
//		int countNotAdd = 0;
		
		for(SkyTuple tuple: tuple_list){
			
			if(sp_list.isEmpty()){
				sp_list.offerFirst(tuple);
			}else{
				int i = 0;
				boolean flag = true;
				while(flag){
					//调用支配关系测试函数，测试两个对象之间的支配关系
					int isDominate = IsDominate.dominateBetweenTuples(sp_list.get(i), tuple);
					switch(isDominate){
					case 0:											
						flag = false;								//跳出while循环，读下一个待判定对象
//						countNotAdd ++;
						break;
					case 1:											//tuple支配sp_list中的第i个对象，删去该对象，继续往后比较，进入下一次循环
						sp_list.remove(i);
//						countRemove ++;
						break;
					case 2:											//tuple与spList中的第i个对象互不支配，继续往后比较，进入下一次循环
						i++;										//比较对象变为链表中的下一个对象
						break;
					default:
						break;
					}
					if(i==sp_list.size()){							//如果比较到sp_list的最后一个对象而tuple仍然未被支配，则将其加入该链表的表尾
//						spList.addLast(tuple);
						sp_list.offerFirst(tuple);
						flag = false;								//结束while循环，读tuple_list中的下一行
					}
				}
			}
		}
//		System.out.println("the number of tuples removed from the GSP_List in BNL is :" + countRemove);
//		System.out.println("the number of tuples not add into the GSP_List in BNL is :" + countNotAdd);
		return sp_list;
	}
}
