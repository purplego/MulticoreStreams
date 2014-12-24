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
	
	private BlockingQueue<ComboTuple> submitQueue;					// ȡ���ݵĵط�����PsExecutor��������
	private ArrayList<LinkedList<SkyTuple>> subGSPs;				// ά����ȫ��Skyline���Ӽ�
	private ArrayList<LinkedList<MutaTuple>> subCSPs;				// ά����ȫ��CSP���Ӽ�
	
	/**
	 * Merger��Ĵ������Ĺ��캯��
	 * @param submitQueue	ȡ���ݵĵط�
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
	 * PsMerger��Ĵ������Ĺ��캯��2,ģ�⻬�������Ѿ���װ�������
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
			if(queryGranularity == Constants.QueryGran){
				time1 = System.currentTimeMillis();
				Logger.getLogger("PsSkyline").info("Merger Executor: " + 
						" The total time to process " + Constants.QueryGran + " tuples: " + (time1-time0) + " ms.");
			
				break;
			}
			
			try {
				// ��submitQueue�ж�ȡһ��comboTuple(�̰߳�ȫ,��ȡ����Ԫ�أ�������)
				ComboTuple comboTuple = submitQueue.take();
				queryGranularity ++;
				
				// �������Ԫ��
				if(comboTuple.getExpiredTuple() != null){
					handleExpiredTuple(comboTuple);
				}
				
				// �����µ���Ԫ��: markΪlsp
				if(comboTuple.getMark().equals("lsp")){
					handleLspTuple(comboTuple);
				}
				
				// �����µ���Ԫ�飺markΪcsp
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
	 * �������Ԫ���Ӱ��: �ȴ�id��ͬ��subGSP�в��ҹ���Ԫ��,
	 * ����иù���Ԫ�飬��ô�Ӹ�subGSP��ɾ������Ԫ�飬������subCSP���Ƿ���Ԫ���latestDominateIdΪexpiredTuple.id
	 * (��subCSP����latestDominateId % numSubCSP����֯,���Լ��ٶԶ��subCSP�ı�������)
	 * 
	 * ���û�иù���Ԫ�飬��ôsubCSP�п϶�Ҳû�б���expiredTuple֧���Ԫ�飬do nothing
	 * @param comboTuple
	 */
	public void handleExpiredTuple(ComboTuple comboTuple){
		
		SkyTuple expiredTuple = comboTuple.getExpiredTuple();
		int indexInGSP = (int) (expiredTuple.getTupleID() % numSubGSP);
		
		boolean isInGSP = expiredFromMyGSP(subGSPs.get(indexInGSP), expiredTuple);
		
		// ֻ�й���Ԫ������id��ͬ��subGSPʱ������Ҫ����subCSP�б�����Ԫ��֧���Ԫ��
		if(isInGSP == true){
		
			int indexUpdate = -1;												// ��־��������CSP tupleӦ�����ĸ�subGSP[i]
			LinkedList<Integer> indexUpdate_list = new LinkedList<Integer>();	// ��־updateTuple����������ЩsubGSP
			
			int indexInCSP = (int) (expiredTuple.getTupleID() % numSubCSP);
			Iterator<MutaTuple> it = subCSPs.get(indexInCSP).listIterator(0);
			while(it.hasNext()){
				MutaTuple mutaTuple = it.next();
				if(mutaTuple.getDominateID() == expiredTuple.getTupleID()){
					SkyTuple updateTuple = mutaTuple.getSkyTuple();
					indexUpdate = (int) (updateTuple.getTupleID() % numSubGSP);
					
					subGSPs.get(indexUpdate).offer(updateTuple);
					it.remove();
					
					// �����ظ�����
					if(indexUpdate_list.contains(indexUpdate)){
						indexUpdate_list.offer(indexUpdate);
					}
				}
			}
			for(int sortIndex: indexUpdate_list){
				Collections.reverse(subGSPs.get(sortIndex));					// ��GlobalSkylines��һ����������SkyTuple��Ȼ���˳��
			}
		}
	}
	
	/**
	 * �жϹ���Ԫ���Ƿ���������Լ����Ǹ�subGSP��,������,ɾ������Ԫ�鲢����true;����,����false
	 * @param mySubGSP_list			�����Ϲ���Ԫ�����ڵ��Ǹ�subGSP
	 * @param expiredTuple		����Ԫ��expiredTuple
	 * @return
	 */
	public boolean expiredFromMyGSP(LinkedList<SkyTuple> mySubGSP_list, SkyTuple expiredTuple){
		
		boolean isInGSP = false;
		
		// ��ָ����subGSP��ɾ������Ԫ�飨���У�
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
	 * ���comoTuple�е�newTuple����LSP�������ȴ�id��ͬ��subGSP��ɾ��dominateSet������Ԫ�飻
	 * ��newTuple����������subGSP�е�Ԫ��Ƚϣ������newTuple������subGSP�е�Ԫ��֧�䣬�������subCSP��������latestDominateId
	 * ����subCSP�Ĺ����У�ͬʱ����subCSP��Ԫ���Ƿ񱻸�newTuple֧��
	 * 
	 * �����newTuple֧������subGSP�е�Ԫ�飬��ɾ����Щ��֧���Ԫ�飬�����newTuple��subCSP��Ԫ���֧���ϵ
	 * 
	 * @param comboTuple
	 */
	public void handleLspTuple(ComboTuple comboTuple){
		
		boolean isGSP = true;														// ��־newTuple�Ƿ������ճ�ΪGSP�е�Ԫ�飬��ʼ��Ĭ��Ϊtrue
		SkyTuple newTuple = comboTuple.getNewTuple();
		int indexInGSP = (int) (newTuple.getTupleID() % numSubGSP);
	
		for(int i = 0; i < numSubGSP; i++){
			
			// ��id��ͬ��subGSP�е�Ԫ��Ƚ�
			if(i != indexInGSP){
				isGSP = dominateTestWithOtherGSP(i, comboTuple);
				
			}
			// ��id��ͬ��subGSP��ɾ��dominateSet������Ԫ��
			else if(i == indexInGSP){
				removeDominateSetFromMyGSP(i, comboTuple);
			}
		}
		
		// newTuple����ָ����subGSP��ָ����CSP
		if(isGSP == true){
			subGSPs.get(indexInGSP).offerFirst(newTuple);
		}
		else{
			insertIntoCSP(comboTuple);
		}
	}
	
	/**
	 * �ж�newTuple������id��ͬ��subGSP��֧���ϵ
	 * 
	 * @param index			���ж���subGSP������
	 * @param comboTuple	���ж���comboTuple
	 * @return				comboTuple.getNewTuple()�Ը�subGSP[index]�����Ƿ���GSP
	 */
	public boolean dominateTestWithOtherGSP(int index, ComboTuple comboTuple){
		boolean isGSP = true;													// ��־newTuple�Ƿ������ճ�ΪGSP�е�Ԫ�飬��ʼ��Ĭ��Ϊtrue
		
		Iterator<SkyTuple> iter = subGSPs.get(index).listIterator(0);
		while(iter.hasNext()){													// ����subGSP[i]
			SkyTuple tmpTuple = iter.next();
			int isDominate = IsDominate.dominateBetweenTuples(comboTuple.getNewTuple(), tmpTuple);
			
			// ���newTuple֧��tmpTuple
			if(isDominate == 0){
				iter.remove();													// ��tmpTuple��subGSP[i]��ɾ��
				continue;														// newTuple������subGSP[i]�е���һ��tmpTupleԪ��Ƚ�
			}
			// ���newTuple��tmpTuple֧��
			else if(isDominate == 1){
				isGSP = false;													// newTuple�����ܳ�ΪGSP
				if(comboTuple.getLatestDominateID() < tmpTuple.getTupleID()){
					comboTuple.setLatestDominateID(tmpTuple.getTupleID());		// ����newTuple��latestDominateId
				}
				break;															// ����whileѭ��
			}
			// ���newTuple��tmpTuple����֧��
			else if(isDominate == 2){
				continue;														// newTuple������subGSP[i]�е���һ��Ԫ��tmpTuple�Ƚ�
			}
		}
		return isGSP;
	}
	
	/**
	 * ��id��ͬ��subGSP��ɾ��newTuple֧���Ԫ�飬��comboTuple.dominateSet�е�Ԫ��
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
					break;							// ����һ��tID�Ƚ�
				}
			}
		}
	}
	/**
	 * ��һ��newTuple����ָ����CSP��ͬʱɾ������CSP�б�newTuple֧���Ԫ��
	 * �����Ԫ��֧��newTuple��������latestDominateId
	 * 
	 * @param comboTuple
	 */
	public void insertIntoCSP(ComboTuple comboTuple){
		
		updateCSP(comboTuple);
		int indexInCSP = (int) (comboTuple.getLatestDominateID() % numSubCSP);
		
		subCSPs.get(indexInCSP).offerFirst(new MutaTuple(comboTuple.getNewTuple(), comboTuple.getLatestDominateID()));
	}
	
	/**
	 * �жϸ�subCSP�е�Ԫ����newTuple��֧���ϵ��ɾ��subCSP�б�֧���Ԫ�飬���߸���newTuple��latestDominateId
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
				
				// ���newTuple֧��tmpTuple
				if(isDominate == 0){
					iter.remove();
					continue;
				}
				// ���newTuple��tmpTuple֧��
				else if(isDominate == 1){
					if(comboTuple.getLatestDominateID() < tmpTuple.getTupleID()){
						comboTuple.setLatestDominateID(tmpTuple.getTupleID());		// ����newTuple��latestDominateId
					}
					break;															// ����whileѭ��
				}
				// ���newTuple��tmpTuple����֧��
				else if(isDominate == 2){
					continue;
				}
			}
		}
	}
	
	/**
	 * ���comoTuple�е�newTuple������LSP��������CSP
	 * ɾ����CSP�б���newTuple֧��ľ�Ԫ��
	 * ������subGSP�е���Ԫ��Ƚϣ����¸�newTuple��latestDominateId�򣬽������id��ͬ��subCSP��
	 * 
	 * @param comboTuple
	 */
	public void handleCspTuple(ComboTuple comboTuple){
		
		// ������subGSP�е�Ԫ��Ƚϣ����¸�newTuple��latestDominateId��
		udpateDominateIdWithOhterGSP(comboTuple);
		
		// ɾ����subCSP�б���newTuple֧��ľ�Ԫ�飬����newTuple��latestDominateId
		updateCSP(comboTuple);
		
		// ��newTuple����latestDominateIdһ����װ��MutaTuple��������ָ����subCSP��
		int indexInCSP = (int) (comboTuple.getLatestDominateID() % numSubCSP);
		subCSPs.get(indexInCSP).offer(new MutaTuple(comboTuple.getNewTuple(), comboTuple.getLatestDominateID()));
	}
	
	/**
	 * comboTuple.newTuple������id��ͬ��subGSP�Ƚϣ�����newTuple��latestDominateId
	 * 
	 * @param comboTuple
	 */
	public void udpateDominateIdWithOhterGSP(ComboTuple comboTuple){
		
		SkyTuple newTuple = comboTuple.getNewTuple();
		int indexInGSP = (int) (newTuple.getTupleID() % numSubGSP);		// ��ȡnewTuple���ڵ�subGSP��index
		for(int i=0; i<numSubGSP; i++){
			if(i != indexInGSP){										// ��id��ͬ��subGSP���Ƚ�
				
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
