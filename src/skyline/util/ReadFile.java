package skyline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class ReadFile {
	
	/**
	 * readbyBlock���� �����С��ȡԭʼ�����ļ���������һ����String���Ͷ���Ϊ�ڵ������
	 * 
	 * @param fileDir 	���ԭʼ�����ļ���Ŀ¼
	 * @param filename	����ȡ���ļ��������ԭʼ�����ļ�
	 * @param startPos	��ʼ��ȡ��λ�ã���ʼֵΪ0
	 * @param blocksize ÿ�ζ�ȡ�Ŀ��С
	 * @param count 	��¼��ȡ���ǵڼ��飬���ڶ�λ��ʼ��ȡ��λ��startPos
	 * @return 			�����õ�����LinkedList<String>
	 */
	public static LinkedList<String> readbyBlock(String fileDir, String filename, long blocksize, int cycleCount){
		
		LinkedList<String> temp_list = new LinkedList<String>();//��Ź����õ�����
		
		String filePathIn = fileDir + filename;
		File fileIn = new File(filePathIn);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String str_line = "";
			boolean isLoop = true;
			while(bReader.ready()){
//				bReader.skip(cycleCount*blocksize);
				
				// ������ǰ���Ѿ��Ķ�ȡ������
				if(isLoop){
					for(int j=0; j<(cycleCount*blocksize); j++){
						str_line = bReader.readLine();
					}
					isLoop = false;
				}
				// ��ȡ��Ч�в���ӵ�������
				else{
					str_line = bReader.readLine();
					if(str_line == null){
						System.out.println("Error read! Null line!");
					}else{
						if(temp_list.size() < blocksize){
							temp_list.add(str_line);
						}else{
							cycleCount++;
						}
					}
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return temp_list;
	}

}
