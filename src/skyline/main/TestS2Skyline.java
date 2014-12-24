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
		
		// ָ�������ļ��������ļ�����־�ļ���Ŀ¼���ļ���
		String configDir = "/root/multicoreQueries/config/";
		Constants.CONFIG_DIRECTORY = configDir;
		String configfile = "args.properties";
		
		ParseProperties.parseProperties(configDir + configfile);			// ��ȡ�����ļ��еĲ���(����еĻ�,����ʹ��Ĭ�ϲ���)
		
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
		logname = "S2Skyline_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "S2Skyline");
		
		// �ڶ���ѭ����ָ�����ݵ�ά��Ϊ2��3��4��5��6��7��8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// ������ѭ����ָ��ȫ�ֻ������ڳ���Ϊ10M��20M��30M��40M��50M
			for(long globalWindowSize = 10000000; globalWindowSize <= 50000000; globalWindowSize += 10000000){	// ��ʼ�������ڳ���Ϊ10M
				Constants.GlobalWindowSize = globalWindowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				
				// �������ݻ���buffer��ȫ�ֻ������ڡ�ȫ��skyline�����ȫ��CSP����
				BlockingQueue<SkyTuple> tupleBuffer = new LinkedBlockingDeque<SkyTuple>();
				LinkedList<SkyTuple> globalWindow_list = new LinkedList<SkyTuple>();
				LinkedList<SkyTuple> globalSP_list = new LinkedList<SkyTuple>();
				LinkedList<MutaTuple> candidateSP_list = new LinkedList<MutaTuple>();
				
				/*
				 * ��ѯ��ʼ���׶Σ���ǰ���������ڳ������Խ���ʵ������е�setup time
				 */
				Initializer initializer = new Initializer();
				long time0 = System.currentTimeMillis();
				initializer.loadGlobalWindow(globalWindow_list, globalWindowSize, infileDir, infilename);		// Ԥװ��ȫ�ֻ�������
				initializer.loadGSP(globalSP_list, dataType, dim, globalWindowSize, infileDir);					// Ԥװ��ȫ��Skyline����
				initializer.loadCSP(candidateSP_list, dataType, dim, globalWindowSize, infileDir);				// Ԥװ��ȫ�ֺ�ѡSkyline����
				long time1 = System.currentTimeMillis();
				
				logger.info("-------------- Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + globalWindowSize + " --------------");
				logger.info("Precomputing time: " + (time1-time0) + " ms.");
				
				/*
				 * ��ѯ�ȶ�ִ�н׶�
				 */
				Thread fetchDataThread = new Thread(new FetchDataThread(tupleBuffer, infileDir, infilename, globalWindowSize));
				Thread executorThread = new Thread(new S2Skyline(tupleBuffer, globalWindow_list, globalSP_list, candidateSP_list));
				fetchDataThread.start();
				executorThread.start();
				
				/*
				 * ϵͳ״̬��Ϣͳ��
				 * ÿ��10s����һ��ռ�õ��ڴ��С��CPU������
				 */
				
				
				// �ȴ��߳���ֹ
				fetchDataThread.join();	
				executorThread.join();

				System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + globalWindowSize + "-------");
				
				// ������ݽṹ
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
