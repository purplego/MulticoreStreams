package skyline.model;

import java.util.LinkedList;

/**
 * 一个组合的元组类，主要用于保证消息传递时的顺序性（newTuple和expiredTuple的成对出现）
 * @author Administrator
 *
 */
public class ComboTuple implements Comparable<Object> {

	private SkyTuple newTuple;				// 新到达的元组
	private SkyTuple expiredTuple;			// 与该newTuple成对出现的expiredTuple，如果local window还没有满，则该域为null		
	
	private String mark;					// 标志这个元组的类型：lsp表示这个元组是局部Skyline，csp表示这个元组属于CSP
	private LinkedList<Long> dominateSet;	// 若mark为lsp，则该域为LSP中被newTuple支配的元组集合；否则若mark为csp，该域为null
	private long latestDominateID;			// 若mark为csp，则该域为支配该元组的比它自己老的最新元组的id，否则若mark为lsp，该域为-1
	
	/**
	 * ComboTuple类的带参数的构造函数
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
	 * ComboTuple类型数据之间的比较方法，比较的标准为：newTuple的ID的大小
	 * 要求ComboTuple类实现Comparable<Object>接口
	 * 实现该接口后，ComboTuple类型的LinkedList做sort的话，id较小的元组会放在链表头
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
