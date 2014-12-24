package skyline.main;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import skyline.algorithms.SFS;
import skyline.model.SkyTuple;
import skyline.thread.FetchDataThread;
import skyline.thread.NaiveS2Skyline;
import skyline.util.Constants;
import skyline.util.Initializer;

public class TestNaiveS2Skyline {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		/*
		 * ArrayBlockingQueue:�涨��С��BlockingQueue,�乹�캯�������һ��int������ָ�����С
		 * 
		 * LinkedBlockingQueue:��С������BlockingQueue,���乹�캯����һ���涨��С�Ĳ���,���ɵ�BlockingQueue�д�С����
		 */
		BlockingQueue<SkyTuple> tupleBuffer = new LinkedBlockingDeque<SkyTuple>();
		LinkedList<SkyTuple> globalWindow_list = new LinkedList<SkyTuple>();
		LinkedList<SkyTuple> globalSP_list = new LinkedList<SkyTuple>();

		// ��ر�������ֵ
		long globalWindowSize = Constants.GlobalWindowSize;
		String fileDir = Constants.DATA_DIRECTORY;
		String filename = "rawdata.txt";
		
		/*
		 * ��ѯ��ʼ���׶Σ���ǰ���������ڳ������Խ���ʵ������е�setup time
		 */
		Initializer initializer = new Initializer();
		initializer.loadGlobalWindow(globalWindow_list, globalWindowSize, fileDir, filename);	// Ԥװ��ȫ�ֻ�������
		Collections.sort(globalWindow_list);
		globalSP_list = SFS.sfsQuery(globalWindow_list);										// ��ʼ������ȫ��Skyline��ʵ����������㣩
		
		/*
		 * ��ѯ�ȶ�ִ�н׶�
		 */
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(new FetchDataThread(tupleBuffer, fileDir, filename, globalWindowSize));
		exec.execute(new NaiveS2Skyline(tupleBuffer, globalWindow_list));
	}

}
