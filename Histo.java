import java.util.*;

class Histo {
	final int length;
	final Map<Integer, Integer> data; // Key distance, Value occurrences in the list

	// consumes the iterator and populates the counts in data.
	Histo(Iterator<Integer> it){
		data = new HashMap<Integer,Integer>();
		int cnt = 0;
		Integer newK = 0;
		Integer oldV = 0;
		while(it.hasNext()){
			newK = it.next();
			oldV = data.get(newK);
			if(oldV == null){
				data.put(newK, 1);
			} else {
				data.put(newK, oldV+1);
			}
			cnt++;
		}
		length = cnt;
	}

	/**
	   Prob of randomly picking twice the same value from a list of length n.
	   The prob of picking a key k with c occurances in the list, is c/n, 
	   the probability of picking it again is c-1/n-1.
	   
	   \frac{1}{n*(n-1)} * \sum_{c \in counts} c * c-1 
	   
	 */
	
	public float probCollision(){
		Iterator<Integer> it = data.values().iterator();
		int res = 0;
		Integer count = 0;
		while(it.hasNext()){
			count = it.next();
			res += count * (count-1);
		}
		return ((float)res) / ((float)(length * (length-1)));
	}
}

