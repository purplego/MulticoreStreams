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
		
		// 与数据元组本身相关的参数：维度、集的势、数据分布类型
		if(argsParse.isContains("DATATYPE"))
			Constants.DATATYPE = Integer.parseInt(argsParse.getArgs("DATATYPE"));
		if(argsParse.isContains("DIMENSION"))
			Constants.DIMENSION = Integer.parseInt(argsParse.getArgs("DIMENSION"));
		if(argsParse.isContains("CARDINALITY"))
			Constants.CARDINALITY = Long.parseLong(argsParse.getArgs("CARDINALITY"));
		
		// 与数据流本身有关的参数：数据流速率、滑动窗口长度
		if(argsParse.isContains("StreamRate"))
			Constants.StreamRate = Long.parseLong(argsParse.getArgs("StreamRate"));
		if(argsParse.isContains("GlobalWindowSize"))
			Constants.GlobalWindowSize = Long.parseLong(argsParse.getArgs("GlobalWindowSize"));
		if(argsParse.isContains("LocalWindowSize"))
			Constants.LocalWindowSize = Long.parseLong(argsParse.getArgs("LocalWindowSize"));
		
		// 与查询处理相关的参数：并行的线程数、最大Buffer规模
		if(argsParse.isContains("NumPsExecutor"))
			Constants.NumPsExecutor = Integer.parseInt(argsParse.getArgs("NumPsExecutor"));
		if(argsParse.isContains("MaxBufferSize"))
			Constants.MaxBufferSize = Long.parseLong(argsParse.getArgs("MaxBufferSize"));
		if(argsParse.isContains("QueryGran"))
			Constants.QueryGran = Integer.parseInt(argsParse.getArgs("QueryGran"));
	}
	

	

}
