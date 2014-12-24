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
public class BNL {
	
	/**
	 * bnlSkyline���������ļ�inFilename�ж�ȡԭʼ���ݼ�������BNL�㷨���skyline��
	 * ���õ�spList����SkyTuple����Ȼ����֯
	 * 
	 * @param fileDir 		���ԭʼ�����ļ���Ŀ¼	
	 * @param inFilename	���ԭʼ���ݼ����ļ�
	 * @return spList		���ش�žֲ�skyline��LinkedList��spList
	 */
	public static LinkedList<SkyTuple> bnlQuery(String fileDir, String inFilename){
		
		String filePathIn = fileDir + inFilename;
		File fileIn=new File(filePathIn);
		
		//����һ���ڵ�Ϊ�������͵��������ڴ�ź�ѡ��Skyline�㼰���ս��
		LinkedList<SkyTuple> spList = new LinkedList<SkyTuple>();
		Hpreprocess h_process = new Hpreprocess();
		try{
			FileReader fReader=new FileReader(fileIn);
			BufferedReader bReader=new BufferedReader(fReader);
			
			while(bReader.ready()){
				String str_line = bReader.readLine();
				SkyTuple tuple = h_process.buildTupleFromStr(str_line);
				if(spList.isEmpty())
					spList.add(tuple);
				else{
					int i = 0;
					boolean flag = true;
					while(flag){
						//����֧���ϵ���Ժ�����������������֮���֧���ϵ
						int isDominate = IsDominate.dominateBetweenTuples(spList.get(i), tuple);
						switch(isDominate){
						case 0:										//tuple��֧�䣬ֱ��ɾȥ�ö���
							flag = false;							//����whileѭ��������һ������
							break;
						case 1:										//tuple֧��spList�еĵ�i������ɾȥ�ö��󣬼�������Ƚϣ�������һ��ѭ��
							spList.remove(i);
							break;
						case 2:										//tuple��spList�еĵ�i�����󻥲�֧�䣬��������Ƚϣ�������һ��ѭ��
							i++;									//�Ƚ϶����Ϊ�����е���һ������
							break;
						default:
							break;
						}
						if(i==spList.size()){						//����Ƚϵ�spList�����һ�������tuple��Ȼδ��֧�䣬������������ı�β
							spList.addLast(tuple);
							flag = false;							//����whileѭ�������ļ��е���һ��
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
	 * bnlSkyline������������tuple_list�ж�ȡԭʼ���ݼ�������BNL�㷨���skyline��
	 * ���õ�spList����SkyTuple����Ȼ���������֯
	 * 
	 * @param tuple_list�����ԭʼ���ݼ�������
	 * @return ���ش�žֲ�skyline��LinkedList��spList, spList�ǰ�SkyTuple����Ȼ�����������ģ���id�ϴ��Ԫ����������ͷ
	 */
	public static LinkedList<SkyTuple> bnlQuery(LinkedList<SkyTuple> tuple_list){
		
		//����һ���ڵ�Ϊ�������͵��������ڴ�ź�ѡ��Skyline�㼰���ս��
		LinkedList<SkyTuple> sp_list = new LinkedList<SkyTuple>();
//		int countRemove = 0;
//		int countNotAdd = 0;
		
		for(SkyTuple tuple: tuple_list){
			
			if(sp_list.isEmpty()){
				sp_list.offerFirst(tuple);
			}else{
				int i = 0;
				boolean flag = true;
				while(flag){
					//����֧���ϵ���Ժ�����������������֮���֧���ϵ
					int isDominate = IsDominate.dominateBetweenTuples(sp_list.get(i), tuple);
					switch(isDominate){
					case 0:											
						flag = false;								//����whileѭ��������һ�����ж�����
//						countNotAdd ++;
						break;
					case 1:											//tuple֧��sp_list�еĵ�i������ɾȥ�ö��󣬼�������Ƚϣ�������һ��ѭ��
						sp_list.remove(i);
//						countRemove ++;
						break;
					case 2:											//tuple��spList�еĵ�i�����󻥲�֧�䣬��������Ƚϣ�������һ��ѭ��
						i++;										//�Ƚ϶����Ϊ�����е���һ������
						break;
					default:
						break;
					}
					if(i==sp_list.size()){							//����Ƚϵ�sp_list�����һ�������tuple��Ȼδ��֧�䣬������������ı�β
//						spList.addLast(tuple);
						sp_list.offerFirst(tuple);
						flag = false;								//����whileѭ������tuple_list�е���һ��
					}
				}
			}
		}
//		System.out.println("the number of tuples removed from the GSP_List in BNL is :" + countRemove);
//		System.out.println("the number of tuples not add into the GSP_List in BNL is :" + countNotAdd);
		return sp_list;
	}
}
