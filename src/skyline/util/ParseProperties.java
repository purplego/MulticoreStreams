package skyline.util;

import java.io.File;
import java.util.Set;

import skyline.model.MyProperties;

public class ParseProperties {
	
	public static void parseProperties(String filePath){
		
		if(new File(filePath).isFile()){
			
			MyProperties mypro = new MyProperties(filePath);
			
			Set<String> keySet = mypro.propertiesNames();
			
			// 与数据元组本身相关的参数：维度、集的势、数据分布类型
			if(keySet.contains("DATATYPE"))
				Constants.DATATYPE = Integer.parseInt(mypro.getValueByKey("DATATYPE"));
			if(keySet.contains("DIMENSION"))
				Constants.DIMENSION = Integer.parseInt(mypro.getValueByKey("DIMENSION"));
			if(keySet.contains("CARDINALITY"))
				Constants.CARDINALITY = Long.parseLong(mypro.getValueByKey("CARDINALITY"));
			
			// 与数据流本身有关的参数：数据流速率、滑动窗口长度
			if(keySet.contains("StreamRate"))
				Constants.StreamRate = Long.parseLong(mypro.getValueByKey("StreamRate"));
			if(keySet.contains("GlobalWindowSize"))
				Constants.GlobalWindowSize = Long.parseLong(mypro.getValueByKey("GlobalWindowSize"));
			if(keySet.contains("LocalWindowSize"))
				Constants.LocalWindowSize = Long.parseLong(mypro.getValueByKey("LocalWindowSize"));
			
			// 与查询处理相关的参数：并行的线程数、最大Buffer规模
			if(keySet.contains("NumPsExecutor"))
				Constants.NumPsExecutor = Integer.parseInt(mypro.getValueByKey("NumPsExecutor"));
			if(keySet.contains("MaxBufferSize"))
				Constants.MaxBufferSize = Long.parseLong(mypro.getValueByKey("MaxBufferSize"));
			if(keySet.contains("QueryGran"))
				Constants.QueryGran = Integer.parseInt(mypro.getValueByKey("QueryGran"));
		}
		
	}
	
	

}
