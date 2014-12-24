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
	 * ����̴߳�Buffer�л�ȡ���ݣ����оֲ������ٹ鲢
	 * (Ϊ�˲��л���ÿ��PsExecutor�̷ֱ߳𵽲�ͬ��tupleBufferȡ����)
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws SecurityException, IOException, InterruptedException {

		// ָ�������ļ��������ļ�����־�ļ���Ŀ¼���ļ���
		String configDir = "/root/multicoreQueries/config/";
		Constants.CONFIG_DIRECTORY = configDir;
		String configfile = "args.properties";
		
		ParseProperties.parseProperties(configDir + configfile);	// ��ȡ�����ļ��еĲ���(����еĻ�,����ʹ��Ĭ�ϲ���)
		
		String infileDir = "/root/multicoreQueries/data/";
		Constants.DATA_DIRECTORY = infileDir;
		String infilename = "";
		
		String logDir = "/root/multicoreQueries/log/";
		Constants.LOG_DIRECTORY = logDir;
		String logname = "";
		
		// ��һ��ѭ����ָ�����ݷֲ�����Ϊ0��1��2
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// ����д��־
		logname = "PsSkyline_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "PsSkyline");
		
		// �ڶ���ѭ����ָ�����ݵ�ά��Ϊ2��3��4��5��6��7��8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// ������ѭ����ָ��ȫ�ֻ������ڳ���Ϊ10M��20M��30M��40M��50M
			for(long windowSize = 10000000; windowSize <= 50000000; windowSize += 10000000){	// ��ʼ�������ڳ���Ϊ10M
				Constants.GlobalWindowSize = windowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				
				// ������ѭ����ָ�����������߳���Ŀ���ֱ�Ϊ1��2��4��8��16��32��64��128��256��512��1024
				for(int threadNum = 1; threadNum <= 1024; threadNum *= 2){
					Constants.NumPsExecutor = threadNum;
					Constants.LocalWindowSize = windowSize/threadNum;	// �ֲ��������ڵĳ��� = ȫ�ֻ������ڳ��� / ���������߳���Ŀ
					
					// ���������ύ���С����ݻ���buffer������ֲ��������ڡ�����ֲ�Skyline�����Լ����subGSP��subCSP����
					BlockingQueue<ComboTuple> submitQueue = new LinkedBlockingDeque<ComboTuple>();				// PsMerger�˵ģ�����ύ���ŶӶ���
					ArrayList<BlockingQueue<SkyTuple>> tupleBuffer = new ArrayList<BlockingQueue<SkyTuple>>();	// ��PsExecutorȡ���ݵĵط�
					
					ArrayList<LinkedList<SkyTuple>> localWindows = new ArrayList<LinkedList<SkyTuple>>();		// �ֲ���������
					ArrayList<LinkedList<SkyTuple>> localSPs = new ArrayList<LinkedList<SkyTuple>>();			// �ֲ�Skyline�����
					ArrayList<LinkedList<SkyTuple>> subGSPs = new ArrayList<LinkedList<SkyTuple>>();			// ÿ��PsExecutor�̶߳�Ӧһ��subGSP����
					ArrayList<LinkedList<MutaTuple>> subCSPs = new ArrayList<LinkedList<MutaTuple>>();			// ÿ��PsExecutor�̶߳�Ӧһ��subCSP����
					
					for(int i=0; i<threadNum; i++){
						localWindows.add(new LinkedList<SkyTuple>());
						localSPs.add(new LinkedList<SkyTuple>());
						subGSPs.add(new LinkedList<SkyTuple>());
					}
					for(int i=0; i<Constants.NumSUBCSP; i++){
						subCSPs.add(new LinkedList<MutaTuple>());
					}
					/*
					 * ��ѯ��ʼ���׶Σ���ǰ���������ڳ������Խ���ʵ������е�setup time
					 */
					Initializer initializer = new Initializer();
					long time0 = System.currentTimeMillis();
					initializer.loadLocalWindow(localWindows, windowSize, infileDir, infilename, threadNum);			// װ�ؾֲ���������
					long time1 = System.currentTimeMillis();
					initializer.loadGSPForPsSkyline(localSPs, dataType, dim, threadNum, windowSize, infileDir);			// �ó�ʼ��GSP�����ʼ�ľֲ�Skyline
					long time2 = System.currentTimeMillis();
					initializer.loadGSPForPsSkyline(subGSPs, dataType, dim, threadNum, windowSize, infileDir);			// װ�س�ʼ��GSP�ļ�
					initializer.loadCSPForParallelSkyline(subCSPs, dataType, dim, threadNum, windowSize, infileDir);	// װ�س�ʼ��CSP�ļ�
					long time3 = System.currentTimeMillis();
					
					logger.info("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " @threadNum=" + threadNum + "-------");
					logger.info("Precomputing time: " + (time3-time0) + " ms. " +
								"Loading local windows time: " + (time1-time0) + " ms. " +
								"Loading local Skyline time: " + (time2-time1) + " ms. " +
								"Loading GSP and CSP time: " + (time3-time2) + " ms.");
					
					/*
					 * ��ѯ�ȶ�ִ�н׶�
					 */
					// ���ļ��ж�ȡ���ݲ�����buffer����
					Thread fetchDataThread = new Thread(new FetchDataForPsSkylineThread(tupleBuffer, threadNum, infileDir, infilename, windowSize));
					fetchDataThread.start();
					
					// ����threadNum��ִ���߳�
					Thread[] executorThread = new Thread[threadNum];
					for(int i=0; i<threadNum; i++){
						executorThread[i] = new Thread(new PsExecutor(tupleBuffer.get(i), submitQueue, localWindows.get(i), localSPs.get(i)));
						executorThread[i].start();
					}
					
					// ����1��merger�߳�
					Thread mergerThread = new Thread(new PsMerger(submitQueue, subGSPs, subCSPs));
					mergerThread.start();
					
					/*
					 * ϵͳ״̬��Ϣͳ��
					 * ÿ��10s����һ��ռ�õ��ڴ��С��CPU������
					 */
					
					
					// �ȴ��߳���ֹ
					fetchDataThread.join();
					mergerThread.join();
					for(int i=0; i<threadNum; i++){
						executorThread[i].join();
					}
					
					System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " threadNum=" + threadNum + "-------");
				
					// ������ݽṹ
					submitQueue.clear();
				}
				logger.info("\n");
			}
			logger.info("\n");
		}
		logger.info("\n");
	}
}
