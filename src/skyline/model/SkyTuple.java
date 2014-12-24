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
	//��дtoString()��������tuple����Ҫ����֯��String���͵��ַ���������Ԫ��ID�͸�ά����ֵ
	public String toStringTuple(){
		String str = "";
		str += getTupleID();
		str += ",";
		str += Converter.arrayToString(getAttrs());
		return str;
	}
	
	//��дtoString()��������tuple����Ҫ����֯��String���͵��ַ���������Ԫ��ID��sum�͸�ά����ֵ
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
	 * ��дequal�������ж�����SkyTuple�Ƿ����
	 * �������tuple id��ͬ������Ϊ�������
	 */
	public boolean equals(Object obj)
	{
		boolean flag = false;
		if(obj instanceof SkyTuple)
		{
			SkyTuple tuple = (SkyTuple)obj;
			//���ж�����Tuple ID��������
			if(this.tupleID == tuple.getTupleID())
				flag = true;
		}
		
		return flag;
	}

	/**
	 * SkyTuple��������֮��ıȽϷ������Ƚϵı�׼Ϊ������ID�Ĵ�С
	 * Ҫ��SkyTuple��ʵ��Comparable<Object>�ӿ�
	 * ʵ�ָýӿں�SkyTuple���͵�LinkedList��sort�Ļ���id��С��Ԫ����������ͷ
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
