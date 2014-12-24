package skyline.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import skyline.algorithms.BNL;
import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.FSLog;
import skyline.util.Hpreprocess;
import skyline.util.WriteFile;

public class EstimateGSP {

	/**
	 * 评估不同数据分布类型、不同维度的数据在不同长度滑动窗口下，
	 * 计算整个滑动窗口的全局Skyline(GSP)所需的时间，以及GSP的规模
	 * 数据类型为0、1、2，维度为2、3、4、5、6、7，滑动窗口长度分别为10K、100K、1M、10M、100M
	 * @param args
	 */
	public static void main(String[] args) {

		// 用于写日志
		FSLog logger = new FSLog("./log/", "estimateGSP.txt");
		
		// 指定数据文件的目录和文件名
		String infileDir = "//10.107.18.220/data/";
		String infilename = "";
		String outfileDir = "./data/";
		String outfilename = "";
		
		// 第一重循环，指定数据分布类型为0、1、2
		for(int dataType = 0; dataType <= 2; dataType ++){
			Constants.DATATYPE = dataType;
			
			// 第二重循环，指定数据的维度为2、3、4、5、6、7
			for(int dim = 2; dim <= 7; dim ++){
				Constants.DIMENSION = dim;
				
				// 第三重循环，指定全局滑动窗口长度为10K、100K、1M、10M、100M
				long windowSize = 10000;
				while(windowSize <= 100000000){
					infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
					LinkedList<SkyTuple> window = new LinkedList<SkyTuple>();
					
					/*
					 * 从指定文件filename中读取windowSize各数据并插入链表window中
					 */
					File fileIn = new File(infileDir + infilename);
					try{
						FileReader fReader = new FileReader(fileIn);
						BufferedReader bReader = new BufferedReader(fReader);
						
						String str_line = "";
						Hpreprocess hp = new Hpreprocess();
						SkyTuple tuple = null;
						while(bReader.ready()){
							str_line = bReader.readLine();				// 从文件中读取一行内容
							if(str_line != null){
								tuple = hp.buildTupleFromStr(str_line);	// 解析读取的字符串为SkyTuple类型
								window.offer(tuple);					// 把读取的数据加入到链表中
								if(window.size() < windowSize){
									continue;							// 继续下一次循环，读取下一行文件内容
								}else{	
									break;								// 跳出while循环，停止读取文件
								}
							}else{
								System.err.println("Error read! Null line!");
							}
						}
						bReader.close();
					}catch(Exception e){
						e.printStackTrace();
					}
					
					/*
					 * 以window为数据集计算该window之上的全局Skyline，即GSP
					 */
					LinkedList<SkyTuple> gsp_list = new LinkedList<SkyTuple>();
					long time0 = System.currentTimeMillis();
					gsp_list = BNL.bnlQuery(window);								// 利用BNL算法计算全局Skyline
					long time1= System.currentTimeMillis();
					outfilename = "GSP_" + dataType + "_" + Constants.genDim() + "_" + windowSize + ".txt";
					WriteFile.writeListToFile(gsp_list, outfileDir, outfilename);		// 将全局Skyline写入对应的文件中
					
					logger.info("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " / " + window.size() + "-------");
					logger.info("GSP computing time: " + (time1-time0) + " ms; GSP size: " + gsp_list.size());
					System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " / " + window.size() + "-------");
					System.out.println("GSP computing time: " + (time1-time0) + " ms; GSP size: " + gsp_list.size());
					
					// 对windowSize做增量
					windowSize *= 10;
				}
			}
			
		}
	}

}
