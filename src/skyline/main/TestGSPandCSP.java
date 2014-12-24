package skyline.main;

import java.util.LinkedList;

import skyline.model.MutaTuple;
import skyline.model.MyLogger;
import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.Initializer;
import skyline.util.WriteFile;

public class TestGSPandCSP {

	/**
	 * 预先计算出不同数据分布类型、不同维度的数据在不同滑动窗口下的GSP和CSP，
	 * 给出整个计算所需时间，以及GSP和CSP的规模
	 * 数据类型为0、1、2，维度为2、3、4、5、6、7、8，滑动窗口长度分别为10M、20M、30M、40M、50M
	 * @param args
	 */
	public static void main(String[] args) {

		// 指定输入数据文件、输出数据文件和日志文件的目录和文件名
		String infileDir = "/root/multicoreQueries/data/";
		Constants.DATA_DIRECTORY = infileDir;
		String infilename = "";
		
		String outfileDir = infileDir;
		String outfilenameGSP = "";
		String outfilenameCSP = "";
		
		String logDir = "/root/multicoreQueries/log/";
		Constants.LOG_DIRECTORY = logDir;
		String logname = "";
		
		// 第一重循环，指定数据分布类型为0、1、2
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// 用于写日志
		logname = "GSPCSP_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "GSPCSP" + dataType);
			
		// 第二重循环，指定数据的维度为2、3、4、5、6、7、8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// 第三重循环，指定全局滑动窗口长度为10M、20M、30M、40M、50M
					
			for(long windowSize = 10000000; windowSize <= 50000000; windowSize += 10000000){	// 初始滑动窗口长度为10M
//			for(long windowSize = 1000; windowSize <= 1000000; windowSize *= 10){				// 初始滑动窗口长度为1K
				Constants.GlobalWindowSize = windowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				
				// 定义全局滑动窗口链表、全局Skyline链表和全局候选Skyline链表
				LinkedList<SkyTuple> globalWindow = new LinkedList<SkyTuple>();
				LinkedList<SkyTuple> gsp_list = new LinkedList<SkyTuple>();
				LinkedList<MutaTuple> csp_list = new LinkedList<MutaTuple>();
				
				// 装载窗口和计算GSP、CSP
				Initializer initializer = new Initializer();
				long time0 = System.currentTimeMillis();
				initializer.loadGlobalWindow(globalWindow, windowSize, infileDir, infilename);	// 将滑动窗口填满
				long time1 = System.currentTimeMillis();
				initializer.initializeGSPandCSP(globalWindow, gsp_list, csp_list);				// 计算该滑动窗口之上的GSP和CSP
				long time2 = System.currentTimeMillis();
				
				// 结果输出
				outfilenameGSP = "GSP_" + dataType + "_" + Constants.genDim() + "_" + windowSize + ".txt";
				outfilenameCSP = "CSP_" + dataType + "_" + Constants.genDim() + "_" + windowSize + ".txt";
				
				WriteFile.writeListToFile(gsp_list, outfileDir, outfilenameGSP);				// 把计算出来的GSP写入文件
				WriteFile.writeListToFile2(csp_list, outfileDir, outfilenameCSP);				// 把计算出来的CSP写入文件
				
				logger.info("-------------- Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " --------------");
				logger.info("Loading window time: " + (time1-time0) + " ms.");
				logger.info("GSP and CSP precomputing time: " + (time2-time1) + " ms; GSP size: " + gsp_list.size() + " ; CSP size: " + csp_list.size());
			
				// 数据结构清除
				globalWindow.clear();
				gsp_list.clear();
				csp_list.clear();
				
			}
			
			logger.info("\n");																	// 换行,提高日志的可读性
		}
		logger.info("\n");		
	}

}
