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
		 * ArrayBlockingQueue:规定大小的BlockingQueue,其构造函数必须带一个int参数来指明其大小
		 * 
		 * LinkedBlockingQueue:大小不定的BlockingQueue,若其构造函数带一个规定大小的参数,生成的BlockingQueue有大小限制
		 */
		BlockingQueue<SkyTuple> tupleBuffer = new LinkedBlockingDeque<SkyTuple>();
		LinkedList<SkyTuple> globalWindow_list = new LinkedList<SkyTuple>();
		LinkedList<SkyTuple> globalSP_list = new LinkedList<SkyTuple>();

		// 相关变量及赋值
		long globalWindowSize = Constants.GlobalWindowSize;
		String fileDir = Constants.DATA_DIRECTORY;
		String filename = "rawdata.txt";
		
		/*
		 * 查询初始化阶段，提前将滑动窗口充满，以降低实验过程中的setup time
		 */
		Initializer initializer = new Initializer();
		initializer.loadGlobalWindow(globalWindow_list, globalWindowSize, fileDir, filename);	// 预装载全局滑动窗口
		Collections.sort(globalWindow_list);
		globalSP_list = SFS.sfsQuery(globalWindow_list);										// 初始化计算全局Skyline（实际上无需计算）
		
		/*
		 * 查询稳定执行阶段
		 */
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.execute(new FetchDataThread(tupleBuffer, fileDir, filename, globalWindowSize));
		exec.execute(new NaiveS2Skyline(tupleBuffer, globalWindow_list));
	}

}
