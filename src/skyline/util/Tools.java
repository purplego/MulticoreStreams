package skyline.util;

/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class Tools {
	
	/**
	 * the parseArgs approach: to parse args in config file
	 * @param config_file
	 */
	public static void parseArgs(String config_dir, String config_file){
		ArgsParse argsParse = new ArgsParse(config_dir, config_file);
		
		// ������Ԫ�鱾����صĲ�����ά�ȡ������ơ����ݷֲ�����
		if(argsParse.isContains("DATATYPE"))
			Constants.DATATYPE = Integer.parseInt(argsParse.getArgs("DATATYPE"));
		if(argsParse.isContains("DIMENSION"))
			Constants.DIMENSION = Integer.parseInt(argsParse.getArgs("DIMENSION"));
		if(argsParse.isContains("CARDINALITY"))
			Constants.CARDINALITY = Long.parseLong(argsParse.getArgs("CARDINALITY"));
		
		// �������������йصĲ��������������ʡ��������ڳ���
		if(argsParse.isContains("StreamRate"))
			Constants.StreamRate = Long.parseLong(argsParse.getArgs("StreamRate"));
		if(argsParse.isContains("GlobalWindowSize"))
			Constants.GlobalWindowSize = Long.parseLong(argsParse.getArgs("GlobalWindowSize"));
		if(argsParse.isContains("LocalWindowSize"))
			Constants.LocalWindowSize = Long.parseLong(argsParse.getArgs("LocalWindowSize"));
		
		// ���ѯ������صĲ��������е��߳��������Buffer��ģ
		if(argsParse.isContains("NumPsExecutor"))
			Constants.NumPsExecutor = Integer.parseInt(argsParse.getArgs("NumPsExecutor"));
		if(argsParse.isContains("MaxBufferSize"))
			Constants.MaxBufferSize = Long.parseLong(argsParse.getArgs("MaxBufferSize"));
		if(argsParse.isContains("QueryGran"))
			Constants.QueryGran = Integer.parseInt(argsParse.getArgs("QueryGran"));
	}
	

	

}
