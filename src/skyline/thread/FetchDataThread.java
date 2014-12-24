package skyline.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.BlockingQueue;

import skyline.model.SkyTuple;
import skyline.util.Constants;
import skyline.util.Hpreprocess;

public class FetchDataThread implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer; 	// 线程安全队列，有多个实现	
	
	private String fileDir;							// 原始数据所在的文件夹
	private String filename;						// 原始数据的文件名
	private long startID;							// 读取的数据的开始序号
	
	/**
	 * FetchDataThread类的带参数的构造函数
	 * @param tupleBuffer	存放生成数据的队列
	 * @param fileDir		原始数据所在的文件夹
	 * @param filename		原始数据的文件名
	 */
	public FetchDataThread(BlockingQueue<SkyTuple> tupleBuffer, String fileDir, String filename)
	{
		this.tupleBuffer = tupleBuffer;
		this.fileDir = fileDir;
		this.filename = filename;
		
		this.startID = 0;
	}
	
	/**
	 * FetchDataThread类的带参数的构造函数2
	 * @param tupleBuffer	存放生成数据的队列
	 * @param fileDir		原始数据所在的文件夹
	 * @param filename		原始数据的文件名
	 * @param startID		表示从哪个序号开始生成数据，一般为globalWindowSize(),模拟提前将滑动窗口充满以节省实验中的setup time
	 */
	public FetchDataThread(BlockingQueue<SkyTuple> tupleBuffer, String fileDir, String filename, long startID)
	{
		this.tupleBuffer = tupleBuffer;
		this.fileDir = fileDir;
		this.filename = filename;
		
		this.startID = startID;
	}

	/**
	 * 采用实现Runnable接口来实现线程功能，需要实现一个run()方法
	 * 以一定速率循环不断地从原始数据文件rawdata.txt中读取所需要的数据
	 */
	public void run() {
		
		long queryGranularity = 0L;
		try{
			
			// 与文件读取相关的各种变量
			File fileIn = new File(fileDir + filename);
			FileReader fReader = new FileReader(fileIn);
			BufferedReader bReader = new BufferedReader(fReader);
			
			String str_line = "";
			long countLine = 0;										// 控制从哪一行开始读取数据，跳过一开始的行
			Hpreprocess hp = new Hpreprocess();						// 数据预处理
			SkyTuple tuple = null;
			
			// 从原始数据文件中读取数据并构造SkyTuple
			while(bReader.ready()){
				str_line = bReader.readLine();
				if(str_line != null){
					countLine ++;
					
					if(countLine > startID){
						
						tuple = hp.buildTupleFromStr(str_line); 	// 将读取的字符串解析成SkyTuple
						
						// 加一个判断，当tupleBuffer.size()超过maxBufferSize时，做一个暂停10s不往tupleBuffer中放数据
						if(tupleBuffer.size() > Constants.MaxBufferSize){
							System.err.println("The tuple buffer is full now.");
							Thread.sleep(10*1000);					// 控制数据流的读取速率（当缓冲慢时，暂停10s）
						}else{
							tupleBuffer.put(tuple);
							
							queryGranularity ++;
							if(queryGranularity == (Constants.QueryGran + 100)){
								break;								// 停止线程
							}
							Thread.sleep(Constants.StreamRate);		
						}
						
					}else if(countLine <= startID){
						continue;									// 跳过数据源文件中一开始的若干行数据
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
