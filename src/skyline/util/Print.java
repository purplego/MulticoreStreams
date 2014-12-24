package skyline.util;

import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.TreeMap;

import skyline.model.SkyTuple;
import skyline.model.MutaTuple;

/**
 * @author Purple Wang
 * Jan 21, 2014
 */
public class Print {
	
	public static void printList(LinkedList<SkyTuple> tuple_list){
		System.out.println("*******************************************");
		for(SkyTuple t: tuple_list){
			System.out.println(t.toStringTuple());
		}
	}
	
	public static void printList2(LinkedList<MutaTuple> tuple_list){
		System.out.println("*******************************************");
		for(MutaTuple t: tuple_list){
			System.out.println(t.getSkyTuple().toStringTuple() + " [" + t.getDominateID() + "]");
		}
	}
	
	public static void printMap(TreeMap<SkyTuple, Long> map){
		/*NavigableSet<SkyTuple> inverseSet = map.descendingKeySet();
		for(SkyTuple tuple: inverseSet){
			System.out.println(tuple.toStringTuple());
		}*/
		
		System.out.println("*******************************************");
		
		for(Entry<SkyTuple, Long> entries: map.entrySet()){
			System.out.println(entries.getKey().toStringTuple() + " [" + entries.getValue() + "]");
		}
	}
	
}
