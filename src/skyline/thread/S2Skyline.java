package skyline.thread;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import skyline.util.Constants;

import skyline.algorithms.IsDominate;
import skyline.model.SkyTuple;
import skyline.model.MutaTuple;

/**
 * 串行的数据流Skyline查询算法，是Lazy算法的变种
 * 主要开销在于CSP的维护，以及expiredTuple产生后，如何从CSP中找出合格的点并插入GSP的过程
 * @author Administrator
 *
 */
public class S2Skyline implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer;					// 取数据的地方，由GenDataThread产生数据
	private LinkedList<SkyTuple> globalWindow_list;					// 维护的全局滑动窗口
	private LinkedList<SkyTuple> globalSP_list;						// 维护的全局Skyline集合（当前滑动窗口的Skyline集合）
	private LinkedList<MutaTuple> candidateSP_list;					// 维护的候选Skyline集合（当前滑动窗口中被支配但比较新的节点）
	
	/**
	 * S2Skyline类的带参数的构造函数
	 * @param tupleBuffer			取数据的地方
	 * @param globalWindow_list
	 * @param globalSP_list
	 * @param candidateSP_list
	 */
	public S2Skyline(BlockingQueue<SkyTuple> tupleBuffer){
		
		this.tupleBuffer = tupleBuffer;
		this.globalWindow_list = new LinkedList<SkyTuple>();
		this.globalSP_list = new LinkedList<SkyTuple>();
		this.candidateSP_list = new LinkedList<MutaTuple>();
	}
	
	/**
	 * 带参数的构造函数2，模拟滑动窗口已经被装满的情况，且当前globalSkylines和candidateSkylines中已经有值
	 * @param tupleBuffer
	 * @param globalWindow_list
	 * @param globalSP_list
	 * @param candidateSP_list
	 */
	public S2Skyline(BlockingQueue<SkyTuple> tupleBuffer, LinkedList<SkyTuple> globalWindow_list, 
			LinkedList<SkyTuple> globalSP_list, LinkedList<MutaTuple> candidateSP_list){
		
		this.tupleBuffer = tupleBuffer;
		this.globalWindow_list = globalWindow_list;
		this.globalSP_list = globalSP_list;
		this.candidateSP_list = candidateSP_list;
	}
	
	/**
	 * 采用实现Runnable接口来实现线程功能，需要实现一个run()方法
	 */
	@Override
	public void run() {

		long queryGranularity = 0L;								// 用于结果统计的计数，记录1000个tuple的处理时间
		long time0 = 0L, time1 = 0L, time2 = 0L, time3 = 0L;
		
		while(true){
			
			if(queryGranularity == 0){
				time0 = System.currentTimeMillis();
			}
			if(queryGranularity == Constants.QueryGran){
				time1 = System.currentTimeMillis();
				// 记录日志
				Logger.getLogger("S2Skyline").info("The total time to process " + Constants.QueryGran + " tuples: " + (time1-time0) + 
						" ms.\n" + " time0:" + time0 + " time1:" + time1 + " time2:" + time2 + " time3:" + time3);
				
				break;											// 结束while循环，线程终止
			}
			
			try{
				// 从tupleBuffer中取一个newTuple(线程安全,若取不到元素，则阻塞)
				SkyTuple newTuple = tupleBuffer.take();
				queryGranularity ++;
				
				/*
				 * 判断滑动窗口的情况（未满或已满）;如果滑动窗口已满，则产生一个过期元组
				 * 将过期元组从全局滑动窗口globalSlidingWindow中删除(链表头的元组)
				 */
				SkyTuple expiredTuple = null;
				if(globalWindow_list.size() >= Constants.GlobalWindowSize){
					
					expiredTuple = globalWindow_list.poll();
				}
				
				// 将newTuple插入全局滑动窗口globalSlidingWindow
				globalWindow_list.offer(newTuple);
				
				// 处理过期元组expiredTuple
				if(expiredTuple != null){
					handleExpiredTuple(expiredTuple);
				}
				time2 = System.currentTimeMillis();
				
				// 处理新到达的元组newTuple
				if(newTuple != null){
					handleNewTuple(newTuple);
				}
				time3 = System.currentTimeMillis();
				
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 处理过期元组expiredTuple
	 * 如果expiredTuple ∈ GSP，则首先删除GSP中的过期元组，然后找出CSP中dominateID为expiredTuple.ID的元组并插入到GSP中；
	 * 如果expiredTuple不属于GSP，则do nothing
	 * 
	 * @param expiredTuple
	 */
	private void handleExpiredTuple(SkyTuple expiredTuple){
		
		boolean isInGSP = expireFromGSP(globalSP_list, expiredTuple);

		if(isInGSP == true){
			boolean isSort = false;												// 判断是否需要对subGSP做排序(只有插入元组后才会重新排序)
			Iterator<MutaTuple> it = candidateSP_list.listIterator(0);
			while(it.hasNext()){
				MutaTuple temp = it.next();
				if(temp.getDominateID() == expiredTuple.getTupleID()){
					globalSP_list.offer(temp.getSkyTuple());					// 将满足条件的t插入GlobalSkylines的链表尾
					it.remove();												// 从CSP中删除升级为GSP的tuple
					isSort = true;
				}
			}
			if(isSort == true){
				Collections.reverse(globalSP_list);								// 把GlobalSkylines做一个排序（逆于SkyTuple自然序的顺序）
			}
		}
	}
	
	/**
	 * 判断一个应该过期的SkyTuple元组expiredTuple是否属于指定的链表gsp_list,
	 * 如果属于,从gsp_list中删除该expiredTuple,并返回true;否则,返回false
	 * 
	 * @param globalSP_list		指定的SkyTuple链表
	 * @param expiredTuple		待判定的过期元组
	 * @return					若tuple属于tuple_list,返回true;否则,返回false
	 */
	private boolean expireFromGSP(LinkedList<SkyTuple> globalSP_list, SkyTuple expiredTuple)
	{
		boolean isInGSP = false;												// 标志expiredTuple是否属于globalSP_list
		Iterator<SkyTuple> iter = globalSP_list.descendingIterator();
		while(iter.hasNext()){
			if(iter.next().getTupleID() == expiredTuple.getTupleID()){
				iter.remove();
				isInGSP = true;
				break;
			}
		}
		return isInGSP;
	}

	/**
	 * 处理新到达的元组newTuple
	 * @param newTuple
	 */
	private void handleNewTuple(SkyTuple newTuple){
		
		if(globalSP_list != null){
			boolean isGSP = true;													// 判断newTuple是否属于GSP的标志，初始为true
			long latestDominatedID = -1;											// 当前滑动窗口中支配newTuple的比它旧的最新元组的ID，初始化为-1
			
			Iterator<SkyTuple> iter = globalSP_list.listIterator(0);				// GSP的链表头为id较大的元组
			while(iter.hasNext()){
				SkyTuple tuple = iter.next();
				
				int isDominate = IsDominate.dominateBetweenTuples(newTuple, tuple);	// 判断newTuple与GSP中元组的支配关系
				
				// 返回0表示newTuple支配tuple, 删除GSP中被newTuple支配的元组, 继续与GSP中的下一元组比较
				if(isDominate == 0){
					iter.remove();
					continue;
				}
				// 返回1表示newTuple被tuple支配, 更新newTuple的latestDominateId值, break from while
				else if(isDominate == 1){
					isGSP = false;
					latestDominatedID = tuple.getTupleID();
					break;
				}
				// 返回2表示互不支配, 继续与GSP中的下一元组比较
				else if(isDominate == 2){
					continue;
				}
			}
			
			// isGSP为TRUE表示newTuple属于GSP，则肯定不会被CSP中的任意元组支配
			if(isGSP == true){													
				globalSP_list.offerFirst(newTuple);									// 把newTuple插入GSP
				removeFromCandidateSkylines(newTuple);								// 直接删除CSP中被newTuple支配的元组
				
			}else{
				insertIntoCandidateSkylines(newTuple, latestDominatedID);
			}
			
		}else{
			globalSP_list.offerFirst(newTuple);
		}
	}
	
	/**
	 * 将newTuple插入candidateSkylines(为null就直接插入，否则就先处理再插入)
	 * 遍历candidateSkylines，删除所有被newTuple支配的元组；
	 * 如果有元组支配newTuple，则（在需要的时候）更新newTuple的latestDominateTupleID
	 * 
	 * @param newTuple
	 * @param latestDominatedTupleID
	 */
	private void insertIntoCandidateSkylines(SkyTuple newTuple, long latestDominateID){
		
		MutaTuple mutaTuple = new MutaTuple(newTuple, latestDominateID);
		
		if(candidateSP_list != null){
			
			Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
			while(iter.hasNext()){
				MutaTuple tempTuple = iter.next();
				int isDominate = IsDominate.dominateBetweenTuples(newTuple, tempTuple.getSkyTuple());
				
				// 返回0表示newTuple支配tempTuple,从CSP中删除被newTuple支配的元组, 继续与CSP中的下一元组比较
				if(isDominate == 0){
					iter.remove();
					continue;
				}
				// 返回1表示newTuple被tempTuple支配, 更新newTuple的latestDominateTupleID, 结束while循环
				else if(isDominate == 1){
					
					if(tempTuple.getSkyTuple().getTupleID() > latestDominateID){
						latestDominateID = tempTuple.getSkyTuple().getTupleID();
						mutaTuple.setDominateID(latestDominateID);
						break;
					}
				}
				// 返回2表示newTuple与tempTuple互不支配, 继续与CSP中的下一元组比较
				else if(isDominate == 2){
					continue;
				}
			}
		}
		candidateSP_list.offerFirst(mutaTuple);											// 把newTuple插入CSP
	}

	/**
	 * 从CandidateSkylines中删除被newTuple支配的元组,已确定newTuple是GSP
	 * @param newTuple
	 */
	private void removeFromCandidateSkylines(SkyTuple newTuple){
		
		Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
		while(iter.hasNext()){
			MutaTuple tempTuple = iter.next();
			int isDominate = IsDominate.dominateBetweenTuples(newTuple, tempTuple.getSkyTuple());
			
			// 返回0表示newTuple支配tempTuple
			if(isDominate == 0){
				iter.remove();													// 从CSP中删除被newTuple支配的元组
			}
		}
	}
}
