package skyline.main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import skyline.model.ComboTuple;
import skyline.model.MutaTuple;
import skyline.model.LogFormat;
import skyline.model.MyLogger;
import skyline.model.SkyTuple;
import skyline.thread.FetchDataForPsSkylineThread;
import skyline.thread.PsExecutor;
import skyline.thread.PsMerger;
import skyline.util.Constants;
import skyline.util.Initializer;
import skyline.util.ParseProperties;
import skyline.util.Tools;

public class TestPsSkyline {

	/**
	 * 多个线程从Buffer中获取数据，进行局部处理再归并
	 * (为了并行化，每个PsExecutor线程分别到不同的tupleBuffer取数据)
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws SecurityException, IOException, InterruptedException {

		// 指定配置文件、数据文件、日志文件的目录和文件名
		String configDir = "/root/multicoreQueries/config/";
		Constants.CONFIG_DIRECTORY = configDir;
		String configfile = "args.properties";
		
		ParseProperties.parseProperties(configDir + configfile);	// 读取配置文件中的参数(如果有的话,否则使用默认参数)
		
		String infileDir = "/root/multicoreQueries/data/";
		Constants.DATA_DIRECTORY = infileDir;
		String infilename = "";
		
		String logDir = "/root/multicoreQueries/log/";
		Constants.LOG_DIRECTORY = logDir;
		String logname = "";
		
		// 第一重循环，指定数据分布类型为0、1、2
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// 用于写日志
		logname = "PsSkyline_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "PsSkyline");
		
		// 第二重循环，指定数据的维度为2、3、4、5、6、7、8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// 第三重循环，指定全局滑动窗口长度为10M、20M、30M、40M、50M
			for(long windowSize = 10000000; windowSize <= 50000000; windowSize += 10000000){	// 初始滑动窗口长度为10M
				Constants.GlobalWindowSize = windowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				
				// 第四重循环，指定参与计算的线程数目，分别为1、2、4、8、16、32、64、128、256、512、1024
				for(int threadNum = 1; threadNum <= 1024; threadNum *= 2){
					Constants.NumPsExecutor = threadNum;
					Constants.LocalWindowSize = windowSize/threadNum;	// 局部滑动窗口的长度 = 全局滑动窗口长度 / 参与计算的线程数目
					
					// 定义数据提交队列、数据缓存buffer、多个局部滑动窗口、多个局部Skyline链表、以及多个subGSP和subCSP链表
					BlockingQueue<ComboTuple> submitQueue = new LinkedBlockingDeque<ComboTuple>();				// PsMerger端的（结果提交）排队队列
					ArrayList<BlockingQueue<SkyTuple>> tupleBuffer = new ArrayList<BlockingQueue<SkyTuple>>();	// 各PsExecutor取数据的地方
					
					ArrayList<LinkedList<SkyTuple>> localWindows = new ArrayList<LinkedList<SkyTuple>>();		// 局部滑动窗口
					ArrayList<LinkedList<SkyTuple>> localSPs = new ArrayList<LinkedList<SkyTuple>>();			// 局部Skyline结果集
					ArrayList<LinkedList<SkyTuple>> subGSPs = new ArrayList<LinkedList<SkyTuple>>();			// 每个PsExecutor线程对应一个subGSP链表
					ArrayList<LinkedList<MutaTuple>> subCSPs = new ArrayList<LinkedList<MutaTuple>>();			// 每个PsExecutor线程对应一个subCSP链表
					
					for(int i=0; i<threadNum; i++){
						localWindows.add(new LinkedList<SkyTuple>());
						localSPs.add(new LinkedList<SkyTuple>());
						subGSPs.add(new LinkedList<SkyTuple>());
					}
					for(int i=0; i<Constants.NumSUBCSP; i++){
						subCSPs.add(new LinkedList<MutaTuple>());
					}
					/*
					 * 查询初始化阶段，提前将滑动窗口充满，以降低实验过程中的setup time
					 */
					Initializer initializer = new Initializer();
					long time0 = System.currentTimeMillis();
					initializer.loadLocalWindow(localWindows, windowSize, infileDir, infilename, threadNum);			// 装载局部滑动窗口
					long time1 = System.currentTimeMillis();
					initializer.loadGSPForPsSkyline(localSPs, dataType, dim, threadNum, windowSize, infileDir);			// 用初始的GSP替代初始的局部Skyline
					long time2 = System.currentTimeMillis();
					initializer.loadGSPForPsSkyline(subGSPs, dataType, dim, threadNum, windowSize, infileDir);			// 装载初始的GSP文件
					initializer.loadCSPForParallelSkyline(subCSPs, dataType, dim, threadNum, windowSize, infileDir);	// 装载初始的CSP文件
					long time3 = System.currentTimeMillis();
					
					logger.info("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " @threadNum=" + threadNum + "-------");
					logger.info("Precomputing time: " + (time3-time0) + " ms. " +
								"Loading local windows time: " + (time1-time0) + " ms. " +
								"Loading local Skyline time: " + (time2-time1) + " ms. " +
								"Loading GSP and CSP time: " + (time3-time2) + " ms.");
					
					/*
					 * 查询稳定执行阶段
					 */
					// 从文件中读取数据并存入buffer数组
					Thread fetchDataThread = new Thread(new FetchDataForPsSkylineThread(tupleBuffer, threadNum, infileDir, infilename, windowSize));
					fetchDataThread.start();
					
					// 生成threadNum个执行线程
					Thread[] executorThread = new Thread[threadNum];
					for(int i=0; i<threadNum; i++){
						executorThread[i] = new Thread(new PsExecutor(tupleBuffer.get(i), submitQueue, localWindows.get(i), localSPs.get(i)));
						executorThread[i].start();
					}
					
					// 生成1个merger线程
					Thread mergerThread = new Thread(new PsMerger(submitQueue, subGSPs, subCSPs));
					mergerThread.start();
					
					/*
					 * 系统状态信息统计
					 * 每隔10s抽样一次占用的内存大小和CPU利用率
					 */
					
					
					// 等待线程终止
					fetchDataThread.join();
					mergerThread.join();
					for(int i=0; i<threadNum; i++){
						executorThread[i].join();
					}
					
					System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " threadNum=" + threadNum + "-------");
				
					// 清除数据结构
					submitQueue.clear();
				}
				logger.info("\n");
			}
			logger.info("\n");
		}
		logger.info("\n");
	}
}
