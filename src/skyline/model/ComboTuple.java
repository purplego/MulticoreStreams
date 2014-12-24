package skyline.model;

import java.util.LinkedList;

/**
 * һ����ϵ�Ԫ���࣬��Ҫ���ڱ�֤��Ϣ����ʱ��˳���ԣ�newTuple��expiredTuple�ĳɶԳ��֣�
 * @author Administrator
 *
 */
public class ComboTuple implements Comparable<Object> {

	private SkyTuple newTuple;				// �µ����Ԫ��
	private SkyTuple expiredTuple;			// ���newTuple�ɶԳ��ֵ�expiredTuple�����local window��û�����������Ϊnull		
	
	private String mark;					// ��־���Ԫ������ͣ�lsp��ʾ���Ԫ���Ǿֲ�Skyline��csp��ʾ���Ԫ������CSP
	private LinkedList<Long> dominateSet;	// ��markΪlsp�������ΪLSP�б�newTuple֧���Ԫ�鼯�ϣ�������markΪcsp������Ϊnull
	private long latestDominateID;			// ��markΪcsp�������Ϊ֧���Ԫ��ı����Լ��ϵ�����Ԫ���id��������markΪlsp������Ϊ-1
	
	/**
	 * ComboTuple��Ĵ������Ĺ��캯��
	 * 
	 * @param newTuple
	 * @param expiredTuple
	 * @param mark
	 * @param dominateSe
	 * @param latestDominateId
	 */
	public ComboTuple(String mark, SkyTuple newTuple, SkyTuple expiredTuple,  
			LinkedList<Long> dominateSet, long latestDominateID){
		this.newTuple = newTuple;
		this.expiredTuple = expiredTuple;
		this.mark = mark;
		this.dominateSet = dominateSet;
		this.latestDominateID = latestDominateID;
	}
	
	/**
	 * ComboTuple��������֮��ıȽϷ������Ƚϵı�׼Ϊ��newTuple��ID�Ĵ�С
	 * Ҫ��ComboTuple��ʵ��Comparable<Object>�ӿ�
	 * ʵ�ָýӿں�ComboTuple���͵�LinkedList��sort�Ļ���id��С��Ԫ����������ͷ
	 */
	@Override
	public int compareTo(Object o) {
		ComboTuple t = (ComboTuple) o;
		if(this.getNewTuple().getTupleID() > t.getNewTuple().getTupleID())
			return 1;
		else
			return -1;
	}
	
	/*
	 * The Getter and setters of all the Class Members
	 */
	public void setNewTuple(SkyTuple newTuple) {
		this.newTuple = newTuple;
	}


	public SkyTuple getNewTuple() {
		return newTuple;
	}


	public void setExpiredTuple(SkyTuple expiredTuple) {
		this.expiredTuple = expiredTuple;
	}


	public SkyTuple getExpiredTuple() {
		return expiredTuple;
	}


	public void setMark(String mark) {
		this.mark = mark;
	}


	public String getMark() {
		return mark;
	}


	public void setDominatedSet(LinkedList<Long> dominatedSet) {
		this.dominateSet = dominatedSet;
	}


	public LinkedList<Long> getDominatedSet() {
		return dominateSet;
	}


	public void setLatestDominateID(long latestDominateID) {
		this.latestDominateID = latestDominateID;
	}


	public long getLatestDominateID() {
		return latestDominateID;
	}

}
