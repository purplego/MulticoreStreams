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
 * SharedSkyline�࣬��S2Skyline�ĸĽ��������ж�GSP��CSP�Ĵ����л�
 * ��S2Skyline �㷨�Ķ��̰߳汾�����̴߳���GSP��CSP
 * @author ��
 *
 */
public class SharedSkyline implements Runnable {
	
	private int numSubGSP = Constants.NumSUBGSP;
	private int numSubCSP = Constants.NumSUBCSP;

	private BlockingQueue<SkyTuple> tupleBuffer;			// ȡ���ݵĵط�,��FetchDataThread���ļ��ж�ȡ����
	private LinkedList<SkyTuple> globleWindow_list;			// ά����ȫ�ֻ�������
	private ArrayList<LinkedList<SkyTuple>> subGSPs;		// ���̴߳���GSP,��GSP��Ϊ�����GSP,GSP�е�tuple����tupleID ȡģ����
	private ArrayList<LinkedList<MutaTuple>> subCSPs;		// ���̴߳���CSP,��CSP��Ϊ�����CSP,��latestDominateId��֯
	
	/**
	 * SharedSkyline��Ĵ������Ĺ��캯��
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
	 * SharedSkyline��Ĵ������Ĺ��캯��2,ģ�⻬�������Ѿ���װ�������,�ҵ�ǰGSP��CSP���Ѿ���ֵ
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

		boolean isRunning = true;							// �����Ƿ�����߳�
		long queryGranularity = 0L;							// ���ڽ��ͳ�Ƶļ�������¼1000��tuple�Ĵ���ʱ��
		long time0 = 0L, time1 = 0L, time2 = 0L, time3 = 0L;
		
		while(isRunning) {
			if(queryGranularity == 0){
				time0 = System.currentTimeMillis();
			}
			if(queryGranularity == Constants.QueryGran){
				time1 = System.currentTimeMillis();
				isRunning = false;							// �߳���ֹ
				
				// ��¼��־
				Logger.getLogger("SharedSkyline").info("The total time to process " + Constants.QueryGran + " tuples: " + (time1-time0) + " ms.\n");
			}
			
			try {
				SkyTuple newTuple = tupleBuffer.take();		// ��tupleBuffer��ȡһ��newTuple
				queryGranularity ++;
				
				SkyTuple expiredTuple = null;
				
				// �������ݹ��ڣ���ӻ��������������������expiredTuple
				if(globleWindow_list.size() >= Constants.GlobalWindowSize){
					expiredTuple = globleWindow_list.poll();
				}
				
				// �򻬶������в����µ�������newTuple
				globleWindow_list.offer(newTuple);
				
				// �������Ԫ��expiredTuple
				if(expiredTuple != null) {
					handleExpiredTuple(expiredTuple);
				}
				
				// �����µ����Ԫ��newTuple
				if(newTuple != null){
					handleNewTuple(newTuple);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * �������Ԫ��expiredTuple,��Ҫ�Ǵ�CSP���ҳ��ϸ��Ԫ������ΪGSP��
	 * ���ȼ���Ԫ���Ƿ���GSP�У����expiredTuple����GSP�У������κβ���(CSP�в������б�expiredTuple֧���Ԫ��)
	 * ���expiredTuple��GSP��,��
	 * (1)ɾ��GSP�� �����expiredTuple,
	 * (2)��CSP���ҳ���latestDominateIdΪexpiredTuple��Ԫ����뵽GSP��,
	 * (3)���޸Ĺ���GSP��Ҫ������
	 * 
	 * @param expiredTuple
	 */
	private void handleExpiredTuple(SkyTuple expiredTuple)
	{
		int indexInGSP = (int) (expiredTuple.getTupleID() % numSubGSP);		// ����Ԫ��id�ж�expiredTupleӦ��������һ��subGSP[i]
		
		boolean isInGsp = expireFromGSP(subGSPs.get(indexInGSP), expiredTuple);	// �жϸ�expiredTuple�Ƿ��ڶ�Ӧ��subGSP��,��ɾ������Ԫ��
		if(isInGsp == true) {
			
			/*
			 * �Ѷ��subCSP����dominateId % numSubCSP��֯��subCSPS���arrayList��,
			 * ֻҪ�ҵ�ĳ��dominateId % numSubCSP��subCSPs[i],�����ҵ�����dominateIdΪexpiredTuple.id��Ԫ��
			 * ����������subCSPs[i]
			 */
			int indexInCSP = (int) (expiredTuple.getTupleID() % numSubCSP);	// ��־dominateIdΪexpiredTuple.id���Ǹ�subCSP[i]
			
			int index = -1;														// ��־��������CSP tupleӦ�����ĸ�subGSP[i]
			LinkedList<Integer> index_list = new LinkedList<Integer>();
			Iterator<MutaTuple> it = subCSPs.get(indexInCSP).listIterator(0);
			while(it.hasNext()){
				MutaTuple temp = it.next();
				if(temp.getDominateID() == expiredTuple.getTupleID()){
					index = (int) (temp.getSkyTuple().getTupleID() % numSubGSP);
					subGSPs.get(index).offer(temp.getSkyTuple());				// ������������t����subGSP[i]������β
					it.remove();												// ��CSP��ɾ������ΪGSP��tuple
					index_list.offer(index);
				}
			}
			for(int sortIndex: index_list){
				Collections.reverse(subGSPs.get(sortIndex));						// ��GlobalSkylines��һ����������SkyTuple��Ȼ���˳��
			}
		}
	} 
	
	/**
	 * ���̲߳��еĴ���NewTuple,
	 * ����,���е��ж�newTuple�����subGSP[i]��֧���ϵ��
	 * (1)���NewTuple��֧��,�����newTuple��latestDominateId,��newTuple(����MutaTuple)����CSP��ӵ�CSP��,
	 * (2)���NewTuple����֧��,������ӵ���Ӧ��GSP��(����newTupleID % numSubGSP�ҵ���ȷ��index)
	 * 
	 * Ȼ��,����newTuple�Ƿ�����GSP,����Ҫɾ������subCSP[i]�б�newTuple֧���Ԫ��
	 * @param newTuple
	 */
	private void handleNewTuple(SkyTuple newTuple) {
		/*
		 * ���subGSPs[i]ͬʱ���д���,����ʹ���˶��߳�
		 */
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<Long>> dominateId_list = new ArrayList<Future<Long>>();	// ��Ÿ��̷߳��صĹ���newTuple��latestDominateId��Ϣ
		for(int i = 0 ;i < numSubGSP ; i++) {
			dominateId_list.add(exec.submit(new GspHandlerThread(subGSPs.get(i), newTuple)));
		}
		
		long latestDominateId = -1;													// newTuple��latestDominateId
		
		for(Future<Long> tmpDominateId : dominateId_list) {
			try {
				// Future���get()������ȡ�߳�ִ�еķ���ֵ: ����߳�ûִ����,������,�ȴ��������,Ȼ���ȡ����
				if(tmpDominateId.get() > latestDominateId)
					latestDominateId = tmpDominateId.get();							// ��ȡ�������̷߳��ص�����latestDominateIdֵ
			
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		/*
		 * latestDominateId == -1,��ʾnewTuple������subGSP�е��κ�Ԫ��֧��
		 * ��newTuple��ȫ��Skyline,Ӧ����ӵ���ȷ��subGSP[i]��(����ȡģ����������)
		 * ͬʱ��Ӧ��ɾ����subCSP�б�newTuple֧���Ԫ��
		 */
		if(latestDominateId == -1) {
			int indexInGSP = (int) (newTuple.getTupleID() % numSubGSP);
			subGSPs.get(indexInGSP).offerFirst(newTuple);
			
			for(int i = 0; i< numSubCSP; i++){
				exec.execute(new CspUpdateThread(subCSPs.get(i), newTuple));
			}
		}
		// newTuple��ĳ����ǰ��ȫ��skyline��֧��,��newTuple���뵽CSP��
		else {
			insertIntoCSP(newTuple, latestDominateId);
		}
	}
	
	/**
	 * ����Ԫ��newTuple���뵽CSP��,����newTuple��(latestDominateId % numSubCSP)�ҵ���ȷ��subCSP[i]
	 * ͬʱɾ����subCSP�б�newTuple֧���Ԫ��
	 * @param tuple
	 * @param latestDominateID
	 */
	private void insertIntoCSP(SkyTuple tuple, long latestDominateID) {
		
		/*
		 * ���subCSPs[i]ͬʱ���д���,����ʹ���˶��߳�
		 */
		ExecutorService exec = Executors.newCachedThreadPool();
		ArrayList<Future<Long>> dominateId_list= new ArrayList<Future<Long>>();	// ��Ÿ��̷߳��صĹ���newTuple��latestDominateId��Ϣ
		for(int i = 0 ;i < numSubCSP ; i++) {
			dominateId_list.add(exec.submit(new CspHandlerThread(subCSPs.get(i), tuple, latestDominateID)));
		}
		
		long latestDominateId_new = latestDominateID;
		for(Future<Long> tmpDominateId : dominateId_list) {
			try {
				// Future���get()������ȡ�߳�ִ�еķ���ֵ: ����߳�ûִ����,������,�ȴ��������,Ȼ���ȡ����
				if(tmpDominateId.get() > latestDominateId_new)
					latestDominateId_new = tmpDominateId.get();					// ����newTuple��latestDominateId
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		/*
		 * ����newTuple��latestDominateIdȷ����ȷ��subCSP[i],����newTuple������ȷ��subCSP[i]
		 */
		int index = (int) (latestDominateId_new % numSubCSP);					
		subCSPs.get(index).offerFirst(new MutaTuple(tuple, latestDominateId_new));
	}
	
	
	/**
	 * �ж�һ��Ӧ�ù��ڵ�SkyTupleԪ��expiredTuple�Ƿ�����ָ��������gsp_list,
	 * �������,��gsp_list��ɾ����expiredTuple,������true;����,����false
	 * 
	 * @param gsp_list			ָ����SkyTuple����
	 * @param expiredTuple		���ж��Ĺ���Ԫ��
	 * @return					��tuple����tuple_list,����true;����,����false
	 */
	private boolean expireFromGSP(LinkedList<SkyTuple> gsp_list, SkyTuple expiredTuple)
	{
		boolean isMine = false;										// ��־expiredTuple�Ƿ�����gsp_list
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
 * GspHandlerThread��,�߳�ʵ��
 * 
 * ����newTuple��ָ��subGSP(ȫ��skyline�����һ����)�и�Ԫ���֧���ϵ:
 * (1)���newTuple��֧��subGSP�е�ĳ��skylineԪ��֧��,�򷵻�֧��newTuple������tuple��ID��Ϊ��latestDominateId
 * (2)���newTuple����subGSP�е�����Ԫ��֧��,�򷵻�-1��Ϊ��latestDominateId
 * (3)���subGSP����Ԫ�鱻newTuple֧��,��ɾ��subGSP�б�newTuple֧���Ԫ��
 * 
 * ����ֵ: newTuple��latestDominateId
 * 
 * @author ��
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
				 * �ж�newTuple��GSP��Ԫ���֧���ϵ
				 * ����0��ʾnewTuple֧��tuple;����1��ʾnewTuple��tuple֧��;2��ʾ����֧��
				 */
				int flag = IsDominate.dominateBetweenTuples(newTuple,tuple);
				
				// ����0��ʾnewTuple֧��tuple
				if(flag == 0) {
					iter.remove();
					continue;
				}
				// ����1��ʾnewTuple��tuple֧��
				else if(flag == 1 ) {
					latestDominatedID  = tuple.getTupleID();
					break;					//GSP����,���Կ���ֱ�ӽ����Ƚ�
				}
				// ����2��ʾ����֧��
				else if(flag == 2) {
					continue;
				}
			}
		}

		return latestDominatedID;
	}
}

/**
 * CspHandlerThread��,�߳�ʵ��
 * 
 * ����: newTuple������GSP,��Ҫ����ĳ��subCSP
 * ����: ����newTuple��ָ��subCSP��Ԫ���֧���ϵ,
 * (1)���newTuple֧��subCSP�е�Ԫ��,��ӱ�subCSP��ɾ����֧���Ԫ��
 * (2)���newTuple��subCSP�е�Ԫ��֧��,�����newTuple��latestDominateId
 * ����ֵ: newTuple��latestDominateId
 * 
 * @author ��
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
				
				// ����0��ʾnewTuple֧��tuple
				if(flag == 0){
					iter.remove();													// ��CSP��ɾ����newTuple֧���Ԫ��
				}
				// ����1��ʾnewTuple��tuple֧��
				else if(flag == 1){
					if(tempTuple.getSkyTuple().getTupleID() > latestDominateID){
						latestDominateID = tempTuple.getSkyTuple().getTupleID();	// ����newTuple��latestDominateTupleID
						break;
					}
				}
			}
		}
		return latestDominateID;
	}
}

/**
 * CspUpdateThread��: �߳�ʵ��
 * ����: һ��newTuple�Ѿ�֪��������GSP��,�����Ӹ�subCSP��ɾ����newTuple֧���Ԫ��
 * 
 * @author ��
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
				iter.remove();								// ��CSP��ɾ����newTuple֧���Ԫ��(��ȷ��newTuple����GSPʱ�Ż���ø÷���)
			}
		}
	}
}
