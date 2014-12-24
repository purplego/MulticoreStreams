package skyline.util;

import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.TreeMap;

import skyline.model.SkyTuple;

public class IfContains {
	
	/**
	 * 如果tuple_list中包含元组tuple，则返回true；否则返回false
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
	 * 如果tupleMap中含有tuple，则返回true；否则，返回false
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
