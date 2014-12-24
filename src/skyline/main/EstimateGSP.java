package skyline.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import skyline.algorithms.BNL;
import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.FSLog;
import skyline.util.Hpreprocess;
import skyline.util.WriteFile;

public class EstimateGSP {

	/**
	 * ������ͬ���ݷֲ����͡���ͬά�ȵ������ڲ�ͬ���Ȼ��������£�
	 * ���������������ڵ�ȫ��Skyline(GSP)�����ʱ�䣬�Լ�GSP�Ĺ�ģ
	 * ��������Ϊ0��1��2��ά��Ϊ2��3��4��5��6��7���������ڳ��ȷֱ�Ϊ10K��100K��1M��10M��100M
	 * @param args
	 */
	public static void main(String[] args) {

		// ����д��־
		FSLog logger = new FSLog("./log/", "estimateGSP.txt");
		
		// ָ�������ļ���Ŀ¼���ļ���
		String infileDir = "//10.107.18.220/data/";
		String infilename = "";
		String outfileDir = "./data/";
		String outfilename = "";
		
		// ��һ��ѭ����ָ�����ݷֲ�����Ϊ0��1��2
		for(int dataType = 0; dataType <= 2; dataType ++){
			Constants.DATATYPE = dataType;
			
			// �ڶ���ѭ����ָ�����ݵ�ά��Ϊ2��3��4��5��6��7
			for(int dim = 2; dim <= 7; dim ++){
				Constants.DIMENSION = dim;
				
				// ������ѭ����ָ��ȫ�ֻ������ڳ���Ϊ10K��100K��1M��10M��100M
				long windowSize = 10000;
				while(windowSize <= 100000000){
					infilename = "rawdata_" + dataType + "_" + Constants.genCard() + Constants.genDim() + ".txt";
					LinkedList<SkyTuple> window = new LinkedList<SkyTuple>();
					
					/*
					 * ��ָ���ļ�filename�ж�ȡwindowSize�����ݲ���������window��
					 */
					File fileIn = new File(infileDir + infilename);
					try{
						FileReader fReader = new FileReader(fileIn);
						BufferedReader bReader = new BufferedReader(fReader);
						
						String str_line = "";
						Hpreprocess hp = new Hpreprocess();
						SkyTuple tuple = null;
						while(bReader.ready()){
							str_line = bReader.readLine();				// ���ļ��ж�ȡһ������
							if(str_line != null){
								tuple = hp.buildTupleFromStr(str_line);	// ������ȡ���ַ���ΪSkyTuple����
								window.offer(tuple);					// �Ѷ�ȡ�����ݼ��뵽������
								if(window.size() < windowSize){
									continue;							// ������һ��ѭ������ȡ��һ���ļ�����
								}else{	
									break;								// ����whileѭ����ֹͣ��ȡ�ļ�
								}
							}else{
								System.err.println("Error read! Null line!");
							}
						}
						bReader.close();
					}catch(Exception e){
						e.printStackTrace();
					}
					
					/*
					 * ��windowΪ���ݼ������window֮�ϵ�ȫ��Skyline����GSP
					 */
					LinkedList<SkyTuple> gsp_list = new LinkedList<SkyTuple>();
					long time0 = System.currentTimeMillis();
					gsp_list = BNL.bnlQuery(window);								// ����BNL�㷨����ȫ��Skyline
					long time1= System.currentTimeMillis();
					outfilename = "GSP_" + dataType + "_" + Constants.genDim() + "_" + windowSize + ".txt";
					WriteFile.writeListToFile(gsp_list, outfileDir, outfilename);		// ��ȫ��Skylineд���Ӧ���ļ���
					
					logger.info("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " / " + window.size() + "-------");
					logger.info("GSP computing time: " + (time1-time0) + " ms; GSP size: " + gsp_list.size());
					System.out.println("-------Parameters: dataType=" + dataType + " dim=" + dim + " windowSize=" + windowSize + " / " + window.size() + "-------");
					System.out.println("GSP computing time: " + (time1-time0) + " ms; GSP size: " + gsp_list.size());
					
					// ��windowSize������
					windowSize *= 10;
				}
			}
			
		}
	}

}
