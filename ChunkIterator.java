import java.util.*;


/**
   Takes an iterator, loads n elements and returns them in bulk.
*/

class ChunkIterator<E> {
	final int chunkSize;
	final Iterator<E> it;
	final boolean strict; 		// return last partial chunk or not
	int chunkCnt = 1;
	List<E> chunk;

	public ChunkIterator(int size, Iterator<E> it, boolean strict) {
		this.it = it;
		this.chunkSize = size;
		this.strict = strict;
	}

	public ChunkIterator(int size, Iterator<E> it) {
		this(size, it, true);
	}

	public boolean hasNext() {
		if (chunk != null) {
			return true;
		} else {
			chunk = new ArrayList<E>();
			int lineCnt = 0;
			E line;
			while (it.hasNext()){
				line = it.next();
				lineCnt += 1;
				chunk.add(line);
				// Util.log(line.substring(0,20));
				if (lineCnt == chunkSize){ // chunk ready
					Util.log("Create: "+chunkCnt+" with lines "+lineCnt);
					chunkCnt += 1;
					return true;
				}
			}
			if (!strict && (lineCnt > 0)) {
				List<E> partial = chunk.subList(0,lineCnt);
				chunk = partial;
				Util.log("Create: "+chunkCnt+" with lines "+lineCnt);
				return true;
			} else {
				chunk = null;
				return false;
			}
		}
	}
	public List<E> next() {
		if(hasNext()){
			List<E> res = chunk;
			chunk = null;
			return res;
		} else {
			throw new NoSuchElementException();
		}
	}
}
