package skyline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import skyline.algorithms.IsDominate;
import skyline.model.SkyTuple;
import skyline.model.MutaTuple;

/**
 * Initializer�ฺ����ɳ���ִ�е�һ�г�ʼ������������
 * (1)Ԥ�ȼ������ͬ�������͵������ڲ�ͬ�������ڹ�ģ�µ�globalSP��candidateSP
 * (2)����ʼ֮ǰ��ȡ���ݽ�������������,����ȡglobalSP��candidateSP����������Ӧ ���ݽṹ
 * 
 * @author Administrator
 *
 */
public class Initializer {

	/**
	 * Initializer��Ĳ��������Ĺ��캯��
	 */
	public Initializer(){}
	
	/**
	 * ��Ԫ��װ�ص�ȫ�ֻ���������: Ѹ�ٴ�ָ���ļ��ж�ȡָ����Ŀ�����ݽ�������������
	 * 
	 * @param globalWindow_list			ָ��ȫ�ֻ�������
	 * @param globalWindowSize			ָ����ȫ�ֻ������ڹ�ģ
	 */
	public void loadGlobalWindow(LinkedList<SkyTuple> globalWindow_list, long globalWindowSize, 
			String fileDir, String filename){
		
		File fileIn = new File(fileDir + filename);
		
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			long countLine = 0;										// ���ƴӶ�ȡ���ݶ�����
			Hpreprocess hp = new Hpreprocess();						// ����Ԥ����
			SkyTuple tuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				if(countLine < globalWindowSize){
					str_line = bReader.readLine();
					if(str_line != null){
						tuple = hp.buildTupleFromStr(str_line);
						globalWindow_list.offer(tuple);				// ����ȡ�����ݲ���globalWindow_list��
						
						countLine ++;
					}
				}else{
					break;											// �������ڹ���������������ѭ��
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	/**
	 * ���㷨����ʼ��������������globalSkylines��candidateSkylines, ����globalSP_list��candidateSP_list�ĳ�ʼ��.
	 * globalSP_list��candidateSP_list��������������SkyTuple��Ȼ���������֯�ģ���id���Ԫ���������ͷ
	 * 
	 * �Ի�������globalWindowΪ�������skyline����,������globalSP_list��; �ѷ���ָ��������Ԫ��ŵ�candidateSP_list��
	 * 
	 * ���globalSP_list��Ϊ�գ���newTupleҪ�����globalSP_list�е����ݱȽϣ���tail���£���head���ɣ��Ƚϣ�
	 * ��newTuple��֧�䣬��ֱ�Ӱ�newTuple����candidateSP_list������latestDominateIDΪ֧������globalSP_listԪ�飬�ȽϽ���
	 * ��newTuple֧��globalSP_list�е�Ԫ��tempTuple�����tempTuple��globalSP_list���Ƴ���������globalSP_list����һԪ��Ƚ�
	 * ÿ����Ԫ�����globalSP_list��candidateSP_listʱ����Ӧɾ��candidateSP_list�б�newTuple֧���Ԫ������latestDominateID
	 * 
	 * @param globalWindow_list 			�Ѿ�Ԥװ�غ�
	 * @param globalSP_list
	 * @param candidateSP_list
	 */
	public void initializeGSPandCSP(LinkedList<SkyTuple> globalWindow_list, 
			LinkedList<SkyTuple> globalSP_list, LinkedList<MutaTuple> candidateSP_list){
		
		for(SkyTuple newTuple: globalWindow_list){								// �ӱ�ͷ��ʼ��idС��Ԫ�飩
			
			if(!globalSP_list.isEmpty()){
				
				int index = 0;													// ��globalSP_list��head(id�ϴ�)��ʼ�Ƚ�
				
				while(true){
					SkyTuple tempTuple = globalSP_list.get(index);				// ȡglobalSP_list��head(id�ϴ�)��Ԫ��tempTuple��newTuple�Ƚ�
					int isDominate = IsDominate.dominateBetweenTuples(tempTuple, newTuple);
					
					//��tempTuple֧��newTuple����newTupleӦ����candidateSP_list
					if(isDominate == 0){
						insertIntoCSP(candidateSP_list, newTuple, tempTuple.getTupleID());
						break;													// ����whileѭ����ȡ��һ��newTuple
					}
					// ��tempTuple��newTuple֧�䣬���globalSP_list���Ƴ�tempTuple��newTuple��globalSP_list�е���һ��tempTuple�Ƚ�
					else if(isDominate == 1){
						globalSP_list.remove(index);
					}
					// ��tempTuple��newTuple����֧�䣬��newTupleӦ��globalSP_list�е���һ��tempTuple�Ƚ�
					else if(isDominate == 2){
						index ++;
					}
					
					// ����Ƚϵ�globalSP_list�����һ�������newTuple��Ȼδ��֧�䣬������������ı�β
					if(index == globalSP_list.size()){				
						globalSP_list.offerFirst(newTuple);						// ��Ԫ�鶼��������ͷ����һ��������֯globalSkylines����
						updateCSP(candidateSP_list, newTuple);
						
						break;													// ����whileѭ����ȡ��һ��newTuple
					}
				}
			}else{
				globalSP_list.offerFirst(newTuple);
			}
		}
	}
	
	/**
	 * ��candidateSP_list�в���һ��Ԫ��newTuple(������globalSP_list): 
	 * ���ȼ��newTuple��candidateSP_list�и�Ԫ���֧���ϵ��ɾ����newTuple֧���Ԫ�飬�Լ�����Ҫ��ʱ�����newTuple��latestDominateId
	 * CSP������SkyTuple��Ȼ���˳����֯
	 * 
	 * @param newTuple
	 * @param latestDominateID
	 */
	public void insertIntoCSP(LinkedList<MutaTuple> candidateSP_list, SkyTuple newTuple, long latestDominateID){
		
		if(!candidateSP_list.isEmpty()){
			Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
			while(iter.hasNext()){
				SkyTuple tmp = iter.next().getSkyTuple();
				int flag = IsDominate.dominateBetweenTuples(newTuple, tmp);
				
				// ���newTuple֧��tmp, ���candidateSP_list��ɾ��tmp,�Ƚ�candidateSP_list�е���һ��Ԫ��
				if(flag == 0){
					iter.remove();
				}
				// ���tmp֧��newTuple,�����newTuple��latestDominateId, �������Ƚ�(candidateSP_list�������)
				else if(flag == 1){
					if(latestDominateID < tmp.getTupleID())
						latestDominateID = tmp.getTupleID();
					break;
				}
				// ���newTuple��tmp����֧��,��Ƚ�candidateSP_list�е���һ��Ԫ��
				else if(flag == 2){
					continue;
				}
			}
		}
		candidateSP_list.offerFirst(new MutaTuple(newTuple, latestDominateID));				// ��Ԫ��newTuple����CSP������ͷ
	}
	
	/**
	 * ����candidateSP_list: ��Ҫ���Ƴ�candidateSP_list�б�newTuple֧���Ԫ��
	 * (��newTuple������globalSP_list��, �϶����ᱻcandidateSP_list�е�Ԫ��֧��)
	 * 
	 * @param tuple
	 */
	public void updateCSP(LinkedList<MutaTuple> candidateSP_list, SkyTuple newTuple){
		
		if(!candidateSP_list.isEmpty()){
			Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
			while(iter.hasNext()){
				int flag = IsDominate.dominateBetweenTuples(newTuple, iter.next().getSkyTuple());
				if(flag == 0){
					iter.remove();
				}
			}
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	/**
	 * ��GSP�ļ��е�Ԫ��װ�ص�ȫ��Skyline�����У�������SkyTuple��Ȼ���˳����֯
	 * 
	 * @param globalSP_list			���ȫ��Skyline������
	 * @param dataType				���ݷֲ�����
	 * @param dim					����ά��
	 * @param globalWindowSize		�������ڴ�С
	 * @param infileDir				�����ļ�Ŀ¼
	 */
	public void loadGSP(LinkedList<SkyTuple> globalSP_list, int dataType, int dim, long globalWindowSize, String infileDir){
		
		String infilename = "GSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();					// ����Ԥ����
			SkyTuple tuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildTupleFromStr(str_line);
					globalSP_list.offer(tuple);					// ����ȡ�����ݲ���globalSP_list������β
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * ��CSP�ļ��е�Ԫ��װ�ص�ȫ��CSP�����У�������SkyTuple��Ȼ���˳����֯
	 * 
	 * @param candidateSP_list		���ȫ��CSP������
	 * @param dataType				���ݷֲ�����
	 * @param dim					����ά��
	 * @param globalWindowSize		�������ڴ�С
	 * @param infileDir				�����ļ�Ŀ¼
	 */
	public void loadCSP(LinkedList<MutaTuple> candidateSP_list, int dataType, int dim, long globalWindowSize, String infileDir){
		
		String infilename = "CSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// ����Ԥ����
			MutaTuple mtuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					mtuple = hp.buildMutaTupleFromStr(str_line);
					candidateSP_list.offer(mtuple);							// ����ȡ�����ݲ���candidateSP_list������β
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	/**
	 * SharedSkyline��GSP load����,��Ԥ�ȼ���õ�GSP�ļ�װ�ص����subGSPs������,��Ԫ��(tupleID % numSubGSP)��֯
	 * 
	 * @param subGSPs					ȫ��Skyline����(����Ϊ����ӱ�)
	 * @param dataType					���ݷֲ�����
	 * @param dim						����ά��
	 * @param numSubGSP					��ȫ��GSP����Ϊ�ӱ����Ŀ(���ֵһ����Ҫ���ݾ���GSP������ȷ��һ���ȽϺ��ʵ�ֵ)
	 * @param globalWindowSize			ȫ�ֻ������ڴ�С
	 * @param infileDir					�����ļ�Ŀ¼
	 */
	public void loadGSPForSharedSkyline(ArrayList<LinkedList<SkyTuple>> subGSPs, int dataType, int dim, int numSubGSP, 
			long globalWindowSize, String infileDir){
		
		String infilename = "GSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// ����Ԥ����
			SkyTuple tuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildTupleFromStr(str_line);
					
					int index = (int) (tuple.getTupleID() % numSubGSP);
					subGSPs.get(index).offer(tuple);						// ����ȡ�����ݲ���subGSP[index]������β
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * SharedSkyline�Լ�PsSkyline��CSP load����,��Ԥ�ȼ���õ�CSP�ļ�װ�ص����subCSPs������,��Ԫ���latestDominateId % numSubCSP��֯
	 * 
	 * @param subCSPs			ȫ��Skyline����(����Ϊ����ӱ�)
	 * @param dataType			���ݷֲ�����
	 * @param dim				����ά��
	 * @param numSubCSP			��ȫ��CSP����Ϊ�ӱ����Ŀ(���ֵһ����Ҫ���ݾ���latestDominateId�ֲ������ȷ��һ���ȽϺ��ʵ�ֵ)
	 * @param globalWindowSize	ȫ�ֻ������ڴ�С
	 * @param infileDir			�����ļ�Ŀ¼
	 */
	public void loadCSPForParallelSkyline(ArrayList<LinkedList<MutaTuple>> subCSPs, int dataType, int dim, int numSubCSP, 
			long globalWindowSize, String infileDir){
		
		String infilename = "CSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// ����Ԥ����
			MutaTuple tuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildMutaTupleFromStr(str_line);
					
					int index = (int) (tuple.getDominateID() % numSubCSP);
					subCSPs.get(index).offer(tuple);						// ����ȡ�����ݲ���subCSP[index]������β
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/*
	 * *************************************************************************************************************************
	 * *************************************************************************************************************************
	 */
	
	/**
	 * ��ԭʼ�����ļ��ж�ȡ������Ԫ�鲢��Ԫ���ȡģ��������ֲ�����������
	 * 
	 * @param localWindows		����ֲ���������
	 * @param globalWindowSize	ȫ�ֻ������ڵĹ�ģ
	 * @param infileDir 		ԭʼ�������ڵ��ļ���
	 * @param infilename		ԭʼ���ݵ��ļ���
	 * @param psExecutorNum		���������߳���Ŀ
	 */
	public void loadLocalWindow(ArrayList<LinkedList<SkyTuple>> localWindows, long globalWindowSize, 
			String infileDir, String infilename, int psExecutorNum){
		
		File fileIn = new File(infileDir + infilename);
		
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			long countLine = 0;										// ���ƴӶ�ȡ���ݶ�����
			Hpreprocess hp = new Hpreprocess();						// ����Ԥ����
			SkyTuple tuple = null;	
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				if(countLine < globalWindowSize){
					str_line = bReader.readLine();
					
					if(str_line != null){
						tuple = hp.buildTupleFromStr(str_line);
						
						int index = (int) (tuple.getTupleID() % psExecutorNum);
						localWindows.get(index).offer(tuple);		// ����ȡ�����ݰ�����Ԫ��Ų����Ӧ��localWindow��
						
						countLine ++;
					}
				}else{
					break;											// �������ڹ���������������ѭ��
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	/**
	 * ��GSP�ļ��е�Ԫ��װ�ص�subGSP����������
	 * @param subGSPs				���ȫ��Skyline����������
	 * @param dataType				���ݷֲ�����
	 * @param dim					����ά��
	 * @param numPsExecutor			���������߳���Ŀ
	 * @param globalWindowSize		ȫ�ֻ������ڴ�С
	 * @param infileDir				�����ļ�Ŀ¼
	 */
	public void loadGSPForPsSkyline(ArrayList<LinkedList<SkyTuple>> subGSPs, int dataType, int dim, int numPsExecutor, 
			long globalWindowSize, String infileDir){
		
		String infilename = "GSP_" + dataType + "_" + dim + "d_" + globalWindowSize + ".txt";
		File fileIn = new File(infileDir + infilename);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			// ���ļ���ȡ��صĸ��ֱ���
			String str_line = "";
			Hpreprocess hp = new Hpreprocess();								// ����Ԥ����
			SkyTuple tuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					tuple = hp.buildTupleFromStr(str_line);
					
					int index = (int) (tuple.getTupleID() % numPsExecutor);
					subGSPs.get(index).offer(tuple);						// ����ȡ�����ݲ���subGSP[index]������β
					
				}else{
					System.err.println("Error read! Null line!");
				}
			}
			bReader.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}

