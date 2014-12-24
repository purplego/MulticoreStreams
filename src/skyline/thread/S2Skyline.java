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
 * ���е�������Skyline��ѯ�㷨����Lazy�㷨�ı���
 * ��Ҫ��������CSP��ά�����Լ�expiredTuple��������δ�CSP���ҳ��ϸ�ĵ㲢����GSP�Ĺ���
 * @author Administrator
 *
 */
public class S2Skyline implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer;					// ȡ���ݵĵط�����GenDataThread��������
	private LinkedList<SkyTuple> globalWindow_list;					// ά����ȫ�ֻ�������
	private LinkedList<SkyTuple> globalSP_list;						// ά����ȫ��Skyline���ϣ���ǰ�������ڵ�Skyline���ϣ�
	private LinkedList<MutaTuple> candidateSP_list;					// ά���ĺ�ѡSkyline���ϣ���ǰ���������б�֧�䵫�Ƚ��µĽڵ㣩
	
	/**
	 * S2Skyline��Ĵ������Ĺ��캯��
	 * @param tupleBuffer			ȡ���ݵĵط�
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
	 * �������Ĺ��캯��2��ģ�⻬�������Ѿ���װ����������ҵ�ǰglobalSkylines��candidateSkylines���Ѿ���ֵ
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
	 * ����ʵ��Runnable�ӿ���ʵ���̹߳��ܣ���Ҫʵ��һ��run()����
	 */
	@Override
	public void run() {

		long queryGranularity = 0L;								// ���ڽ��ͳ�Ƶļ�������¼1000��tuple�Ĵ���ʱ��
		long time0 = 0L, time1 = 0L, time2 = 0L, time3 = 0L;
		
		while(true){
			
			if(queryGranularity == 0){
				time0 = System.currentTimeMillis();
			}
			if(queryGranularity == Constants.QueryGran){
				time1 = System.currentTimeMillis();
				// ��¼��־
				Logger.getLogger("S2Skyline").info("The total time to process " + Constants.QueryGran + " tuples: " + (time1-time0) + 
						" ms.\n" + " time0:" + time0 + " time1:" + time1 + " time2:" + time2 + " time3:" + time3);
				
				break;											// ����whileѭ�����߳���ֹ
			}
			
			try{
				// ��tupleBuffer��ȡһ��newTuple(�̰߳�ȫ,��ȡ����Ԫ�أ�������)
				SkyTuple newTuple = tupleBuffer.take();
				queryGranularity ++;
				
				/*
				 * �жϻ������ڵ������δ����������;����������������������һ������Ԫ��
				 * ������Ԫ���ȫ�ֻ�������globalSlidingWindow��ɾ��(����ͷ��Ԫ��)
				 */
				SkyTuple expiredTuple = null;
				if(globalWindow_list.size() >= Constants.GlobalWindowSize){
					
					expiredTuple = globalWindow_list.poll();
				}
				
				// ��newTuple����ȫ�ֻ�������globalSlidingWindow
				globalWindow_list.offer(newTuple);
				
				// �������Ԫ��expiredTuple
				if(expiredTuple != null){
					handleExpiredTuple(expiredTuple);
				}
				time2 = System.currentTimeMillis();
				
				// �����µ����Ԫ��newTuple
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
	 * �������Ԫ��expiredTuple
	 * ���expiredTuple �� GSP��������ɾ��GSP�еĹ���Ԫ�飬Ȼ���ҳ�CSP��dominateIDΪexpiredTuple.ID��Ԫ�鲢���뵽GSP�У�
	 * ���expiredTuple������GSP����do nothing
	 * 
	 * @param expiredTuple
	 */
	private void handleExpiredTuple(SkyTuple expiredTuple){
		
		boolean isInGSP = expireFromGSP(globalSP_list, expiredTuple);

		if(isInGSP == true){
			boolean isSort = false;												// �ж��Ƿ���Ҫ��subGSP������(ֻ�в���Ԫ���Ż���������)
			Iterator<MutaTuple> it = candidateSP_list.listIterator(0);
			while(it.hasNext()){
				MutaTuple temp = it.next();
				if(temp.getDominateID() == expiredTuple.getTupleID()){
					globalSP_list.offer(temp.getSkyTuple());					// ������������t����GlobalSkylines������β
					it.remove();												// ��CSP��ɾ������ΪGSP��tuple
					isSort = true;
				}
			}
			if(isSort == true){
				Collections.reverse(globalSP_list);								// ��GlobalSkylines��һ����������SkyTuple��Ȼ���˳��
			}
		}
	}
	
	/**
	 * �ж�һ��Ӧ�ù��ڵ�SkyTupleԪ��expiredTuple�Ƿ�����ָ��������gsp_list,
	 * �������,��gsp_list��ɾ����expiredTuple,������true;����,����false
	 * 
	 * @param globalSP_list		ָ����SkyTuple����
	 * @param expiredTuple		���ж��Ĺ���Ԫ��
	 * @return					��tuple����tuple_list,����true;����,����false
	 */
	private boolean expireFromGSP(LinkedList<SkyTuple> globalSP_list, SkyTuple expiredTuple)
	{
		boolean isInGSP = false;												// ��־expiredTuple�Ƿ�����globalSP_list
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
	 * �����µ����Ԫ��newTuple
	 * @param newTuple
	 */
	private void handleNewTuple(SkyTuple newTuple){
		
		if(globalSP_list != null){
			boolean isGSP = true;													// �ж�newTuple�Ƿ�����GSP�ı�־����ʼΪtrue
			long latestDominatedID = -1;											// ��ǰ����������֧��newTuple�ı����ɵ�����Ԫ���ID����ʼ��Ϊ-1
			
			Iterator<SkyTuple> iter = globalSP_list.listIterator(0);				// GSP������ͷΪid�ϴ��Ԫ��
			while(iter.hasNext()){
				SkyTuple tuple = iter.next();
				
				int isDominate = IsDominate.dominateBetweenTuples(newTuple, tuple);	// �ж�newTuple��GSP��Ԫ���֧���ϵ
				
				// ����0��ʾnewTuple֧��tuple, ɾ��GSP�б�newTuple֧���Ԫ��, ������GSP�е���һԪ��Ƚ�
				if(isDominate == 0){
					iter.remove();
					continue;
				}
				// ����1��ʾnewTuple��tuple֧��, ����newTuple��latestDominateIdֵ, break from while
				else if(isDominate == 1){
					isGSP = false;
					latestDominatedID = tuple.getTupleID();
					break;
				}
				// ����2��ʾ����֧��, ������GSP�е���һԪ��Ƚ�
				else if(isDominate == 2){
					continue;
				}
			}
			
			// isGSPΪTRUE��ʾnewTuple����GSP����϶����ᱻCSP�е�����Ԫ��֧��
			if(isGSP == true){													
				globalSP_list.offerFirst(newTuple);									// ��newTuple����GSP
				removeFromCandidateSkylines(newTuple);								// ֱ��ɾ��CSP�б�newTuple֧���Ԫ��
				
			}else{
				insertIntoCandidateSkylines(newTuple, latestDominatedID);
			}
			
		}else{
			globalSP_list.offerFirst(newTuple);
		}
	}
	
	/**
	 * ��newTuple����candidateSkylines(Ϊnull��ֱ�Ӳ��룬������ȴ����ٲ���)
	 * ����candidateSkylines��ɾ�����б�newTuple֧���Ԫ�飻
	 * �����Ԫ��֧��newTuple��������Ҫ��ʱ�򣩸���newTuple��latestDominateTupleID
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
				
				// ����0��ʾnewTuple֧��tempTuple,��CSP��ɾ����newTuple֧���Ԫ��, ������CSP�е���һԪ��Ƚ�
				if(isDominate == 0){
					iter.remove();
					continue;
				}
				// ����1��ʾnewTuple��tempTuple֧��, ����newTuple��latestDominateTupleID, ����whileѭ��
				else if(isDominate == 1){
					
					if(tempTuple.getSkyTuple().getTupleID() > latestDominateID){
						latestDominateID = tempTuple.getSkyTuple().getTupleID();
						mutaTuple.setDominateID(latestDominateID);
						break;
					}
				}
				// ����2��ʾnewTuple��tempTuple����֧��, ������CSP�е���һԪ��Ƚ�
				else if(isDominate == 2){
					continue;
				}
			}
		}
		candidateSP_list.offerFirst(mutaTuple);											// ��newTuple����CSP
	}

	/**
	 * ��CandidateSkylines��ɾ����newTuple֧���Ԫ��,��ȷ��newTuple��GSP
	 * @param newTuple
	 */
	private void removeFromCandidateSkylines(SkyTuple newTuple){
		
		Iterator<MutaTuple> iter = candidateSP_list.listIterator(0);
		while(iter.hasNext()){
			MutaTuple tempTuple = iter.next();
			int isDominate = IsDominate.dominateBetweenTuples(newTuple, tempTuple.getSkyTuple());
			
			// ����0��ʾnewTuple֧��tempTuple
			if(isDominate == 0){
				iter.remove();													// ��CSP��ɾ����newTuple֧���Ԫ��
			}
		}
	}
}
