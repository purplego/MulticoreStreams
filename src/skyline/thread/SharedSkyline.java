package skyline.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import skyline.algorithms.IsDominate;
import skyline.model.MutaTuple;
import skyline.model.SkyTuple;
import skyline.util.Constants;

/**
 * SharedSkyline类，对S2Skyline的改进，将其中对GSP和CSP的处理并行化
 * 即S2Skyline 算法的多线程版本，多线程处理GSP和CSP
 * @author 幻
 *
 */
public class SharedSkyline implements Runnable {
	
	private int numSubGSP = Constants.NumSUBGSP;
	private int numSubCSP = Constants.NumSUBCSP;

	private BlockingQueue<SkyTuple> tupleBuffer;			// 取数据的地方,由FetchDataThread从文件中读取数据
	private LinkedList<SkyTuple> globleWindow_list;			// 维护的全局滑动窗口
	private ArrayList<LinkedList<SkyTuple>> subGSPs;		// 多线程处理GSP,将GSP分为多个子GSP,GSP中的tuple按照tupleID 取模加入
	private ArrayList<LinkedList<MutaTuple>> subCSPs;		// 多线程处理CSP,将CSP分为多个子CSP,按latestDominateId组织
	
	/**
	 * SharedSkyline类的带参数的构造函数
	 * @param tupleBuffer
	 */
	public SharedSkyline(BlockingQueue<SkyTuple> tupleBuffer) {
		super();
		this.tupleBuffer = tupleBuffer;
		this.globleWindow_list = new LinkedList<SkyTuple>();
		this.subGSPs = new ArrayList<LinkedList<SkyTuple>>();
		this.subCSPs = new ArrayList<LinkedList<MutaTuple>>();
		
		for(int i = 0 ;i< numSubGSP ;i++) {
			subGSPs.add(new LinkedList<SkyTuple>());
		}
		
		for(int i = 0 ; i< numSubCSP ; i++) {
			subCSPs.add(new LinkedList<MutaTuple>());
		}
	}
	
	/**
	 * SharedSkyline类的带参数的构造函数2,模拟滑动窗口已经被装满的情况,且当前GSP和CSP中已经有值
	 * @param tupleBuffer
	 * @param globleSlidingWindow
	 * @param subGSPs
	 * @param subCSPs
	 */
	public SharedSkyline(BlockingQueue<SkyTuple> tupleBuffer, LinkedList<SkyTuple> globleSlidingWindow,
			ArrayList<LinkedList<SkyTuple>> subGSPs, ArrayList<LinkedList<MutaTuple>> subCSPs) {
		super();
		this.tupleBuffer = tupleBuffer;
		this.globleWindow_list = globleSlidingWindow;
		this.subGSPs = subGSPs;
		this.subCSPs = subCSPs;
	}

	@Override
	public void run() {

		boolean isRunning = true;							// 控制是否结束线程
		long queryGranularity = 0L;							// 用于结果统计的计数，记录1000个tuple的处理时间
		long time0 = 0L, time1 = 0L, time2 = 0L, time3 = 0L;
		
		while(isRunning) {
			if(queryGranularity == 0){
				time0 = System.currentTimeMillis();
			}
			if(queryGranularity == Constants.QueryGran){
				time1 = System.currentTimeMillis();
				isRunning = false;							// 线程终止
				
				// 记录日志
				Logger.getLogger("SharedSkyline").info("The total time to process " + Constants.QueryGran + " tuples: " + (time1-time0) + " ms.\n");
			}
			
			try {
				SkyTuple newTuple = tupleBuffer.take();		// 从tupleBuffer中取一个newTuple
				queryGranularity ++;
				
				SkyTuple expiredTuple = null;
				
				// 若有数据过期，则从滑动窗口中溢出过期数据expiredTuple
				if(globleWindow_list.size() >= Constants.GlobalWindowSize){
					expiredTuple = globleWindow_list.poll();
				}
				
				// 向滑动窗口中插入新到达数据newTuple
				globleWindow_list.offer(newTuple);
				
				// 处理过期元组expiredTuple
				if(expiredTuple != null) {
					handleExpiredTuple(expiredTuple);
				}
				
				// 处理新到达的元组newTuple
				if(newTuple != null){
					handleNewTuple(newTuple);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 处理过期元组expiredTuple,主要是从CSP中找出合格的元组升级为GSP：
	 * 首先检查该元素是否在GSP中，如果expiredTuple不在GSP中，不做任何操作(CSP中不可能有被expiredTuple支配的元组)
	 * 如果expiredTuple在GSP中,则
	 * (1)删除GSP中 保存的expiredTuple,
	 * (2)从CSP中找出其latestDominateId为expiredTuple的元组加入到GSP中,
	 * (3)对修改过的GSP按要求排序
	 * 
	 * @param expiredTuple
	 */
	private void handleExpiredTuple(SkyTuple expiredTuple)
	{
		int indexInGSP = (int) (expiredTuple.getTupleID() % numSubGSP);		// 根据元组id判断expiredTuple应该属于哪一个subGSP[i]
		
		boolean isInGsp = expireFromGSP(subGSPs.get(indexInGSP), expiredTuple);	// 判断该expiredTuple是否在对应的subGSP中,并删除过期元组
		if(isInGsp == true) {
			
			/*
			 * 把多个subCSP按照dominateId % numSubCSP组织在subCSPS这个arrayList中,
			 * 只要找到某个dominateId % numSubCSP的subCSPs[i],即可找到所以dominateId为expiredTuple.id的元组
			 * 无需遍历多个subCSPs[i]
			 */
			int indexInCSP = (int) (expiredTuple.getTupleID() % numSubCSP);	// 标志dominateId为expiredTuple.id的那个subCSP[i]
			
			int index = -1;														// 标志被升级的CSP tuple应插入哪个subGSP[i]
			LinkedList<Integer> index_list = new LinkedList<Integer>();
			Iterator<MutaTuple> it = subCSPs.get(indexInCSP).listIterator(0);
			while(it.hasNext()){
				MutaTuple temp = it.next();
				if(temp.getDominateID() == expiredTuple.getTupleID()){
					index = (int) (temp.getSkyTuple().getTupleID() % numSubGSP);
					subGSPs.get(index).offer(temp.getSkyTuple());				// 将满足条件的t插入subGSP[i]的链表尾
					it.remove();												// 从CSP中删除升级为GSP的tuple
					index_list.offer(index);
				}
			}
			for(int sortIndex: index_list){
				Collections.reverse(subGSPs.get(sortIndex));						// 把GlobalSkylines做一个排序（逆于SkyTuple自然序的顺序）
			}
		}
	} 
	
	/**
	 * 多线程并行的处理NewTuple,
	 * 首先,并行的判断newTuple与各个subGSP[i]的支配关系：
	 * (1)如果NewTuple被支配,则更新newTuple的latestDominateId,将newTuple(构建MutaTuple)插入CSP添加到CSP中,
	 * (2)如果NewTuple不被支配,则将其添加到对应的GSP中(根据newTupleID % numSubGSP找到正确的index)
	 * 
	 * 然后,无论newTuple是否属于GSP,都需要删除各个subCSP[i]中被newTuple支配的元组
	 * @param newTuple
	 */
	private void handleNewTuple(SkyTuple newTuple) {
		/*
		 * 多个subGSPs[i]同时并行处理,所以使用了多线程
		 */
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<Long>> dominateId_list = new ArrayList<Future<Long>>();	// 存放各线程返回的关于newTuple的latestDominateId信息
		for(int i = 0 ;i < numSubGSP ; i++) {
			dominateId_list.add(exec.submit(new GspHandlerThread(subGSPs.get(i), newTuple)));
		}
		
		long latestDominateId = -1;													// newTuple的latestDominateId
		
		for(Future<Long> tmpDominateId : dominateId_list) {
			try {
				// Future类的get()方法获取线程执行的返回值: 如果线程没执行完,则阻塞,等待计算完成,然后获取其结果
				if(tmpDominateId.get() > latestDominateId)
					latestDominateId = tmpDominateId.get();							// 获取各处理线程返回的最大的latestDominateId值
			
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		/*
		 * latestDominateId == -1,表示newTuple不被各subGSP中的任何元素支配
		 * 该newTuple是全局Skyline,应该添加到正确的subGSP[i]中(按照取模运算进行添加)
		 * 同时还应该删除各subCSP中被newTuple支配的元组
		 */
		if(latestDominateId == -1) {
			int indexInGSP = (int) (newTuple.getTupleID() % numSubGSP);
			subGSPs.get(indexInGSP).offerFirst(newTuple);
			
			for(int i = 0; i< numSubCSP; i++){
				exec.execute(new CspUpdateThread(subCSPs.get(i), newTuple));
			}
		}
		// newTuple被某个当前的全局skyline点支配,将newTuple插入到CSP中
		else {
			insertIntoCSP(newTuple, latestDominateId);
		}
	}
	
	/**
	 * 将新元素newTuple插入到CSP中,根据newTuple的(latestDominateId % numSubCSP)找到正确的subCSP[i]
	 * 同时删除各subCSP中被newTuple支配的元组
	 * @param tuple
	 * @param latestDominateID
	 */
	private void insertIntoCSP(SkyTuple tuple, long latestDominateID) {
		
		/*
		 * 多个subCSPs[i]同时并行处理,所以使用了多线程
		 */
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<Long>> dominateId_list= new ArrayList<Future<Long>>();	// 存放各线程返回的关于newTuple的latestDominateId信息
		for(int i = 0 ;i < numSubCSP ; i++) {
			dominateId_list.add(exec.submit(new CspHandlerThread(subCSPs.get(i), tuple, latestDominateID)));
		}
		
		long latestDominateId_new = latestDominateID;
		for(Future<Long> tmpDominateId : dominateId_list) {
			try {
				// Future类的get()方法获取线程执行的返回值: 如果线程没执行完,则阻塞,等待计算完成,然后获取其结果
				if(tmpDominateId.get() > latestDominateId_new)
					latestDominateId_new = tmpDominateId.get();					// 更新newTuple的latestDominateId
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		/*
		 * 根据newTuple的latestDominateId确定正确的subCSP[i],并把newTuple插入正确的subCSP[i]
		 */
		int index = (int) (latestDominateId_new % numSubCSP);					
		subCSPs.get(index).offerFirst(new MutaTuple(tuple, latestDominateId_new));
	}
	
	
	/**
	 * 判断一个应该过期的SkyTuple元组expiredTuple是否属于指定的链表gsp_list,
	 * 如果属于,从gsp_list中删除该expiredTuple,并返回true;否则,返回false
	 * 
	 * @param gsp_list			指定的SkyTuple链表
	 * @param expiredTuple		待判定的过期元组
	 * @return					若tuple属于tuple_list,返回true;否则,返回false
	 */
	private boolean expireFromGSP(LinkedList<SkyTuple> gsp_list, SkyTuple expiredTuple)
	{
		boolean isMine = false;										// 标志expiredTuple是否属于gsp_list
		Iterator<SkyTuple> iter = gsp_list.descendingIterator();
		while(iter.hasNext()){
			if(iter.next().getTupleID() == expiredTuple.getTupleID()){
				iter.remove();
				isMine = true;
				break;
			}
		}
		return isMine;
	}
}

/**
 * GspHandlerThread类,线程实现
 * 
 * 测试newTuple与指定subGSP(全局skyline链表的一部分)中各元组的支配关系:
 * (1)如果newTuple被支配subGSP中的某个skyline元组支配,则返回支配newTuple的最新tuple的ID作为其latestDominateId
 * (2)如果newTuple不被subGSP中的任意元组支配,则返回-1作为其latestDominateId
 * (3)如果subGSP中有元组被newTuple支配,则删除subGSP中被newTuple支配的元组
 * 
 * 返回值: newTuple的latestDominateId
 * 
 * @author 幻
 */
class GspHandlerThread implements Callable<Long>
{
	LinkedList<SkyTuple> subGSP;
	SkyTuple newTuple;
	
	/**
	 * The constructor of the GspHandlerThread class
	 * @param subGSP
	 * @param newTuple
	 */
	GspHandlerThread(LinkedList<SkyTuple> subGSP,SkyTuple newTuple) {
		this.subGSP =subGSP;
		this.newTuple = newTuple;
	}
	
	@Override
	public Long call() throws Exception {

		long latestDominatedID = -1;
		if(subGSP != null) {
			Iterator<SkyTuple> iter = subGSP.listIterator(0);
			while(iter.hasNext()) {
				SkyTuple tuple = iter.next();
				/*
				 * 判断newTuple与GSP中元组的支配关系
				 * 返回0表示newTuple支配tuple;返回1表示newTuple被tuple支配;2表示互不支配
				 */
				int flag = IsDominate.dominateBetweenTuples(newTuple,tuple);
				
				// 返回0表示newTuple支配tuple
				if(flag == 0) {
					iter.remove();
					continue;
				}
				// 返回1表示newTuple被tuple支配
				else if(flag == 1 ) {
					latestDominatedID  = tuple.getTupleID();
					break;					//GSP有序,所以可以直接结束比较
				}
				// 返回2表示互不支配
				else if(flag == 2) {
					continue;
				}
			}
		}

		return latestDominatedID;
	}
}

/**
 * CspHandlerThread类,线程实现
 * 
 * 条件: newTuple不属于GSP,需要插入某个subCSP
 * 功能: 测试newTuple与指定subCSP中元组的支配关系,
 * (1)如果newTuple支配subCSP中的元组,则从被subCSP中删除被支配的元组
 * (2)如果newTuple被subCSP中的元组支配,则更新newTuple的latestDominateId
 * 返回值: newTuple的latestDominateId
 * 
 * @author 幻
 *
 */
class CspHandlerThread implements Callable<Long>
{
	private LinkedList<MutaTuple > subCSP;
	private SkyTuple newTuple;
	private long latestDominateID ;
	
	/**
	 * The constructor of the class CspHandlerThread
	 * @param subCSP
	 * @param newTuple
	 * @param latestDominateID
	 */
	CspHandlerThread(LinkedList<MutaTuple> subCSP, SkyTuple newTuple, long latestDominateID)
	{
		this.subCSP = subCSP;
		this.newTuple = newTuple;
		this.latestDominateID = latestDominateID;
	}
	@Override
	public Long call(){
		
		if(subCSP != null){
			Iterator<MutaTuple> iter = subCSP.listIterator(0);
			while(iter.hasNext()){
				MutaTuple tempTuple = iter.next();
				int flag = IsDominate.dominateBetweenTuples(newTuple, tempTuple.getSkyTuple());
				
				// 返回0表示newTuple支配tuple
				if(flag == 0){
					iter.remove();													// 从CSP中删除被newTuple支配的元组
				}
				// 返回1表示newTuple被tuple支配
				else if(flag == 1){
					if(tempTuple.getSkyTuple().getTupleID() > latestDominateID){
						latestDominateID = tempTuple.getSkyTuple().getTupleID();	// 更新newTuple的latestDominateTupleID
						break;
					}
				}
			}
		}
		return latestDominateID;
	}
}

/**
 * CspUpdateThread类: 线程实现
 * 条件: 一个newTuple已经知道是属于GSP了,继续从各subCSP中删除被newTuple支配的元组
 * 
 * @author 幻
 *
 */
class CspUpdateThread implements Runnable
{
	private LinkedList<MutaTuple> subCSP;
	private SkyTuple newTuple;
	
	// The constructor of the class CspUpdateThread
	CspUpdateThread(LinkedList<MutaTuple> subCSP, SkyTuple newTuple)
	{
		this.subCSP = subCSP;
		this.newTuple = newTuple;
	}
	@Override
	public void run() {
	
		Iterator<MutaTuple> iter = subCSP.listIterator(0);
		while(iter.hasNext()){
			MutaTuple tempTuple = iter.next();
			int flag = IsDominate.dominateBetweenTuples(newTuple, tempTuple.getSkyTuple());
			if(flag == 0){
				iter.remove();								// 从CSP中删除被newTuple支配的元组(已确定newTuple属于GSP时才会调用该方法)
			}
		}
	}
}
