package skyline.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import skyline.model.MutaTuple;
import skyline.model.MyLogger;
import skyline.model.SkyTuple;
import skyline.thread.FetchDataThread;
import skyline.thread.SharedSkyline;
import skyline.util.Constants;
import skyline.util.Initializer;
import skyline.util.Tools;

public class TestSharedSkyline {

	/**
	 * 基本处理流程同S2Skyline，但是在GSP和CSP中的支配关系测试时，采用并行化的方法
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws SecurityException, IOException {
		// TODO Auto-generated method stub

		// 指定配置文件、数据文件、日志文件的目录和文件名
		String configDir = "/root/multicoreQueries/config/";
		String configfile = "args";
		Tools.parseArgs(configDir, configfile);					// 读取配置文件中的参数（如果有的话，否则使用默认参数）
		
		String infileDir = "/root/multicoreQueries/data/";
		String infilename = "";
		
		String logDir = "/root/multicoreQueries/log/";
		String logname = "";
		
		Constants.DATA_DIRECTORY = infileDir;
		Constants.LOG_DIRECTORY = logDir;
		Constants.CONFIG_DIRECTORY = configDir;
		
		// 第一重循环，指定数据分布类型为0、1、2
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// 用于写日志
		logname = "SharedSkyline_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "S2Skyline");
		
		// 第二重循环，指定数据的维度为2、3、4、5、6、7、8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// 第三重循环，指定全局滑动窗口长度为10M、20M、30M、40M、50M
			for(long windowSize = 10000000; windowSize <= 50000000; windowSize += 10000000){// 初始滑动窗口长度为10M
				Constants.GlobalWindowSize = windowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				int numSubGSP = Constants.NumSUBGSP;
				int numSubCSP = Constants.NumSUBCSP;
				
				// 定义数据缓存buffer、全局滑动窗口、全局skyline链表(组织为多个子表)和全局CSP链表(组织为多个子表)
				BlockingQueue<SkyTuple> tupleBuffer = new LinkedBlockingDeque<SkyTuple>();
				LinkedList<SkyTuple> globalSlidingWindow = new LinkedList<SkyTuple>();
				ArrayList<LinkedList<SkyTuple>> subGSPs = new ArrayList<LinkedList<SkyTuple>>();
				ArrayList<LinkedList<MutaTuple>> subCSPs = new ArrayList<LinkedList<MutaTuple>>();
				
				/*
				 * 查询初始化阶段，提前将滑动窗口充满，以降低实验过程中的setup time
				 */
				Initializer initializer = new Initializer();
				long time0 = System.currentTimeMillis();
				initializer.loadGlobalWindow(globalSlidingWindow, windowSize, infileDir, infilename);			// 预装载全局滑动窗口
				initializer.loadGSPForSharedSkyline(subGSPs, dataType, dim, numSubGSP, windowSize, infileDir);	// 预装载全局Skyline链表
				initializer.loadCSPForParallelSkyline(subCSPs, dataType, dim, numSubCSP, windowSize, infileDir);	// 预装载全局候选Skyline链表
				long time1 = System.currentTimeMillis();
				
				logger.info("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + "-------");
				logger.info("Precomputing time: " + (time1-time0) + " ms.");
				
				/*
				 * 查询稳定执行阶段
				 */
				Thread fetchDataThread = new Thread(new FetchDataThread(tupleBuffer, infileDir, infilename, windowSize));
				Thread executorThread = new Thread(new SharedSkyline(tupleBuffer, globalSlidingWindow, subGSPs, subCSPs));
				fetchDataThread.start();
				executorThread.start();
				
				try {
					fetchDataThread.join();	// 等待该线程终止
					executorThread.join();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + "-------");
			}
		}
	}

}
