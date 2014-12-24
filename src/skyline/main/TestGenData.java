package skyline.main;

import skyline.model.MyLogger;
import skyline.util.Constants;
import skyline.util.WriteFile;

public class TestGenData {

	/**
	 * ��Ҫ������18��300M������Ԫ���ԭʼ�����ļ�����Ϊ����������Դ
	 * ��������Ϊ0��1��2��ά��Ϊ2��3��4��5��6��7��8
	 * @param args
	 */
	public static void main(String[] args) {

		
		// ָ�������ļ�����־�ļ���Ŀ¼���ļ���
		String fileDir = "/root/multicoreQueries/data/";
		Constants.DATA_DIRECTORY = fileDir;
		String filename = "";
		
		String logDir = "/root/multicoreQueries/log/";
		Constants.LOG_DIRECTORY = logDir;
		String logname = "";
		
		// ָ�����ɵ����ݹ�ģ
		long card = 300*1000000;
		Constants.CARDINALITY = card;
		
		// ��һ��ѭ��������0��1��2�������ݷֲ����͵�����
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// д��־
		logname = "dataGenerator_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir+logname, "dataGenerator" + dataType);
		
		// �ڶ���ѭ��������2~7ά������
		for(int dim = 2; dim <= 8; dim ++){
			Constants.DIMENSION = dim;
			
			logger.info("--------------------- dataType: " + dataType + " dim: " + dim + " cardinality: " + card + " ---------------------");
			filename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
			
			long time0 = System.currentTimeMillis();
			WriteFile.writeRandomToFile(fileDir, filename, card, dim, dataType);
			long time1 = System.currentTimeMillis();
			
			logger.info("Finish to generate and write rawdata file: " + filename + "; time: " + (time1-time0)/1000 + " s.");
		}
	}

}
