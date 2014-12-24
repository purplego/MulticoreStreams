 package skyline.thread;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Logger;

import skyline.algorithms.IsDominate;
import skyline.model.ComboTuple;
import skyline.model.MutaTuple;
import skyline.model.SkyTuple;
import skyline.util.Constants;

public class PsMerger implements Runnable {

	private int numSubGSP = Constants.NumPsExecutor;
	private int numSubCSP = Constants.NumSUBCSP;
	
	private BlockingQueue<ComboTuple> submitQueue;					// 取数据的地方，由PsExecutor产生数据
	private ArrayList<LinkedList<SkyTuple>> subGSPs;				// 维护的全局Skyline的子集
	private ArrayList<LinkedList<MutaTuple>> subCSPs;				// 维护的全局CSP的子集
	
	/**
	 * Merger类的带参数的构造函数
	 * @param submitQueue	取数据的地方
	 */
	public PsMerger(BlockingQueue<ComboTuple> submitQueue){
		this.submitQueue = submitQueue;
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
	 * PsMerger类的带参数的构造函数2,模拟滑动窗口已经被装满的情况
	 * @param submitQueue
	 * @param subGSPs
	 * @param subCSPs
	 */
	public PsMerger(BlockingQueue<ComboTuple> submitQueue, ArrayList<LinkedList<SkyTuple>> subGSPs, 
			ArrayList<LinkedList<MutaTuple>> subCSPs){
		
		this.submitQueue = submitQueue;
		this.subGSPs = subGSPs;
		this.subCSPs = subCSPs;
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
			if(queryGranularity == Constants.QueryGran){
				time1 = System.currentTimeMillis();
				Logger.getLogger("PsSkyline").info("Merger Executor: " + 
						" The total time to process " + Constants.QueryGran + " tuples: " + (time1-time0) + " ms.");
			
				break;
			}
			
			try {
				// 从submitQueue中读取一个comboTuple(线程安全,若取不到元素，则阻塞)
				ComboTuple comboTuple = submitQueue.take();
				queryGranularity ++;
				
				// 处理过期元组
				if(comboTuple.getExpiredTuple() != null){
					handleExpiredTuple(comboTuple);
				}
				
				// 处理新到达元组: mark为lsp
				if(comboTuple.getMark().equals("lsp")){
					handleLspTuple(comboTuple);
				}
				
				// 处理新到达元组：mark为csp
				else if(comboTuple.getMark().equals("csp")){
					handleCspTuple(comboTuple);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally{
				System.out.println("This is the end of the PsMerger thread.");
			}
		}
	}
	
	/**
	 * 处理过期元组的影响: 先从id相同的subGSP中查找过期元组,
	 * 如果有该过期元组，那么从该subGSP中删除过期元组，并检查各subCSP中是否有元组的latestDominateId为expiredTuple.id
	 * (把subCSP按照latestDominateId % numSubCSP来组织,可以减少对多个subCSP的遍历次数)
	 * 
	 * 如果没有该过期元组，那么subCSP中肯定也没有被该expiredTuple支配的元组，do nothing
	 * @param comboTuple
	 */
	public void handleExpiredTuple(ComboTuple comboTuple){
		
		SkyTuple expiredTuple = comboTuple.getExpiredTuple();
		int indexInGSP = (int) (expiredTuple.getTupleID() % numSubGSP);
		
		boolean isInGSP = expiredFromMyGSP(subGSPs.get(indexInGSP), expiredTuple);
		
		// 只有过期元组属于id相同的subGSP时，才需要检查各subCSP中被过期元组支配的元组
		if(isInGSP == true){
		
			int indexUpdate = -1;												// 标志被升级的CSP tuple应插入哪个subGSP[i]
			LinkedList<Integer> indexUpdate_list = new LinkedList<Integer>();	// 标志updateTuple都插入了那些subGSP
			
			int indexInCSP = (int) (expiredTuple.getTupleID() % numSubCSP);
			Iterator<MutaTuple> it = subCSPs.get(indexInCSP).listIterator(0);
			while(it.hasNext()){
				MutaTuple mutaTuple = it.next();
				if(mutaTuple.getDominateID() == expiredTuple.getTupleID()){
					SkyTuple updateTuple = mutaTuple.getSkyTuple();
					indexUpdate = (int) (updateTuple.getTupleID() % numSubGSP);
					
					subGSPs.get(indexUpdate).offer(updateTuple);
					it.remove();
					
					// 避免重复排序
					if(indexUpdate_list.contains(indexUpdate)){
						indexUpdate_list.offer(indexUpdate);
					}
				}
			}
			for(int sortIndex: indexUpdate_list){
				Collections.reverse(subGSPs.get(sortIndex));					// 把GlobalSkylines做一个排序（逆于SkyTuple自然序的顺序）
			}
		}
	}
	
	/**
	 * 判断过期元组是否存在于它自己的那个subGSP中,若存在,删除过期元组并返回true;否则,返回false
	 * @param mySubGSP_list			理论上过期元组属于的那个subGSP
	 * @param expiredTuple		过期元组expiredTuple
	 * @return
	 */
	public boolean expiredFromMyGSP(LinkedList<SkyTuple> mySubGSP_list, SkyTuple expiredTuple){
		
		boolean isInGSP = false;
		
		// 从指定的subGSP中删除过期元组（若有）
		Iterator<SkyTuple> iter = mySubGSP_list.descendingIterator();
		while(iter.hasNext()){
			SkyTuple tuple = iter.next();
			if(tuple.getTupleID() == expiredTuple.getTupleID()){
				iter.remove();
				isInGSP = true;
				break;
			}
		}
		
		return isInGSP;
	}
	
	/**
	 * 
	 * 如果comoTuple中的newTuple属于LSP，则首先从id相同的subGSP中删除dominateSet包含的元组；
	 * 该newTuple还需与其他subGSP中的元组比较；如果该newTuple被其他subGSP中的元组支配，则将其放入subCSP，并更新latestDominateId
	 * 插入subCSP的过程中，同时检查各subCSP的元组是否被该newTuple支配
	 * 
	 * 如果该newTuple支配其他subGSP中的元组，则删除这些被支配的元组，并检查newTuple与subCSP中元组的支配关系
	 * 
	 * @param comboTuple
	 */
	public void handleLspTuple(ComboTuple comboTuple){
		
		boolean isGSP = true;														// 标志newTuple是否能最终成为GSP中的元组，初始化默认为true
		SkyTuple newTuple = comboTuple.getNewTuple();
		int indexInGSP = (int) (newTuple.getTupleID() % numSubGSP);
	
		for(int i = 0; i < numSubGSP; i++){
			
			// 与id不同的subGSP中的元组比较
			if(i != indexInGSP){
				isGSP = dominateTestWithOtherGSP(i, comboTuple);
				
			}
			// 从id相同的subGSP中删除dominateSet包含的元组
			else if(i == indexInGSP){
				removeDominateSetFromMyGSP(i, comboTuple);
			}
		}
		
		// newTuple插入指定的subGSP或指定的CSP
		if(isGSP == true){
			subGSPs.get(indexInGSP).offerFirst(newTuple);
		}
		else{
			insertIntoCSP(comboTuple);
		}
	}
	
	/**
	 * 判断newTuple与其他id不同的subGSP的支配关系
	 * 
	 * @param index			待判定的subGSP索引号
	 * @param comboTuple	待判定的comboTuple
	 * @return				comboTuple.getNewTuple()对该subGSP[index]而言是否是GSP
	 */
	public boolean dominateTestWithOtherGSP(int index, ComboTuple comboTuple){
		boolean isGSP = true;													// 标志newTuple是否能最终成为GSP中的元组，初始化默认为true
		
		Iterator<SkyTuple> iter = subGSPs.get(index).listIterator(0);
		while(iter.hasNext()){													// 遍历subGSP[i]
			SkyTuple tmpTuple = iter.next();
			int isDominate = IsDominate.dominateBetweenTuples(comboTuple.getNewTuple(), tmpTuple);
			
			// 如果newTuple支配tmpTuple
			if(isDominate == 0){
				iter.remove();													// 把tmpTuple从subGSP[i]中删除
				continue;														// newTuple继续与subGSP[i]中的下一个tmpTuple元组比较
			}
			// 如果newTuple被tmpTuple支配
			else if(isDominate == 1){
				isGSP = false;													// newTuple不可能成为GSP
				if(comboTuple.getLatestDominateID() < tmpTuple.getTupleID()){
					comboTuple.setLatestDominateID(tmpTuple.getTupleID());		// 更新newTuple的latestDominateId
				}
				break;															// 结束while循环
			}
			// 如果newTuple与tmpTuple互不支配
			else if(isDominate == 2){
				continue;														// newTuple继续与subGSP[i]中的下一个元组tmpTuple比较
			}
		}
		return isGSP;
	}
	
	/**
	 * 从id相同的subGSP中删除newTuple支配的元组，即comboTuple.dominateSet中的元组
	 * 
	 * @param indexInGSP
	 * @param comboTuple
	 */
	public void removeDominateSetFromMyGSP(int indexInGSP, ComboTuple comboTuple){
		
		for(long tID: comboTuple.getDominatedSet()){
			
			Iterator<SkyTuple> iter = subGSPs.get(indexInGSP).descendingIterator();
			while(iter.hasNext()){
				if(iter.next().getTupleID() == tID){
					iter.remove();
					break;							// 与下一个tID比较
				}
			}
		}
	}
	/**
	 * 把一个newTuple插入指定的CSP，同时删除各个CSP中被newTuple支配的元组
	 * 如果有元组支配newTuple，更新其latestDominateId
	 * 
	 * @param comboTuple
	 */
	public void insertIntoCSP(ComboTuple comboTuple){
		
		updateCSP(comboTuple);
		int indexInCSP = (int) (comboTuple.getLatestDominateID() % numSubCSP);
		
		subCSPs.get(indexInCSP).offerFirst(new MutaTuple(comboTuple.getNewTuple(), comboTuple.getLatestDominateID()));
	}
	
	/**
	 * 判断各subCSP中的元组与newTuple的支配关系，删除subCSP中被支配的元组，或者更新newTuple的latestDominateId
	 * 
	 * @param comboTuple
	 */
	public void updateCSP(ComboTuple comboTuple){
		
		for(int i = 0; i < numSubCSP; i++){
			
			Iterator<MutaTuple> iter = subCSPs.get(i).listIterator(0);
			while(iter.hasNext()){
				MutaTuple mutaTuple = iter.next();
				SkyTuple tmpTuple = mutaTuple.getSkyTuple();
				int isDominate = IsDominate.dominateBetweenTuples(comboTuple.getNewTuple(), tmpTuple);
				
				// 如果newTuple支配tmpTuple
				if(isDominate == 0){
					iter.remove();
					continue;
				}
				// 如果newTuple被tmpTuple支配
				else if(isDominate == 1){
					if(comboTuple.getLatestDominateID() < tmpTuple.getTupleID()){
						comboTuple.setLatestDominateID(tmpTuple.getTupleID());		// 更新newTuple的latestDominateId
					}
					break;															// 结束while循环
				}
				// 如果newTuple与tmpTuple互不支配
				else if(isDominate == 2){
					continue;
				}
			}
		}
	}
	
	/**
	 * 如果comoTuple中的newTuple不属于LSP，则属于CSP
	 * 删除各CSP中被该newTuple支配的旧元组
	 * 与其他subGSP中的新元组比较，更新该newTuple的latestDominateId域，将其插入id相同的subCSP中
	 * 
	 * @param comboTuple
	 */
	public void handleCspTuple(ComboTuple comboTuple){
		
		// 与其他subGSP中的元组比较，更新该newTuple的latestDominateId域
		udpateDominateIdWithOhterGSP(comboTuple);
		
		// 删除各subCSP中被该newTuple支配的旧元组，更新newTuple的latestDominateId
		updateCSP(comboTuple);
		
		// 把newTuple与其latestDominateId一起组装成MutaTuple，并插入指定的subCSP中
		int indexInCSP = (int) (comboTuple.getLatestDominateID() % numSubCSP);
		subCSPs.get(indexInCSP).offer(new MutaTuple(comboTuple.getNewTuple(), comboTuple.getLatestDominateID()));
	}
	
	/**
	 * comboTuple.newTuple与其他id不同的subGSP比较，更新newTuple的latestDominateId
	 * 
	 * @param comboTuple
	 */
	public void udpateDominateIdWithOhterGSP(ComboTuple comboTuple){
		
		SkyTuple newTuple = comboTuple.getNewTuple();
		int indexInGSP = (int) (newTuple.getTupleID() % numSubGSP);		// 获取newTuple属于的subGSP的index
		for(int i=0; i<numSubGSP; i++){
			if(i != indexInGSP){										// 与id不同的subGSP做比较
				
				Iterator<SkyTuple> iter = subGSPs.get(i).listIterator(0);
				while(iter.hasNext()){
					SkyTuple gspTuple = iter.next();
					int isDominate = IsDominate.dominateBetweenTuples(newTuple, gspTuple);
					if(isDominate == 1){
						if(comboTuple.getLatestDominateID() < gspTuple.getTupleID()){
							comboTuple.setLatestDominateID(gspTuple.getTupleID());
						}
						break;
					}
					else if(isDominate == 2){
						continue;
					}
				}
			}
		}
	}
	
}
