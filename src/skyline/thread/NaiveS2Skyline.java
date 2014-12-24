package skyline.thread;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import skyline.util.Constants;

import skyline.algorithms.BNL;
import skyline.algorithms.SFS;
import skyline.model.SkyTuple;

public class NaiveS2Skyline implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer;		// 取数据的地方，由GenDataThread产生数据
	private LinkedList<SkyTuple> globalWindow_list;		// 维护的全局滑动窗口
	private LinkedList<SkyTuple> globalSP_list;			// 维护的全局Skyline集合（当前滑动窗口的Skyline集合）
	
	/**
	 * 带参数的构造函数
	 * @param tupleBuffer
	 */
	public NaiveS2Skyline(BlockingQueue<SkyTuple> tupleBuffer){
		this.tupleBuffer = tupleBuffer;
		this.globalWindow_list = new LinkedList<SkyTuple>();
		this.globalSP_list = new LinkedList<SkyTuple>();
	}
	
	/**
	 * 带参数的构造函数2，模拟滑动窗口已经被装满的情况
	 * @param tupleBuffer
	 * @param globalWindow_list
	 */
	public NaiveS2Skyline(BlockingQueue<SkyTuple> tupleBuffer, LinkedList<SkyTuple> globalWindow_list){
		
		this.tupleBuffer = tupleBuffer;
		this.globalWindow_list = globalWindow_list;
		this.globalSP_list = new LinkedList<SkyTuple>();
	}
	
	/**
	 * 采用实现Runnable接口来实现线程功能，需要实现一个run()方法
	 */
	@Override
	public void run() {

		while(true){
			try{
				
				// 从tupleBuffer中取一个newTuple(线程安全,若取不到元素，则阻塞)
				SkyTuple newTuple = tupleBuffer.take();
				System.err.println("The tupleBuffer size:  " + tupleBuffer.size());
				SkyTuple expiredTuple = null;
				
				if(globalWindow_list.size() >= Constants.GlobalWindowSize){
					/*
					 * 判断滑动窗口的情况（未满或已满）;如果滑动窗口已满，则产生一个过期元组
					 * 将过期元组从全局滑动窗口globalSlidingWindow中删除(链表头的元组)
					 */
					expiredTuple = globalWindow_list.poll();
//					System.out.println("Expired Tuple: " + expiredTuple.toStringTuple());
				}
				
				// 将newTuple插入全局滑动窗口globalSlidingWindow
				globalWindow_list.offer(newTuple);
				
				// 以globalSlidingWindow为数据集合计算全局Skyline
				Collections.sort(globalWindow_list);
				globalSP_list = SFS.sfsQuery(globalWindow_list);
				System.out.println("The globalSkylines size: " + globalSP_list.size());
				
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
