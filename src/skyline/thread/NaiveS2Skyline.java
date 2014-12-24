package skyline.thread;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import skyline.util.Constants;

import skyline.algorithms.BNL;
import skyline.algorithms.SFS;
import skyline.model.SkyTuple;

public class NaiveS2Skyline implements Runnable {

	private BlockingQueue<SkyTuple> tupleBuffer;		// ȡ���ݵĵط�����GenDataThread��������
	private LinkedList<SkyTuple> globalWindow_list;		// ά����ȫ�ֻ�������
	private LinkedList<SkyTuple> globalSP_list;			// ά����ȫ��Skyline���ϣ���ǰ�������ڵ�Skyline���ϣ�
	
	/**
	 * �������Ĺ��캯��
	 * @param tupleBuffer
	 */
	public NaiveS2Skyline(BlockingQueue<SkyTuple> tupleBuffer){
		this.tupleBuffer = tupleBuffer;
		this.globalWindow_list = new LinkedList<SkyTuple>();
		this.globalSP_list = new LinkedList<SkyTuple>();
	}
	
	/**
	 * �������Ĺ��캯��2��ģ�⻬�������Ѿ���װ�������
	 * @param tupleBuffer
	 * @param globalWindow_list
	 */
	public NaiveS2Skyline(BlockingQueue<SkyTuple> tupleBuffer, LinkedList<SkyTuple> globalWindow_list){
		
		this.tupleBuffer = tupleBuffer;
		this.globalWindow_list = globalWindow_list;
		this.globalSP_list = new LinkedList<SkyTuple>();
	}
	
	/**
	 * ����ʵ��Runnable�ӿ���ʵ���̹߳��ܣ���Ҫʵ��һ��run()����
	 */
	@Override
	public void run() {

		while(true){
			try{
				
				// ��tupleBuffer��ȡһ��newTuple(�̰߳�ȫ,��ȡ����Ԫ�أ�������)
				SkyTuple newTuple = tupleBuffer.take();
				System.err.println("The tupleBuffer size:  " + tupleBuffer.size());
				SkyTuple expiredTuple = null;
				
				if(globalWindow_list.size() >= Constants.GlobalWindowSize){
					/*
					 * �жϻ������ڵ������δ����������;����������������������һ������Ԫ��
					 * ������Ԫ���ȫ�ֻ�������globalSlidingWindow��ɾ��(����ͷ��Ԫ��)
					 */
					expiredTuple = globalWindow_list.poll();
//					System.out.println("Expired Tuple: " + expiredTuple.toStringTuple());
				}
				
				// ��newTuple����ȫ�ֻ�������globalSlidingWindow
				globalWindow_list.offer(newTuple);
				
				// ��globalSlidingWindowΪ���ݼ��ϼ���ȫ��Skyline
				Collections.sort(globalWindow_list);
				globalSP_list = SFS.sfsQuery(globalWindow_list);
				System.out.println("The globalSkylines size: " + globalSP_list.size());
				
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
