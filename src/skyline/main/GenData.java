package skyline.main;

import java.util.LinkedList;

import skyline.algorithms.BNL;
import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.WriteFile;

public class GenData {

	/**
	 * 按要求生成一个较大的数据文件，作为数据流的来源
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// 指定生成的数据类型、维度和规模
		int dataType = Constants.DATATYPE;
		int dim = Constants.DIMENSION;
		long card = Constants.CARDINALITY;
		
		// 指定数据文件的目录和文件名
		String fileDir = Constants.DATA_DIRECTORY;
		String filename = "";
		switch(dataType){
		case 0:
			filename = "rawdata_" + "indep_" + Constants.genCard() + Constants.genDim() + ".txt";
			break;
		case 1:
			filename = "rawdata_" + "corr_" + Constants.genCard() + Constants.genDim() + ".txt";
			break;
		case 2:
			filename = "rawdata_" + "anti_" + Constants.genCard() + Constants.genDim() + ".txt";
			break;
		default:// 默认为0，即独立分布的数据
			filename = "rawdata_" + "indep_" + Constants.genCard() + Constants.genDim() + ".txt";
			break;
		}
		
		// 生成数据并写入文件
		long time0 = System.currentTimeMillis();
		WriteFile.writeRandomToFile(fileDir, filename, card, dim, dataType);
		long time1 = System.currentTimeMillis();
		System.out.println("Finish to write random to file: " + (time1 - time0) + " ms.");
		
		//为生成的测试数据计算skyline结果集
		LinkedList<SkyTuple> spList = BNL.bnlQuery(fileDir, filename);
		long time2 = System.currentTimeMillis();
		System.out.println("Global skyline computing time: " + (time2 - time1) + " ms. " + "GSP size: " + spList.size());
		WriteFile.writeListToFile(spList, fileDir, "globalskyline_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".csv");
		System.out.println("Time to write global skylines to file: " + (System.currentTimeMillis() - time2) + " ms.");
		
	}

}
