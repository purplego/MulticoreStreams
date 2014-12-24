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
	 * readbyBlock方法 按块大小读取原始数据文件，并构造一个以String类型对象为节点的链表
	 * 
	 * @param fileDir 	存放原始数据文件的目录
	 * @param filename	待读取的文件名，存放原始数据文件
	 * @param startPos	开始读取的位置，初始值为0
	 * @param blocksize 每次读取的块大小
	 * @param count 	记录读取的是第几块，用于定位开始读取的位置startPos
	 * @return 			构建好的链表LinkedList<String>
	 */
	public static LinkedList<String> readbyBlock(String fileDir, String filename, long blocksize, int cycleCount){
		
		LinkedList<String> temp_list = new LinkedList<String>();//存放构建好的链表
		
		String filePathIn = fileDir + filename;
		File fileIn = new File(filePathIn);
		try{
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String str_line = "";
			boolean isLoop = true;
			while(bReader.ready()){
//				bReader.skip(cycleCount*blocksize);
				
				// 先跳过前面已经的读取过的行
				if(isLoop){
					for(int j=0; j<(cycleCount*blocksize); j++){
						str_line = bReader.readLine();
					}
					isLoop = false;
				}
				// 读取有效行并添加到链表中
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
