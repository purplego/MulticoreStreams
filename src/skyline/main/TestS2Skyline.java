package skyline.main;

import java.io.IOException;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import skyline.model.MyLogger;
import skyline.model.SkyTuple;
import skyline.model.MutaTuple;
import skyline.thread.FetchDataThread;
import skyline.thread.S2Skyline;
import skyline.util.Constants;
import skyline.util.Initializer;
import skyline.util.ParseProperties;

public class TestS2Skyline {

	/**
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
		
		ParseProperties.parseProperties(configDir + configfile);			// 读取配置文件中的参数(如果有的话,否则使用默认参数)
		
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
		logname = "S2Skyline_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "S2Skyline");
		
		// 第二重循环，指定数据的维度为2、3、4、5、6、7、8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// 第三重循环，指定全局滑动窗口长度为10M、20M、30M、40M、50M
			for(long globalWindowSize = 10000000; globalWindowSize <= 50000000; globalWindowSize += 10000000){	// 初始滑动窗口长度为10M
				Constants.GlobalWindowSize = globalWindowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				
				// 定义数据缓存buffer、全局滑动窗口、全局skyline链表和全局CSP链表
				BlockingQueue<SkyTuple> tupleBuffer = new LinkedBlockingDeque<SkyTuple>();
				LinkedList<SkyTuple> globalWindow_list = new LinkedList<SkyTuple>();
				LinkedList<SkyTuple> globalSP_list = new LinkedList<SkyTuple>();
				LinkedList<MutaTuple> candidateSP_list = new LinkedList<MutaTuple>();
				
				/*
				 * 查询初始化阶段，提前将滑动窗口充满，以降低实验过程中的setup time
				 */
				Initializer initializer = new Initializer();
				long time0 = System.currentTimeMillis();
				initializer.loadGlobalWindow(globalWindow_list, globalWindowSize, infileDir, infilename);		// 预装载全局滑动窗口
				initializer.loadGSP(globalSP_list, dataType, dim, globalWindowSize, infileDir);					// 预装载全局Skyline链表
				initializer.loadCSP(candidateSP_list, dataType, dim, globalWindowSize, infileDir);				// 预装载全局候选Skyline链表
				long time1 = System.currentTimeMillis();
				
				logger.info("-------------- Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + globalWindowSize + " --------------");
				logger.info("Precomputing time: " + (time1-time0) + " ms.");
				
				/*
				 * 查询稳定执行阶段
				 */
				Thread fetchDataThread = new Thread(new FetchDataThread(tupleBuffer, infileDir, infilename, globalWindowSize));
				Thread executorThread = new Thread(new S2Skyline(tupleBuffer, globalWindow_list, globalSP_list, candidateSP_list));
				fetchDataThread.start();
				executorThread.start();
				
				/*
				 * 系统状态信息统计
				 * 每隔10s抽样一次占用的内存大小和CPU利用率
				 */
				
				
				// 等待线程终止
				fetchDataThread.join();	
				executorThread.join();

				System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + globalWindowSize + "-------");
				
				// 清除数据结构
				tupleBuffer.clear();
				globalWindow_list.clear();
				globalSP_list.clear();
				candidateSP_list.clear();
			}
			
			logger.info("\n");
		}
		logger.info("\n");
	}
}
