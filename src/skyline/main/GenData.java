package skyline.main;

import java.util.LinkedList;

import skyline.algorithms.BNL;
import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.WriteFile;

public class GenData {

	/**
	 * ��Ҫ������һ���ϴ�������ļ�����Ϊ����������Դ
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// ָ�����ɵ��������͡�ά�Ⱥ͹�ģ
		int dataType = Constants.DATATYPE;
		int dim = Constants.DIMENSION;
		long card = Constants.CARDINALITY;
		
		// ָ�������ļ���Ŀ¼���ļ���
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
		default:// Ĭ��Ϊ0���������ֲ�������
			filename = "rawdata_" + "indep_" + Constants.genCard() + Constants.genDim() + ".txt";
			break;
		}
		
		// �������ݲ�д���ļ�
		long time0 = System.currentTimeMillis();
		WriteFile.writeRandomToFile(fileDir, filename, card, dim, dataType);
		long time1 = System.currentTimeMillis();
		System.out.println("Finish to write random to file: " + (time1 - time0) + " ms.");
		
		//Ϊ���ɵĲ������ݼ���skyline�����
		LinkedList<SkyTuple> spList = BNL.bnlQuery(fileDir, filename);
		long time2 = System.currentTimeMillis();
		System.out.println("Global skyline computing time: " + (time2 - time1) + " ms. " + "GSP size: " + spList.size());
		WriteFile.writeListToFile(spList, fileDir, "globalskyline_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".csv");
		System.out.println("Time to write global skylines to file: " + (System.currentTimeMillis() - time2) + " ms.");
		
	}

}
