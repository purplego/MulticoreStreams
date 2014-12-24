package skyline.algorithms;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;

import skyline.model.SkyTuple;
import skyline.util.Hpreprocess;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class SFS {
	
	/**
	 * sfsQueryByBNL����������BNL�㷨���������������ļ���skyline����
	 * @param fileDir 		���ԭʼ�����ļ���Ŀ¼
	 * @param inFilename 	�����������ļ�
	 * @return spList�������skyline���ϣ���list��ʽ��֯
	 */
	public static LinkedList<SkyTuple> sfsQueryByBNL(String fileDir, String inFilename){
		
		return BNL.bnlQuery(fileDir, inFilename);
	}

	/**
	 * sfsQuery����������SFS�㷨���������������ļ���skyline����
	 * @param fileDir 		���ԭʼ�����ļ���Ŀ¼
	 * @param inFilename 	�����������ļ�
	 * @return spList		�����skyline���ϣ���list��ʽ��֯
	 */
	public static LinkedList<SkyTuple> sfsQuery(String fileDir, String inFilename){
		
		String filePathIn = fileDir + inFilename;
		File fileIn=new File(filePathIn);
		Hpreprocess h_process = new Hpreprocess();
		
		LinkedList<SkyTuple> spList = new LinkedList<SkyTuple>();		//�����Ѽ������skyline��
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);
			
			String str_line = "";
			while(bReader.ready()){
				str_line = bReader.readLine();
				SkyTuple tuple = h_process.buildTupleFromStr(str_line);
				if(spList.isEmpty())
					spList.add(tuple);
				else{
					int i = 0;
					boolean flag = true;
					while(flag){
						int isDominate = 0;								//��־�������ж�֧���ϵ�Ƿ����
						
						/*
						 * ����֧���ϵ���Ժ�����������������֮���֧���ϵ
						 * SFS�㷨��������������ļ�������isDominate��ֵֻ������0��2������������ݲ���֧��ǰ�������
						 */
						isDominate = IsDominate.dominateBetweenTuples(spList.get(i), tuple);
						switch(isDominate){
						case 0:											//tuple��֧�䣬ֱ��ɾȥ�ö���
							flag = false;								//����whileѭ��������һ������
							break;
						case 2:											//tuple��spList�еĵ�i�����󻥲�֧�䣬��������Ƚϣ�������һ��ѭ��
							i++;										//�Ƚ϶����Ϊ�����е���һ������
							break;
						case 1:
							break;
						default:
							break;
						}
						if(i==spList.size()){							//����Ƚϵ�spList�����һ�������tuple��Ȼδ��֧�䣬������������ı�β
							spList.addLast(tuple);
							flag = false;								//����whileѭ�������ļ��е���һ��
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return spList;
	}
	
	/**
	 * sfsQuery����������SFS�㷨���������������������skyline����
	 * @param tuple_list
	 * @return
	 */
	public static LinkedList<SkyTuple> sfsQuery(LinkedList<SkyTuple> tuple_list){
		
		LinkedList<SkyTuple> sp_list = new LinkedList<SkyTuple>();		//�����Ѽ������skyline��
		for(SkyTuple newTuple: tuple_list){
			
			if(sp_list.isEmpty()){
				sp_list.offer(newTuple);
			}else{
				int index = 0;
				while(true){
					
					/*
					 * ����֧���ϵ���Ժ�����������������֮���֧���ϵ
					 * SFS�㷨��������������ļ�������isDominate��ֵֻ������0��2������������ݲ���֧��ǰ�������
					 */
					int isDominate = IsDominate.dominateBetweenTuples(sp_list.get(index), newTuple);	//��־�������ж�֧���ϵ�Ƿ����
					
					//tuple��֧�䣬ֱ��ɾȥ�ö���
					if(isDominate == 0){
						break;									//����whileѭ��������һ������
					}
					else if(isDominate == 1){
						break;
					}
					//tuple��spList�еĵ�i�����󻥲�֧�䣬��������Ƚϣ�������һ��ѭ��
					else if(isDominate == 2){
						index ++;
						break;									//�Ƚ϶����Ϊ�����е���һ������
					}
					//����Ƚϵ�spList�����һ�������tuple��Ȼδ��֧�䣬������������ı�β
					if(index == sp_list.size()){
						sp_list.offer(newTuple);
						break;									//����whileѭ�������ļ��е���һ��
					}
				}
			}
		}
		return sp_list;
	}
}
