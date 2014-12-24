package skyline.model;

/**
 * һ��SkyTuple��ı��֣������֧���Ԫ��ı����Լ��ϵ�����Ԫ���ID��Ϣ��
 * ��������߽�Ԫ���CSP��������GSP��Ч��
 * @author Administrator
 *
 */
public class MutaTuple implements Comparable<Object>{

	private SkyTuple skyTuple;	// SkylineԪ��(key: Ԫ�鱾��)
	private long dominateID;	// ֧���Ԫ��ģ��ȸ�Ԫ��ɵģ����Ԫ���ID(value: ֧���Ԫ��ľ�Ԫ�������µ�Ԫ���tupleID)
	
	/**
	 * MutaTuple��Ĵ������Ĺ��캯��
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
	 * ��дtoString()��������tuple����Ҫ����֯��String���͵��ַ�����
	 * ����tupleID, ��ά����ֵ���Լ�latestDominateId
	 * @return
	 */
	public String toStringTuple(){
		String str = "";
		str += this.getSkyTuple().toStringTuple();
		str += " [" + this.getDominateID() + "]";
		
		return str;
	}
	
	/**
	 * MutaTuple��������֮��ıȽϷ������Ƚϵı�׼Ϊ������ID�Ĵ�С
	 * Ҫ��MutaTuple��ʵ��Comparable<Object>�ӿ�
	 * ʵ�ָýӿں�MutaTuple���͵�LinkedList��sort�Ļ���id��С��Ԫ����������ͷ
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
