package skyline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import skyline.algorithms.IsDominate;
import skyline.model.SkyTuple;
import skyline.model.MutaTuple;

/**
 * Initializer类负责完成程序执行的一切初始化工作，包括
 * (1)预先计算出不同数据类型的数据在不同滑动窗口规模下的globalSP和candidateSP
 * (2)程序开始之前读取数据将滑动窗口填满,并读取globalSP和candidateSP数据填入相应 数据结构
 * 
 * @author Administrator
 *
 */
public class Initializer {

	/**
	 * Initializer类的不带参数的构造函数
	 */
	public Initializer(){}
	
	/**
	 * 将元组装载到全局滑动窗口中: 迅速从指定文件中读取指定数目的数据将滑动窗口填满
	 * 
	 * @param globalWindow_list			指定全局滑动窗口
	 * @param globalWindowSize			指定的全局滑动窗口规模
	 */
	public void loadGlobalWindow(LinkedList<SkyTuple> globalWindow_list, long globalWindowSize, 
			String fileDir, String filename){
		
		File fileIn = new File(fileDir + filename);
		
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			long countLine = 0;										// 控制从读取数据多少行
			Hpreprocess hp = new Hpreprocess();						// 数据预处理
			SkyTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				if(countLine < globalWindowSize){
					str_line = bReader.readLine();
					if(str_line != null){
						tuple = hp.buildTupleFromStr(str_line);
						globalWindow_list.offer(tuple);				// 将读取的数据插入globalWindow_list中
						
						countLine ++;
					}
				}else{
					break;											// 滑动窗口灌满，跳出并结束循环
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	/**
	 * 对算法做初始化，包括：计算globalSkylines和candidateSkylines, 即对globalSP_list和candidateSP_list的初始化.
	 * globalSP_list和candidateSP_list这两个链表都是以SkyTuple自然序的逆序组织的，即id大的元组放在链表头
	 * 
	 * 以滑动窗口globalWindow为对象计算skyline集合,并放入globalSP_list中; 把符合指定条件的元组放到candidateSP_list中
	 * 
	 * 如果globalSP_list不为空，则newTuple要逐个与globalSP_list中的数据比较（从tail（新）向head（旧）比较）
	 * 若newTuple被支配，则直接把newTuple插入candidateSP_list，且其latestDominateID为支配它的globalSP_list元组，比较结束
	 * 若newTuple支配globalSP_list中的元组tempTuple，则把tempTuple从globalSP_list中移除，继续向globalSP_list的下一元组比较
	 * 每次有元组插入globalSP_list或candidateSP_list时，都应删除candidateSP_list中被newTuple支配的元组或更新latestDominateID
	 * 
	 * @param globalWindow_list 			已经预装载好
	 * @param globalSP_list
	 * @param candidateSP_list
	 */
	public void initializeGSPandCSP(LinkedList<SkyTuple> globalWindow_list, 
			LinkedList<SkyTuple> globalSP_list, LinkedList<MutaTuple> candidateSP_list){
		
		for(SkyTuple newTuple: globalWindow_list){								// 从表头开始（id小的元组）
			
			if(!globalSP_list.isEmpty()){
				
				int index = 0;													// 从globalSP_list的head(id较大)开始比较
				
				while(true){
					SkyTuple tempTuple = globalSP_list.get(index);				// 取globalSP_list中head(id较大)的元组tempTuple与newTuple比较
					int isDominate = IsDominate.dominateBetweenTuples(tempTuple, newTuple);
					
					//若tempTuple支配newTuple，则newTuple应插入candidateSP_list
					if(isDominate == 0){
						insertIntoCSP(candidateSP_list, newTuple, tempTuple.getTupleID());
						break;													// 结束while循环，取下一个newTuple
					}
					// 若tempTuple被newTuple支配，则从globalSP_list中移除tempTuple，newTuple与globalSP_list中的下一个tempTuple比较
					else if(isDominate == 1){
						globalSP_list.remove(index);
					}
					// 若tempTuple与newTuple互不支配，则newTuple应与globalSP_list中的下一个tempTuple比较
					else if(isDominate == 2){
						index ++;
					}
					
					// 如果比较到globalSP_list的最后一个对象而newTuple仍然未被支配，则将其加入该链表的表尾
					if(index == globalSP_list.size()){				
						globalSP_list.offerFirst(newTuple);						// 新元组都插入链表头，以一种逆序组织globalSkylines链表
						updateCSP(candidateSP_list, newTuple);
						
						break;													// 结束while循环，取下一个newTuple
					}
				}
			}else{
				globalSP_list.offerFirst(newTuple);
			}
		}
	}
	
	/**
	 * 向candidateSP_list中插入一个元组newTuple(不属于globalSP_list): 
	 * 首先检查newTuple与candidateSP_list中各元组的支配关系，删除被newTuple支配的元组，以及在需要的时候更新newTuple的latestDominateId
	 * CSP以逆于SkyTuple自然序的顺序组织
	 * 
	 * @param newTuple
	 * @param latestDominateID
	 */
	public void insertIntoCSP(LinkedList<MutaTuple> candidateSP_list, SkyTuple newTuple, long latestDominateID){
		
		if(!candidateSP_list.isEmpty()){
			Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
			while(iter.hasNext()){
				SkyTuple tmp = iter.next().getSkyTuple();
				int flag = IsDominate.dominateBetweenTuples(newTuple, tmp);
				
				// 如果newTuple支配tmp, 则从candidateSP_list中删除tmp,比较candidateSP_list中的下一个元组
				if(flag == 0){
					iter.remove();
				}
				// 如果tmp支配newTuple,则更新newTuple的latestDominateId, 并结束比较(candidateSP_list是有序的)
				else if(flag == 1){
					if(latestDominateID < tmp.getTupleID())
						latestDominateID = tmp.getTupleID();
					break;
				}
				// 如果newTuple与tmp互不支配,则比较candidateSP_list中的下一个元组
				else if(flag == 2){
					continue;
				}
			}
		}
		candidateSP_list.offerFirst(new MutaTuple(newTuple, latestDominateID));				// 新元组newTuple插入CSP的链表头
	}
	
	/**
	 * 更新candidateSP_list: 主要是移除candidateSP_list中被newTuple支配的元组
	 * (而newTuple是属于globalSP_list的, 肯定不会被candidateSP_list中的元组支配)
	 * 
	 * @param tuple
	 */
	public void updateCSP(LinkedList<MutaTuple> candidateSP_list, SkyTuple newTuple){
		
		if(!candidateSP_list.isEmpty()){
			Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
			while(iter.hasNext()){
				int flag = IsDominate.dominateBetweenTuples(newTuple, iter.next().getSkyTuple());
				if(flag == 0){
					iter.remove();
				}
			}
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	/**
	 * 将GSP文件中的元组装载到全局Skyline链表中：以逆于SkyTuple自然序的顺序组织
	 * 
	 * @param globalSP_list			存放全局Skyline的链表
	 * @param dataType				数据分布类型
	 * @param dim					数据维度
	 * @param globalWindowSize		滑动窗口大小
	 * @param infileDir				数据文件目录
	 */
	public void loadGSP(LinkedList<SkyTuple> globalSP_list, int dataType, int dim, long globalWindowSize, String infileDir){
		
		String infilename = "GSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();					// 数据预处理
			SkyTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildTupleFromStr(str_line);
					globalSP_list.offer(tuple);					// 将读取的数据插入globalSP_list的链表尾
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 将CSP文件中的元组装载到全局CSP链表中：以逆于SkyTuple自然序的顺序组织
	 * 
	 * @param candidateSP_list		存放全局CSP的链表
	 * @param dataType				数据分布类型
	 * @param dim					数据维度
	 * @param globalWindowSize		滑动窗口大小
	 * @param infileDir				数据文件目录
	 */
	public void loadCSP(LinkedList<MutaTuple> candidateSP_list, int dataType, int dim, long globalWindowSize, String infileDir){
		
		String infilename = "CSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// 数据预处理
			MutaTuple mtuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					mtuple = hp.buildMutaTupleFromStr(str_line);
					candidateSP_list.offer(mtuple);							// 将读取的数据插入candidateSP_list的链表尾
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	/**
	 * SharedSkyline的GSP load方法,将预先计算好的GSP文件装载到多个subGSPs链表中,按元组(tupleID % numSubGSP)组织
	 * 
	 * @param subGSPs					全局Skyline链表(划分为多个子表)
	 * @param dataType					数据分布类型
	 * @param dim						数据维度
	 * @param numSubGSP					将全局GSP划分为子表的数目(这个值一般需要根据具体GSP长度来确定一个比较合适的值)
	 * @param globalWindowSize			全局滑动窗口大小
	 * @param infileDir					数据文件目录
	 */
	public void loadGSPForSharedSkyline(ArrayList<LinkedList<SkyTuple>> subGSPs, int dataType, int dim, int numSubGSP, 
			long globalWindowSize, String infileDir){
		
		String infilename = "GSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// 数据预处理
			SkyTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildTupleFromStr(str_line);
					
					int index = (int) (tuple.getTupleID() % numSubGSP);
					subGSPs.get(index).offer(tuple);						// 将读取的数据插入subGSP[index]的链表尾
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * SharedSkyline以及PsSkyline的CSP load方法,将预先计算好的CSP文件装载到多个subCSPs链表中,按元组的latestDominateId % numSubCSP组织
	 * 
	 * @param subCSPs			全局Skyline链表(划分为多个子表)
	 * @param dataType			数据分布类型
	 * @param dim				数据维度
	 * @param numSubCSP			将全局CSP划分为子表的数目(这个值一般需要根据具体latestDominateId分布情况来确定一个比较合适的值)
	 * @param globalWindowSize	全局滑动窗口大小
	 * @param infileDir			数据文件目录
	 */
	public void loadCSPForParallelSkyline(ArrayList<LinkedList<MutaTuple>> subCSPs, int dataType, int dim, int numSubCSP, 
			long globalWindowSize, String infileDir){
		
		String infilename = "CSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// 数据预处理
			MutaTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildMutaTupleFromStr(str_line);
					
					int index = (int) (tuple.getDominateID() % numSubCSP);
					subCSPs.get(index).offer(tuple);						// 将读取的数据插入subCSP[index]的链表尾
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	
	/**
	 * 从原始数据文件中读取数据流元组并按元组号取模填入各个局部滑动窗口中
	 * 
	 * @param localWindows		多个局部滑动窗口
	 * @param globalWindowSize	全局滑动窗口的规模
	 * @param infileDir 		原始数据所在的文件夹
	 * @param infilename		原始数据的文件名
	 * @param psExecutorNum		参与计算的线程数目
	 */
	public void loadLocalWindow(ArrayList<LinkedList<SkyTuple>> localWindows, long globalWindowSize, 
			String infileDir, String infilename, int psExecutorNum){
		
		File fileIn = new File(infileDir + infilename);
		
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			long countLine = 0;										// 控制从读取数据多少行
			Hpreprocess hp = new Hpreprocess();						// 数据预处理
			SkyTuple tuple = null;	
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				if(countLine < globalWindowSize){
					str_line = bReader.readLine();
					
					if(str_line != null){
						tuple = hp.buildTupleFromStr(str_line);
						
						int index = (int) (tuple.getTupleID() % psExecutorNum);
						localWindows.get(index).offer(tuple);		// 将读取的数据按照其元组号插入对应的localWindow中
						
						countLine ++;
					}
				}else{
					break;											// 滑动窗口灌满，跳出并结束循环
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 将GSP文件中的元组装载到subGSP链表数组中
	 * @param subGSPs				存放全局Skyline的链表数组
	 * @param dataType				数据分布类型
	 * @param dim					数据维度
	 * @param numPsExecutor			参与计算的线程数目
	 * @param globalWindowSize		全局滑动窗口大小
	 * @param infileDir				数据文件目录
	 */
	public void loadGSPForPsSkyline(ArrayList<LinkedList<SkyTuple>> subGSPs, int dataType, int dim, int numPsExecutor, 
			long globalWindowSize, String infileDir){
		
		String infilename = "GSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// 与文件读取相关的各种变量
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// 数据预处理
			SkyTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildTupleFromStr(str_line);
					
					int index = (int) (tuple.getTupleID() % numPsExecutor);
					subGSPs.get(index).offer(tuple);						// 将读取的数据插入subGSP[index]的链表尾
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}

