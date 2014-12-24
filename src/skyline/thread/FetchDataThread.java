package skyline.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.BlockingQueue;

import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.Hpreprocess;

public class FetchDataThread implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer; 	// �̰߳�ȫ���У��ж��ʵ��	
	
	private String fileDir;							// ԭʼ�������ڵ��ļ���
	private String filename;						// ԭʼ���ݵ��ļ���
	private long startID;							// ��ȡ�����ݵĿ�ʼ���
	
	/**
	 * FetchDataThread��Ĵ������Ĺ��캯��
	 * @param tupleBuffer	����������ݵĶ���
	 * @param fileDir		ԭʼ�������ڵ��ļ���
	 * @param filename		ԭʼ���ݵ��ļ���
	 */
	public FetchDataThread(BlockingQueue<SkyTuple> tupleBuffer, String fileDir, String filename)
	{
		this.tupleBuffer = tupleBuffer;
		this.fileDir = fileDir;
		this.filename = filename;
		
		this.startID = 0;
	}
	
	/**
	 * FetchDataThread��Ĵ������Ĺ��캯��2
	 * @param tupleBuffer	����������ݵĶ���
	 * @param fileDir		ԭʼ�������ڵ��ļ���
	 * @param filename		ԭʼ���ݵ��ļ���
	 * @param startID		��ʾ���ĸ���ſ�ʼ�������ݣ�һ��ΪglobalWindowSize(),ģ����ǰ���������ڳ����Խ�ʡʵ���е�setup time
	 */
	public FetchDataThread(BlockingQueue<SkyTuple> tupleBuffer, String fileDir, String filename, long startID)
	{
		this.tupleBuffer = tupleBuffer;
		this.fileDir = fileDir;
		this.filename = filename;
		
		this.startID = startID;
	}

	/**
	 * ����ʵ��Runnable�ӿ���ʵ���̹߳��ܣ���Ҫʵ��һ��run()����
	 * ��һ������ѭ�����ϵش�ԭʼ�����ļ�rawdata.txt�ж�ȡ����Ҫ������
	 */
	public void run() {
		
		long queryGranularity = 0L;
		try{
			
			// ���ļ���ȡ��صĸ��ֱ���
			File fileIn = new File(fileDir + filename);
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String str_line = "";
			long countLine = 0;										// ���ƴ���һ�п�ʼ��ȡ���ݣ�����һ��ʼ����
			Hpreprocess hp = new Hpreprocess();						// ����Ԥ����
			SkyTuple tuple = null;
			
			// ��ԭʼ�����ļ��ж�ȡ���ݲ�����SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					countLine ++;
					
					if(countLine > startID){
						
						tuple = hp.buildTupleFromStr(str_line); 	// ����ȡ���ַ���������SkyTuple
						
						// ��һ���жϣ���tupleBuffer.size()����maxBufferSizeʱ����һ����ͣ10s����tupleBuffer�з�����
						if(tupleBuffer.size() > Constants.MaxBufferSize){
							System.err.println("The tuple buffer is full now.");
							Thread.sleep(10*1000);					// �����������Ķ�ȡ���ʣ���������ʱ����ͣ10s��
						}else{
							tupleBuffer.put(tuple);
							
							queryGranularity ++;
							if(queryGranularity == (Constants.QueryGran + 100)){
								break;								// ֹͣ�߳�
							}
							Thread.sleep(Constants.StreamRate);		
						}
						
					}else if(countLine <= startID){
						continue;									// ��������Դ�ļ���һ��ʼ������������
					}
				}
			}
			bReader.close();
			
		}catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			System.out.println("This is the end of reading file.");
		}
	}
}
