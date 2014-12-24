package skyline.model;

import skyline.util.Converter;

/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class SkyTuple implements Comparable<Object>{
	
	private long tupleID;			//the id of the uncertain tuple
	private double[] attrs;			//the attributes array of the tuple
	private double sum;				//the sum of each dimension of attribute of the tuple
	
	//the constructor functions
	public SkyTuple(){}
	
	public SkyTuple(long id, double[] attrs, double sum){
		this.tupleID = id;
		this.attrs = attrs;
		this.sum = sum;
	}
	
	public SkyTuple(long id, double[] attrs){
		this.tupleID = id;
		this.attrs = attrs;
		
		this.sum = 0.0;
		for(int i=0; i<attrs.length; i++){
			this.sum += attrs[i];
		}
	}
	
	//the getter and setter of each member variable
	public void setTupleID(long tupleID) {
		this.tupleID = tupleID;
	}
	public long getTupleID() {
		return tupleID;
	}

	public void setSum(double sum) {
		this.sum = sum;
	}
	public double getSum() {
		return sum;
	}
	
	public void setAttrs(double[] atrrs) {
		this.attrs = atrrs;
	}
	public double[] getAttrs() {
		return attrs;
	}
	
	//the toString approaches
	//重写toString()方法，将tuple对象按要求组织成String类型的字符串，包括元组ID和各维属性值
	public String toStringTuple(){
		String str = "";
		str += getTupleID();
		str += ",";
		str += Converter.arrayToString(getAttrs());
		return str;
	}
	
	//重写toString()方法，将tuple对象按要求组织成String类型的字符串，包括元组ID、sum和各维属性值
	public String toStringwithSum(){
		String str = "";
		str += getTupleID();
		str += ",";
		str += getSum();
		str += ",";
		str += Converter.arrayToString(getAttrs());
		return str;
	}
	
	/**
	 * 重写equal方法，判定两个SkyTuple是否相等
	 * 如果两个tuple id相同，就认为他们相等
	 */
	public boolean equals(Object obj)
	{
		boolean flag = false;
		if(obj instanceof SkyTuple)
		{
			SkyTuple tuple = (SkyTuple)obj;
			//简单判定两个Tuple ID相等则相等
			if(this.tupleID == tuple.getTupleID())
				flag = true;
		}
		
		return flag;
	}

	/**
	 * SkyTuple类型数据之间的比较方法，比较的标准为：数据ID的大小
	 * 要求SkyTuple类实现Comparable<Object>接口
	 * 实现该接口后，SkyTuple类型的LinkedList做sort的话，id较小的元组会放在链表头
	 */
	@Override
	public int compareTo(Object o) {
		SkyTuple t = (SkyTuple) o;
		if(this.getTupleID() > t.getTupleID())
			return 1;
		else
			return -1;
	}
	
}
