package skyline.model;

/**
 * 一个SkyTuple类的变种，添加了支配该元组的比它自己老的最新元组的ID信息，
 * 有利于提高将元组从CSP中提升到GSP的效率
 * @author Administrator
 *
 */
public class MutaTuple implements Comparable<Object>{

	private SkyTuple skyTuple;	// Skyline元组(key: 元组本身)
	private long dominateID;	// 支配该元组的（比该元组旧的）最近元组的ID(value: 支配该元组的旧元组中最新的元组的tupleID)
	
	/**
	 * MutaTuple类的带参数的构造函数
	 * @param skyTuple
	 * @param dominateID
	 */
	public MutaTuple(SkyTuple skyTuple, long dominateID){
		this.skyTuple = skyTuple;
		this.dominateID = dominateID;
	}
	
	/*
	 * The getter and setter
	 */
	public void setSkyTuple(SkyTuple skyTuple) {
		this.skyTuple = skyTuple;
	}
	public SkyTuple getSkyTuple() {
		return skyTuple;
	}
	public void setDominateID(long dominateID) {
		this.dominateID = dominateID;
	}
	public long getDominateID() {
		return dominateID;
	}

	/**
	 * 重写toString()方法，将tuple对象按要求组织成String类型的字符串，
	 * 包括tupleID, 各维属性值，以及latestDominateId
	 * @return
	 */
	public String toStringTuple(){
		String str = "";
		str += this.getSkyTuple().toStringTuple();
		str += " [" + this.getDominateID() + "]";
		
		return str;
	}
	
	/**
	 * MutaTuple类型数据之间的比较方法，比较的标准为：数据ID的大小
	 * 要求MutaTuple类实现Comparable<Object>接口
	 * 实现该接口后，MutaTuple类型的LinkedList做sort的话，id较小的元组会放在链表头
	 */
	@Override
	public int compareTo(Object o) {
		MutaTuple t = (MutaTuple) o;
		if(this.getSkyTuple().getTupleID() > t.getSkyTuple().getTupleID())
			return 1;
		else
			return -1;
	}
}
