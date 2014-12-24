package skyline.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map.Entry;

public class ArgsParse {

		private HashMap<String, String> argsMap;
		
		//the constructor without parameter
		public ArgsParse(){
			argsMap = new HashMap<String, String>();
		}
		/**
		 * the constructor with a parameter，跳过以"#"开头的行和空行
		 * @param dir 		目标文件所在的文件夹
		 * @param filename 	目标文件的文件名
		 */
		public ArgsParse(String dir, String filename){
			argsMap = new HashMap<String, String>();
			
			String filePathIn = dir + filename;
			File fileIn = new File(filePathIn);
			if(fileIn.exists()){
				try{
					FileReader fReader = new FileReader(fileIn);
					BufferedReader bReader = new BufferedReader(fReader);
					
					String str_line = "";
					while(bReader.ready()){
						str_line = bReader.readLine();
						if((str_line == null)){
							System.out.println("Error reading. A null line.");
							
						}else if(!str_line.equals("")){
							
							str_line = str_line.replace(" ", "");
							if((str_line.equals("")) || (str_line.startsWith("#"))){
								// This is a null line or a notation line, DO NOTHING
							}else{
								String[] mark = str_line.split("=");
								if(!argsMap.containsKey(mark[0])){
									argsMap.put(mark[0], mark[1]);
								}
							}
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}else{
				System.err.println("File not exist. Using default configuration.");
				return;
			}
			
		}
		
		// the getter and setter, and the add approach
		public void setArgs(String key, String value){
			if(!argsMap.containsKey(key))
				System.out.println("Key: " + key + " not exists, set failed.");
			else
				argsMap.put(key, value);
		}
		
		public String getArgs(String key){
			if(!argsMap.containsKey(key)){
				System.out.println("Key: " + key + " not exists, get failed.");
				return "";
			}
			else
				return argsMap.get(key);
		}
		
		public void addArgs(String key, String value){
			if(argsMap.containsKey(key))
				System.out.println("Key: " + key + "has already existed, add failed.");
			else
				argsMap.put(key, value);
		}
		
		/**
		 * the isContain approach: override of the Map.contain(key) approach
		 * @param key
		 * @return
		 */
		public boolean isContains(String key){
			if(!argsMap.containsKey(key))
				return false;
			else
				return true;
		}
		
		/**
		 * the printer approach: usually for testing
		 * @param args
		 */
		public void printArgs(ArgsParse args){
			for(Entry<String, String> entry: args.argsMap.entrySet()){
				String key = entry.getKey();
				String value = entry.getValue();
				System.out.println(key + " = " + value);//输出到控制台
			}
			
		}
}
