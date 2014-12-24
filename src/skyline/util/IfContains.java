package skyline.util;

import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.TreeMap;

import skyline.model.SkyTuple;

public class IfContains {
	
	/**
	 * ���tuple_list�а���Ԫ��tuple���򷵻�true�����򷵻�false
	 * @param tuple_list
	 * @param tuple
	 * @return
	 */
	public static boolean containsTuple(LinkedList<SkyTuple> tuple_list, SkyTuple tuple){
		for(SkyTuple tuple_temp: tuple_list){
			if(tuple_temp.getTupleID() == tuple.getTupleID()){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * ���tupleMap�к���tuple���򷵻�true�����򣬷���false
	 * @param tuple_list
	 * @param tuple
	 * @return
	 */
	public static boolean containsTuple(TreeMap<SkyTuple, Long> tupleMap, SkyTuple tuple){
		NavigableSet<SkyTuple> keySet = tupleMap.navigableKeySet();
		for(SkyTuple t: keySet){
			if(t.getTupleID() == tuple.getTupleID()){
				return true;
			}
		}
		return false;
	}
}
