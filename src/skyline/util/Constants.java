package skyline.util;

public class Constants {

	// �����ļ�Ŀ¼��Դ�����ļ�
	public static String CONFIG_DIRECTORY = "./config/"; 		// ���������ļ����ڵ�Ŀ¼
	public static String DATA_DIRECTORY = "./data/"; 			// ����Դ�����ļ����ڵ�Ŀ¼
	public static String LOG_DIRECTORY = "./log/";				// ������־�ļ����ڵ�Ŀ¼
//	public static String RAWDATAFILE = "rawdata.txt"; 			// ����Դ�����ļ����ļ���
	
	// ������Ԫ�鱾����صĲ�����ά�ȡ������ơ����ݷֲ�����
	public static int DATATYPE = 0;								// ���ݷֲ����ͣ�0Ϊ�������ݣ�1Ϊ������ݣ�2Ϊ��������ݣ�Ĭ��Ϊ0
	public static int DIMENSION = 5;							// ����ά�ȣ�Ĭ��Ϊ5
	public static long CARDINALITY = 300*1000000;				// �����ƣ�Ĭ��Ϊ1G
	
	// �������������йصĲ��������������ʡ��������ڳ���
	public static long StreamRate = 1;							// ���������ʣ�Ĭ��Ϊ1000 tuple/s
	public static long GlobalWindowSize = 10000000;				// ȫ�ֻ������ڵĴ�С��Ĭ��Ϊ10M
	public static long LocalWindowSize = 1000;					// �ֲ��������ڵĴ�С��һ��ΪGlobalWindowSize/NumPsExecutor��
	
	// ���ѯ������صĲ��������е��߳��������Buffer��ģ
	public static long MaxBufferSize = 10000;					// �涨����tupleBuffer�����ȣ�Ĭ��Ϊ10000
	public static int QueryGran = 10000;						// �涨��ѯ���ȣ���ͳ�ƴ�����ٸ�tuple�����ʱ�䣬Ĭ��Ϊ10000
	
	// 
	public static int NumPsExecutor = 8;						// PsSkyline�У�PsExecutor���߳���Ŀ
//	public static int NumSUBCSP = 8;							// PsSkyline�У�PsMerger��ά����subCSP�ĸ���
	public static int NumSUBGSP = 5;							// SharedSkyline�У����м���GSP���߳���Ŀ,Ҳ��GSP����Ϊ�ӱ����Ŀ
	public static int NumSUBCSP = 5;							// PsSkyline/SharedSkyline�У����м���CGSP���߳���Ŀ,Ҳ��CSP����Ϊ�ӱ����Ŀ
	
	
	
	/*
	 * **************************************************************************************************************************************
	 * **************************************************************************************************************************************
	 */
	
	/**
	 * ��ȡ���ɵ������ļ���card���Եĵ�λ
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
	 * ��ȡ���ɵ������ļ���dim���Եĵ�λ
	 * @return
	 */
	public static String genDim(){
		String str_dim = (DIMENSION + "d");
		return str_dim;
	}
	
}
