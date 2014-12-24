package skyline.util;

public class Constants {

	// 数据文件目录和源数据文件
	public static String CONFIG_DIRECTORY = "./config/"; 		// 设置配置文件所在的目录
	public static String DATA_DIRECTORY = "./data/"; 			// 设置源数据文件所在的目录
	public static String LOG_DIRECTORY = "./log/";				// 设置日志文件所在的目录
//	public static String RAWDATAFILE = "rawdata.txt"; 			// 设置源数据文件的文件名
	
	// 与数据元组本身相关的参数：维度、集的势、数据分布类型
	public static int DATATYPE = 0;								// 数据分布类型，0为独立数据，1为相关数据，2为反相关数据，默认为0
	public static int DIMENSION = 5;							// 数据维度，默认为5
	public static long CARDINALITY = 300*1000000;				// 集的势，默认为1G
	
	// 与数据流本身有关的参数：数据流速率、滑动窗口长度
	public static long StreamRate = 1;							// 数据流速率，默认为1000 tuple/s
	public static long GlobalWindowSize = 10000000;				// 全局滑动窗口的大小，默认为10M
	public static long LocalWindowSize = 1000;					// 局部滑动窗口的大小（一般为GlobalWindowSize/NumPsExecutor）
	
	// 与查询处理相关的参数：并行的线程数、最大Buffer规模
	public static long MaxBufferSize = 10000;					// 规定最大的tupleBuffer链表长度，默认为10000
	public static int QueryGran = 10000;						// 规定查询粒度，即统计处理多少个tuple所需的时间，默认为10000
	
	// 
	public static int NumPsExecutor = 8;						// PsSkyline中，PsExecutor的线程数目
//	public static int NumSUBCSP = 8;							// PsSkyline中，PsMerger端维护的subCSP的个数
	public static int NumSUBGSP = 5;							// SharedSkyline中，并行计算GSP的线程数目,也即GSP划分为子表的数目
	public static int NumSUBCSP = 5;							// PsSkyline/SharedSkyline中，并行计算CGSP的线程数目,也即CSP划分为子表的数目
	
	
	
	/*
	 * **************************************************************************************************************************************
	 * **************************************************************************************************************************************
	 */
	
	/**
	 * 获取生成的数据文件的card属性的单位
	 * @return
	 */
	public static String genCard(){
		String str_card = ""; 
		if(CARDINALITY >= 1000000000)
			str_card = (CARDINALITY/1000000000 + "G_");
		else if(CARDINALITY >= 1000000)
			str_card = (CARDINALITY/1000000 + "M_");
		else if(CARDINALITY >= 1000)
			str_card = (CARDINALITY/1000 + "K_");
		else
			str_card = (CARDINALITY + "_");;
		return str_card;
	}
	
	/**
	 * 获取生成的数据文件的dim属性的单位
	 * @return
	 */
	public static String genDim(){
		String str_dim = (DIMENSION + "d");
		return str_dim;
	}
	
}
