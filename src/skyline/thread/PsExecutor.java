 package skyline.thread;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import skyline.algorithms.IsDominate;
import skyline.model.ComboTuple;
import skyline.model.SkyTuple;
import skyline.util.Constants;

public class PsExecutor implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer;					// 取数据的地方，由GenDataThread产生数据
	private BlockingQueue<ComboTuple> submitQueue;					// 与mergerThread之间的数据交换是通过这个排队器实现的
	
	private LinkedList<SkyTuple> localWindow_list;					// 维护的局部滑动窗口
	private LinkedList<SkyTuple> localSP_list;						// 维护的局部Skyline集合（当前滑动窗口的Skyline集合）
	
	/**
	 * PsExecutor类的带参数的构造函数

	 * @param tupleBuffer	取数据的地方
	 * @param submitQueue	提交数据的地方
	 */
	public PsExecutor(BlockingQueue<SkyTuple> tupleBuffer, BlockingQueue<ComboTuple> submitQueue){
		this.tupleBuffer = tupleBuffer;
		this.submitQueue = submitQueue;
		
		this.localWindow_list = new LinkedList<SkyTuple>();
		this.localSP_list = new LinkedList<SkyTuple>();
	}
	
	/**
	 * PsExecutor类的带参数的构造函数2,模拟滑动窗口已经被装满的情况

	 * @param tupleBuffer			读取数据的地方
	 * @param submitQueue			提交数据的地方
	 * @param localWindow_list		局部滑动窗口
	 * @param localSP_list			局部Skyline集合
	 */
	public PsExecutor(BlockingQueue<SkyTuple> tupleBuffer, BlockingQueue<ComboTuple> submitQueue, 
			LinkedList<SkyTuple> localWindow_list, LinkedList<SkyTuple> localSP_list){
		this.tupleBuffer = tupleBuffer;
		this.submitQueue = submitQueue;
		
		this.localWindow_list = localWindow_list;
		this.localSP_list = localSP_list;
	}
	
	/**
	 * 采用实现Runnable接口来实现线程功能，需要实现一个run()方法
	 */
	@Override
	public void run() {

		long queryGranularity = 0L;				// 用于结果统计的计数，记录1000个tuple的处理时间
		long time0 = 0L, time1 = 0L;
		
		while(true){
			if(queryGranularity == 0){
				time0 = System.currentTimeMillis();
			}
			if(queryGranularity == (Constants.QueryGran/Constants.NumPsExecutor)){
				time1 = System.currentTimeMillis();

				Logger.getLogger("PsSkyline").info("ExecutorID: " + (Thread.currentThread().getName()) + " The total time to process " + 
						(Constants.QueryGran/Constants.NumPsExecutor) + " tuples: " + (time1-time0) + " ms.");

				break;							// 终止线程
			}
			
			try {
				// 从tupleBuffer中取一个newTuple(线程安全,若取不到元素，则阻塞)
				SkyTuple newTuple = tupleBuffer.take();
				queryGranularity ++;
				
				SkyTuple expiredTuple = null;
				if(localWindow_list.size() >= Constants.LocalWindowSize){
					expiredTuple = localWindow_list.poll();
				}
				
				// 将newTuple插入局部滑动窗口localSlidingWindow
				localWindow_list.offer(newTuple);
				
				// 处理过期元组expiredTuple
				if(expiredTuple != null){
					handleExpiredTuple(expiredTuple);
				}
				
				// 处理新到达的元组newTuple
				if(newTuple != null){
					handleNewTuple(newTuple, expiredTuple);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 处理过期元组expiredTuple
	 * 如果expiredTuple ∈ LSP，则从LSP中删除expiredTuple；
	 * 如果expiredTuple不属于LSP，则do nothing
	 * 
	 * @param expiredTuple
	 */
	private void handleExpiredTuple(SkyTuple expiredTuple){
		
		/*
		 *  localSP_list以逆于SkyTuple自然顺序的序组织：比较新的元组在head(大id)，比较旧的元组在tail(小id)，
		 *  将localSP_list做descendingIterator的原因是：先处理比较旧的元组（旧元组比较容易是过期元组）
		 */
		Iterator<SkyTuple> iter = localSP_list.descendingIterator();
		while(iter.hasNext()){
			if(iter.next().getTupleID() == expiredTuple.getTupleID()){
				iter.remove();
				break;
			}
		}
	}
	
	/**
	 * 处理新到达的元组newTuple
	 * 新到达元组可能∈LSP，也可能∈CSP；即使∈LSP，也必须到Merger中继续比较才能确定它是否∈GSP
	 * 
	 * @param newTuple
	 */
	private void handleNewTuple(SkyTuple newTuple, SkyTuple expiredTuple){
		
		String mark = "";										// 标志该newTuple究竟是lsp还是csp
		long latestDominateId = -1;								// 支配该newTuple的旧元组中最新的元组id（如果newTuple∈lsp，则这个值为-1）
		LinkedList<Long> dominateSet = new LinkedList<Long>();	// localSP_list中被newTuple支配的元组集合（只保存id，如果newTuple）
		
		if(!localSP_list.isEmpty()){
			
			boolean isLSP = true;
			Iterator<SkyTuple> iter = localSP_list.listIterator(0);
			while(iter.hasNext()){
				SkyTuple tempTuple = iter.next();
				int isDominate = IsDominate.dominateBetweenTuples(newTuple, tempTuple);
				
				// 如果newTuple支配tempTuple
				if(isDominate == 0){									
					dominateSet.offer(tempTuple.getTupleID());	// 把tempTuple的id加入newTuple的dominateSet
					iter.remove();								// 把tempTuple从lsp_list中删除
					continue;									// newTuple继续与lsp_list中的下一个元组比较
				}
				// 如果newTuple被tempTuple支配
				else if(isDominate == 1){
					isLSP = false;								// newTuple不可能成为lsp
					latestDominateId = tempTuple.getTupleID();	// 标志newTuple的latestDominateId为支配它的tempTuple的id
					break;										// 结束while循环
				}
				// 如果newTuple与tempTuple互不支配
				else if(isDominate == 2){
					continue;									// newTuple继续与lsp_list中的下一个元组比较
				}
			}
			
			if(isLSP == true){
				mark = "lsp";									// newTuple标识为lsp
				this.localSP_list.offerFirst(newTuple);		// newTuple插入lsp_list的表头
			}else{
				mark = "csp";									// newTuple标识为lsp
			}
			
			ComboTuple cTuple = new ComboTuple(mark, newTuple, expiredTuple, dominateSet, latestDominateId);
			try {
				
				/*
				 * 把CTuple插入submitQueue，应该以一种CTuple自然序的方式插入(id小的在队列头，id大的在队列尾)
				 * TO DO 如何排个序呢?
				 */
				submitQueue.put(cTuple);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}else{
			this.localSP_list.offerFirst(newTuple);
		}
	}

}
