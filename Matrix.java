import java.util.*;
import java.io.*;

/**
  This class implements a weighted undirected clique graph. 

  It is implemented as a 'reduced' adjacency matrix:
  1) no element i,i is stored as they are all 0
  2) being symmetric only half of the matrix is stored

  The size in memory can be estimated as (n^2-n)/2 * 32 bit

  Additionally now all weight are >=0 because we implement a metric.
*/

class Matrix {
	private final int size;
	private final int[] data;

	// size is the number of vertices
	Matrix(int size){
		this.size = size;
		this.data = new int[size * (size-1) / 2];
	}

	// Parse a matrix from a file generated with the function save
	Matrix(String fileName) throws Exception {
		final BufferedReader in = new BufferedReader(new FileReader(fileName));
		size = Integer.parseInt(in.readLine().trim());
		data = ofString(in.readLine());
		if(data.length != (size * (size-1) / 2))
			throw new IllegalArgumentException("Mismatched length in file "+fileName);
		in.close();
	}

	// sum of first n integers
	private int sumFirstInt(int n){
		return n*(n+1)/2;
	}
	private void validateCoo(int r, int c){
		if((r < 0) || (r >= size) ||
		   (c < 0) || (c >= size))
			throw new IllegalArgumentException("("+r+","+c+")");
	}
	
	// converts an address of the adjacency matrix to an index in the array
	// takes care of symmetry d(x,y) = d(y,x)
	// assumes coo are valid and not equals
	private int convertCoo(int r, int c){
		if(r > c) {int tmp=r; r=c; c=tmp;}
		return (r * size) - sumFirstInt(r) + c - r -1;
	}

	int size(){
		return size;
	}
	
	// returns distance between user r and c
	// takes care of identity of indiscernible d(x,x) = 0
	int get(int r, int c){
		validateCoo(r,c);
		if(r == c) return 0;
		return data[convertCoo(r,c)];
	}

	// Given a user, iterates over the distances to all other users
	private	class It implements Iterator<Integer> {
		private int cnt;
		private final int user;

		It(int user){
			this.user = user;
			this.cnt = 0;
		}
		// we return size-1 element, need extra care if the user is the last one
		public boolean hasNext(){
			return ((user == size-1) && (cnt<size-1)) || ((user < size-1) && (cnt<size));
		}
		public Integer next(){
			if(hasNext()){
				if(cnt == user) cnt++;
				int res = get(user,cnt);
				cnt++;
				return res;
			} else {throw new NoSuchElementException();}
		}
	}

	Iterator<Integer> getAll(int i){
		return new It(i);
	}
	
	// set distance between user r and c
	// enforces that all values must be >= 0
	// enforces that i,i must be zero
	void set(int r, int c, int val){
		validateCoo(r,c);
		if(val < 0) throw new IllegalArgumentException("Invalid distance "+val);
		if(r == c){
			if(val != 0) throw new IllegalArgumentException("Must be zero");
			else return;
		}
		data[convertCoo(r,c)] = val;
	}

	// sum of two matrices
	void sum(Matrix m){
		if(this.size != m.size()) throw new IllegalArgumentException("Invalid size");
		for(int i=0; i<data.length; i++){
			this.data[i] += m.data[i];
		}
	}

	private int[] ofString(String s){
		String[] tmp = s.split(",");
		int[] res = new int[tmp.length];
		for(int i=0; i<tmp.length; i++){
			res[i] = Integer.parseInt(tmp[i].trim());
		}
		return res;
	}

	// dumps the matrix to file
	void save(String fileName){
		try{
			final BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(String.valueOf(size)+"\n");
			out.write(String.valueOf(data[0]));
			for(int i=1; i<data.length; i++){
				out.write(","+String.valueOf(data[i]));
			}
			out.write("\n");
			out.close();
		}catch(IOException i) {
			i.printStackTrace();
		}
	}

	boolean equals(Matrix m){
		return (size == m.size) && Arrays.equals(data,m.data);
	}

	// computes the probability of collision for each user
	float[] pCollisionPerUser(){
		float[] probs = new float[size];
		for(int i=0; i<probs.length; i++){
			probs[i] = (new Histo(this.getAll(i))).probCollision();
		}
		return probs;
	}
}


class TestMatrix {
	public static void main(String[] args) throws Exception {

		int size = 4;
		Matrix m = new Matrix(size);
		for(int r=0; r<size; r++){
			for(int c=0; c<size; c++){
				System.out.println(r+","+c+" -> "+m.get(r,c));
			}
		}
		m.set(0,0,0);
		// System.out.println(Arrays.toString(m.getAll(3)));
		m.sum(m);
		m.save("tmp-234");
		m.equals(new Matrix("tmp-234"));
	}
}
