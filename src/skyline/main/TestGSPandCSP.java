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
	 * Ԥ�ȼ������ͬ���ݷֲ����͡���ͬά�ȵ������ڲ�ͬ���������µ�GSP��CSP��
	 * ����������������ʱ�䣬�Լ�GSP��CSP�Ĺ�ģ
	 * ��������Ϊ0��1��2��ά��Ϊ2��3��4��5��6��7��8���������ڳ��ȷֱ�Ϊ10M��20M��30M��40M��50M
	 * @param args
	 */
	public static void main(String[] args) {

		// ָ�����������ļ�����������ļ�����־�ļ���Ŀ¼���ļ���
		String infileDir = "/root/multicoreQueries/data/";
		Constants.DATA_DIRECTORY = infileDir;
		String infilename = "";
		
		String outfileDir = infileDir;
		String outfilenameGSP = "";
		String outfilenameCSP = "";
		
		String logDir = "/root/multicoreQueries/log/";
		Constants.LOG_DIRECTORY = logDir;
		String logname = "";
		
		// ��һ��ѭ����ָ�����ݷֲ�����Ϊ0��1��2
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// ����д��־
		logname = "GSPCSP_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir + logname, "GSPCSP" + dataType);
			
		// �ڶ���ѭ����ָ�����ݵ�ά��Ϊ2��3��4��5��6��7��8
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			// ������ѭ����ָ��ȫ�ֻ������ڳ���Ϊ10M��20M��30M��40M��50M
					
			for(long windowSize = 10000000; windowSize <= 50000000; windowSize += 10000000){	// ��ʼ�������ڳ���Ϊ10M
//			for(long windowSize = 1000; windowSize <= 1000000; windowSize *= 10){				// ��ʼ�������ڳ���Ϊ1K
				Constants.GlobalWindowSize = windowSize;
				
				infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
				
				// ����ȫ�ֻ�����������ȫ��Skyline�����ȫ�ֺ�ѡSkyline����
				LinkedList<SkyTuple> globalWindow = new LinkedList<SkyTuple>();
				LinkedList<SkyTuple> gsp_list = new LinkedList<SkyTuple>();
				LinkedList<MutaTuple> csp_list = new LinkedList<MutaTuple>();
				
				// װ�ش��ںͼ���GSP��CSP
				Initializer initializer = new Initializer();
				long time0 = System.currentTimeMillis();
				initializer.loadGlobalWindow(globalWindow, windowSize, infileDir, infilename);	// ��������������
				long time1 = System.currentTimeMillis();
				initializer.initializeGSPandCSP(globalWindow, gsp_list, csp_list);				// ����û�������֮�ϵ�GSP��CSP
				long time2 = System.currentTimeMillis();
				
				// ������
				outfilenameGSP = "GSP_" + dataType + "_" + Constants.genDim() + "_" + windowSize + ".txt";
				outfilenameCSP = "CSP_" + dataType + "_" + Constants.genDim() + "_" + windowSize + ".txt";
				
				WriteFile.writeListToFile(gsp_list, outfileDir, outfilenameGSP);				// �Ѽ��������GSPд���ļ�
				WriteFile.writeListToFile2(csp_list, outfileDir, outfilenameCSP);				// �Ѽ��������CSPд���ļ�
				
				logger.info("-------------- Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " --------------");
				logger.info("Loading window time: " + (time1-time0) + " ms.");
				logger.info("GSP and CSP precomputing time: " + (time2-time1) + " ms; GSP size: " + gsp_list.size() + " ; CSP size: " + csp_list.size());
			
				// ���ݽṹ���
				globalWindow.clear();
				gsp_list.clear();
				csp_list.clear();
				
			}
			
			logger.info("\n");																	// ����,�����־�Ŀɶ���
		}
		logger.info("\n");		
	}

}
