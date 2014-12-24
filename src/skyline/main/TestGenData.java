package skyline.main;

import skyline.model.MyLogger;
import skyline.util.Constants;
import skyline.util.WriteFile;

public class TestGenData {

	/**
	 * 按要求生成18个300M条数据元组的原始数据文件，作为数据流的来源
	 * 数据类型为0、1、2，维度为2、3、4、5、6、7、8
	 * @param args
	 */
	public static void main(String[] args) {

		
		// 指定数据文件、日志文件的目录和文件名
		String fileDir = "/root/multicoreQueries/data/";
		Constants.DATA_DIRECTORY = fileDir;
		String filename = "";
		
		String logDir = "/root/multicoreQueries/log/";
		Constants.LOG_DIRECTORY = logDir;
		String logname = "";
		
		// 指定生成的数据规模
		long card = 300*1000000;
		Constants.CARDINALITY = card;
		
		// 第一重循环，生成0、1、2三种数据分布类型的数据
		int dataType = 0;
		Constants.DATATYPE = dataType;
		
		// 写日志
		logname = "dataGenerator_" + dataType + ".txt";
		MyLogger logger = new MyLogger(logDir+logname, "dataGenerator" + dataType);
		
		// 第二重循环，生成2~7维的数据
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
