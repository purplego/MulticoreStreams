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
	 * ������������ͬS2Skyline��������GSP��CSP�е�֧���ϵ����ʱ�����ò��л��ķ���
	 * @param args
	 * @throws IOException 
	 * @throws SecurityException 
	 */
	public static void main(String[] args) throws SecurityException, IOException {
		// TODO Auto-generated method stub

		// ָ�������ļ��������ļ�����־�ļ���Ŀ¼���ļ���
		String configDir = "/root/multicoreQueries/config/";
		String configfile = "args";
		Tools.parseArgs(configDir, configfile);					// ��ȡ�����ļ��еĲ���������еĻ�������ʹ��Ĭ�ϲ�����
		
		String infileDir = "/root/multicoreQueries/data/";
		String infilename = "";
		
		String logDir = "/root/multicoreQueries/log/";
		String logname = "";
		
		Constants.DATA_DIRECTORY = infileDir;
		Constants.LOG_DIRECTORY = logDir;
		Constants.CONFIG_DIRECTORY = configDir;
		
		// ��һ��ѭ����ָ�����ݷֲ�����Ϊ0��1��2
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// ����д��־
		logname = "SharedSkyline_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "S2Skyline");
		
		// �ڶ���ѭ����ָ�����ݵ�ά��Ϊ2��3��4��5��6��7��8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// ������ѭ����ָ��ȫ�ֻ������ڳ���Ϊ10M��20M��30M��40M��50M
			for(long windowSize = 10000000; windowSize <= 50000000; windowSize += 10000000){// ��ʼ�������ڳ���Ϊ10M
				Constants.GlobalWindowSize = windowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				int numSubGSP = Constants.NumSUBGSP;
				int numSubCSP = Constants.NumSUBCSP;
				
				// �������ݻ���buffer��ȫ�ֻ������ڡ�ȫ��skyline����(��֯Ϊ����ӱ�)��ȫ��CSP����(��֯Ϊ����ӱ�)
				BlockingQueue<SkyTuple> tupleBuffer = new LinkedBlockingDeque<SkyTuple>();
				LinkedList<SkyTuple> globalSlidingWindow = new LinkedList<SkyTuple>();
				ArrayList<LinkedList<SkyTuple>> subGSPs = new ArrayList<LinkedList<SkyTuple>>();
				ArrayList<LinkedList<MutaTuple>> subCSPs = new ArrayList<LinkedList<MutaTuple>>();
				
				/*
				 * ��ѯ��ʼ���׶Σ���ǰ���������ڳ������Խ���ʵ������е�setup time
				 */
				Initializer initializer = new Initializer();
				long time0 = System.currentTimeMillis();
				initializer.loadGlobalWindow(globalSlidingWindow, windowSize, infileDir, infilename);			// Ԥװ��ȫ�ֻ�������
				initializer.loadGSPForSharedSkyline(subGSPs, dataType, dim, numSubGSP, windowSize, infileDir);	// Ԥװ��ȫ��Skyline����
				initializer.loadCSPForParallelSkyline(subCSPs, dataType, dim, numSubCSP, windowSize, infileDir);	// Ԥװ��ȫ�ֺ�ѡSkyline����
				long time1 = System.currentTimeMillis();
				
				logger.info("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + "-------");
				logger.info("Precomputing time: " + (time1-time0) + " ms.");
				
				/*
				 * ��ѯ�ȶ�ִ�н׶�
				 */
				Thread fetchDataThread = new Thread(new FetchDataThread(tupleBuffer, infileDir, infilename, windowSize));
				Thread executorThread = new Thread(new SharedSkyline(tupleBuffer, globalSlidingWindow, subGSPs, subCSPs));
				fetchDataThread.start();
				executorThread.start();
				
				try {
					fetchDataThread.join();	// �ȴ����߳���ֹ
					executorThread.join();
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + "-------");
			}
		}
	}

}
