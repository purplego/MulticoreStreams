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

	private BlockingQueue<SkyTuple> tupleBuffer;					// ȡ���ݵĵط�����GenDataThread��������
	private BlockingQueue<ComboTuple> submitQueue;					// ��mergerThread֮������ݽ�����ͨ������Ŷ���ʵ�ֵ�
	
	private LinkedList<SkyTuple> localWindow_list;					// ά���ľֲ���������
	private LinkedList<SkyTuple> localSP_list;						// ά���ľֲ�Skyline���ϣ���ǰ�������ڵ�Skyline���ϣ�
	
	/**
	 * PsExecutor��Ĵ������Ĺ��캯��

	 * @param tupleBuffer	ȡ���ݵĵط�
	 * @param submitQueue	�ύ���ݵĵط�
	 */
	public PsExecutor(BlockingQueue<SkyTuple> tupleBuffer, BlockingQueue<ComboTuple> submitQueue){
		this.tupleBuffer = tupleBuffer;
		this.submitQueue = submitQueue;
		
		this.localWindow_list = new LinkedList<SkyTuple>();
		this.localSP_list = new LinkedList<SkyTuple>();
	}
	
	/**
	 * PsExecutor��Ĵ������Ĺ��캯��2,ģ�⻬�������Ѿ���װ�������

	 * @param tupleBuffer			��ȡ���ݵĵط�
	 * @param submitQueue			�ύ���ݵĵط�
	 * @param localWindow_list		�ֲ���������
	 * @param localSP_list			�ֲ�Skyline����
	 */
	public PsExecutor(BlockingQueue<SkyTuple> tupleBuffer, BlockingQueue<ComboTuple> submitQueue, 
			LinkedList<SkyTuple> localWindow_list, LinkedList<SkyTuple> localSP_list){
		this.tupleBuffer = tupleBuffer;
		this.submitQueue = submitQueue;
		
		this.localWindow_list = localWindow_list;
		this.localSP_list = localSP_list;
	}
	
	/**
	 * ����ʵ��Runnable�ӿ���ʵ���̹߳��ܣ���Ҫʵ��һ��run()����
	 */
	@Override
	public void run() {

		long queryGranularity = 0L;				// ���ڽ��ͳ�Ƶļ�������¼1000��tuple�Ĵ���ʱ��
		long time0 = 0L, time1 = 0L;
		
		while(true){
			if(queryGranularity == 0){
				time0 = System.currentTimeMillis();
			}
			if(queryGranularity == (Constants.QueryGran/Constants.NumPsExecutor)){
				time1 = System.currentTimeMillis();

				Logger.getLogger("PsSkyline").info("ExecutorID: " + (Thread.currentThread().getName()) + " The total time to process " + 
						(Constants.QueryGran/Constants.NumPsExecutor) + " tuples: " + (time1-time0) + " ms.");

				break;							// ��ֹ�߳�
			}
			
			try {
				// ��tupleBuffer��ȡһ��newTuple(�̰߳�ȫ,��ȡ����Ԫ�أ�������)
				SkyTuple newTuple = tupleBuffer.take();
				queryGranularity ++;
				
				SkyTuple expiredTuple = null;
				if(localWindow_list.size() >= Constants.LocalWindowSize){
					expiredTuple = localWindow_list.poll();
				}
				
				// ��newTuple����ֲ���������localSlidingWindow
				localWindow_list.offer(newTuple);
				
				// �������Ԫ��expiredTuple
				if(expiredTuple != null){
					handleExpiredTuple(expiredTuple);
				}
				
				// �����µ����Ԫ��newTuple
				if(newTuple != null){
					handleNewTuple(newTuple, expiredTuple);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * �������Ԫ��expiredTuple
	 * ���expiredTuple �� LSP�����LSP��ɾ��expiredTuple��
	 * ���expiredTuple������LSP����do nothing
	 * 
	 * @param expiredTuple
	 */
	private void handleExpiredTuple(SkyTuple expiredTuple){
		
		/*
		 *  localSP_list������SkyTuple��Ȼ˳�������֯���Ƚ��µ�Ԫ����head(��id)���ȽϾɵ�Ԫ����tail(Сid)��
		 *  ��localSP_list��descendingIterator��ԭ���ǣ��ȴ���ȽϾɵ�Ԫ�飨��Ԫ��Ƚ������ǹ���Ԫ�飩
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
	 * �����µ����Ԫ��newTuple
	 * �µ���Ԫ����ܡ�LSP��Ҳ���ܡ�CSP����ʹ��LSP��Ҳ���뵽Merger�м����Ƚϲ���ȷ�����Ƿ��GSP
	 * 
	 * @param newTuple
	 */
	private void handleNewTuple(SkyTuple newTuple, SkyTuple expiredTuple){
		
		String mark = "";										// ��־��newTuple������lsp����csp
		long latestDominateId = -1;								// ֧���newTuple�ľ�Ԫ�������µ�Ԫ��id�����newTuple��lsp�������ֵΪ-1��
		LinkedList<Long> dominateSet = new LinkedList<Long>();	// localSP_list�б�newTuple֧���Ԫ�鼯�ϣ�ֻ����id�����newTuple��
		
		if(!localSP_list.isEmpty()){
			
			boolean isLSP = true;
			Iterator<SkyTuple> iter = localSP_list.listIterator(0);
			while(iter.hasNext()){
				SkyTuple tempTuple = iter.next();
				int isDominate = IsDominate.dominateBetweenTuples(newTuple, tempTuple);
				
				// ���newTuple֧��tempTuple
				if(isDominate == 0){									
					dominateSet.offer(tempTuple.getTupleID());	// ��tempTuple��id����newTuple��dominateSet
					iter.remove();								// ��tempTuple��lsp_list��ɾ��
					continue;									// newTuple������lsp_list�е���һ��Ԫ��Ƚ�
				}
				// ���newTuple��tempTuple֧��
				else if(isDominate == 1){
					isLSP = false;								// newTuple�����ܳ�Ϊlsp
					latestDominateId = tempTuple.getTupleID();	// ��־newTuple��latestDominateIdΪ֧������tempTuple��id
					break;										// ����whileѭ��
				}
				// ���newTuple��tempTuple����֧��
				else if(isDominate == 2){
					continue;									// newTuple������lsp_list�е���һ��Ԫ��Ƚ�
				}
			}
			
			if(isLSP == true){
				mark = "lsp";									// newTuple��ʶΪlsp
				this.localSP_list.offerFirst(newTuple);		// newTuple����lsp_list�ı�ͷ
			}else{
				mark = "csp";									// newTuple��ʶΪlsp
			}
			
			ComboTuple cTuple = new ComboTuple(mark, newTuple, expiredTuple, dominateSet, latestDominateId);
			try {
				
				/*
				 * ��CTuple����submitQueue��Ӧ����һ��CTuple��Ȼ��ķ�ʽ����(idС���ڶ���ͷ��id����ڶ���β)
				 * TO DO ����Ÿ�����?
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
