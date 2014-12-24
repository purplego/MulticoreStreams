package skyline.util;

import java.io.File;
import java.util.Set;

import skyline.model.MyProperties;

public class ParseProperties {
	
	public static void parseProperties(String filePath){
		
		if(new File(filePath).isFile()){
			
			MyProperties mypro = new MyProperties(filePath);
			
			Set<String> keySet = mypro.propertiesNames();
			
			// ������Ԫ�鱾����صĲ�����ά�ȡ������ơ����ݷֲ�����
			if(keySet.contains("DATATYPE"))
				Constants.DATATYPE = Integer.parseInt(mypro.getValueByKey("DATATYPE"));
			if(keySet.contains("DIMENSION"))
				Constants.DIMENSION = Integer.parseInt(mypro.getValueByKey("DIMENSION"));
			if(keySet.contains("CARDINALITY"))
				Constants.CARDINALITY = Long.parseLong(mypro.getValueByKey("CARDINALITY"));
			
			// �������������йصĲ��������������ʡ��������ڳ���
			if(keySet.contains("StreamRate"))
				Constants.StreamRate = Long.parseLong(mypro.getValueByKey("StreamRate"));
			if(keySet.contains("GlobalWindowSize"))
				Constants.GlobalWindowSize = Long.parseLong(mypro.getValueByKey("GlobalWindowSize"));
			if(keySet.contains("LocalWindowSize"))
				Constants.LocalWindowSize = Long.parseLong(mypro.getValueByKey("LocalWindowSize"));
			
			// ���ѯ������صĲ��������е��߳��������Buffer��ģ
			if(keySet.contains("NumPsExecutor"))
				Constants.NumPsExecutor = Integer.parseInt(mypro.getValueByKey("NumPsExecutor"));
			if(keySet.contains("MaxBufferSize"))
				Constants.MaxBufferSize = Long.parseLong(mypro.getValueByKey("MaxBufferSize"));
			if(keySet.contains("QueryGran"))
				Constants.QueryGran = Integer.parseInt(mypro.getValueByKey("QueryGran"));
		}
		
	}
	
	

}
