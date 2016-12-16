import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import org.apache.commons.io.*;

class Vcf {

	/**
	   This class handles one vcf sample: parsing, computing hamming distances, 
	   saving distance matrix, compute and save collision probabilities.
	 */
	static class Handler implements Callable<float[]> {
		private static final int totalUsers = 2504;
		private final String name;
		private final List<String> chunk;
		private final Matrix m;
		
		public Handler(String name, List<String> chunk){
			this.name = name;
			this.chunk = chunk;
			this.m = new Matrix(totalUsers);
		}
		
		/**
		   Given a row of values, computes all pair-wise hamming distances and 
		   updates the matrix. This is called for every line so it can increase 
		   distances in the matrix by 1 at most.
		*/
		private void hamming(int[] values){
			for(int i=0; i<values.length; i++){
				for(int j=i+1; j<values.length; j++){
					if (values[i] != values[j])
						m.set(i, j, m.get(i,j)+1);
				}
			}
		}

		/**
		   Parses a vcf line and produces 2504 values in {0,1,2} representing 
		   the three possible states of a SNP: 
		   0) no mutation, 1) single mutation (on either side), 2) double mutation.
		*/
		private int[] parse(String s) throws Exception {
			String[] fields = s.split("\t");
			int[] values = new int[totalUsers];
			for(int i=0; i<totalUsers; i++){
				String f = fields[i+9];
				if(f.equals("0|0")) {values[i] = 0;}
				else if(f.equals("0|1")) {values[i] = 1;}
				else if(f.equals("1|0")) {values[i] = 1;}
				else if(f.equals("1|1")) {values[i] = 2;}
				else {throw new Exception("field: "+f);}
			}
			return values;
		}

		public float[] call() throws Exception {
			float[] probs = null;
			for(String line : chunk)
				hamming(parse(line));

			m.save(name);
			probs = m.pCollisionPerUser();
			return probs;
		}
	}

	/**
	   Filters away lines which are not SNP or that are multiallelic SNP.
	 */
	static class SNPIterator implements Iterator<String> {
		private final Iterator<String> it;
		private String line;

		private boolean goodLine(String line){
			return line.contains("VT=SNP") && !line.contains("MULTI_ALLELIC");
		}
		
		public SNPIterator(Iterator<String> it){
			this.it = it;
			this.line = null;
		}
		
		public boolean hasNext(){
			if(line != null){return true;}
			else {
				while(it.hasNext()){
					line = it.next();
					if(goodLine(line)) return true;
				}
				return false;
			}
		}

		public String next(){
			if(hasNext()){
				String res = line;
				line = null;
				return res;
			} else {
				throw new NoSuchElementException();
			}
		}
	}
}


/**
   This class contains the commands that are called from the main.
*/
class Commands {
	
	/**
	   called as: hamminger chr22.vcf 100
	   produces: chr22_K100_N{1...n} and chr22_K100_prob

	   Parses the vcf file chr22.vcf and splits it in contiguous samples of size 100. 
	   For each sample computes all the pair-wise hamming distances for all the 2504 users.
	   For each sample i, the distance matrix is saved to chr22_K100_N$i. 
	   The probability of collision for each user is saved to chr22_K100_prob, one line per sample.
	*/
	public static void hamminger(String[] args) throws Exception {

		final String srcFileName = args[0]; // chr22.vcf
		final int chunkSize = Integer.parseInt(args[1]); // size of genome or k

		final Pool pool = new Pool(3,6);
		final BufferedReader br = new BufferedReader(new FileReader(srcFileName));
		final Iterator<String> it = new Vcf.SNPIterator(new LineIterator(br));
		final ChunkIterator<String> cit = new ChunkIterator<String>(chunkSize, it);
		final List<Future<float[]>> result = new ArrayList<Future<float[]>>();

		int chunkCnt = 0;
		while (cit.hasNext()){
			chunkCnt++;
			String dstFileName = srcFileName.substring(0,srcFileName.length() -4)
				+"_K"+chunkSize+"_N"+chunkCnt;
			// submit blocks so that we don't parse more than we can handle
			result.add(pool.submit(new Vcf.Handler(dstFileName, cit.next())));
		}
		br.close();
		pool.shutdown();
		Util.log("Finished reading input.");

		String dstFileName = srcFileName.substring(0,srcFileName.length() -4)+"_K"+chunkSize+"_probs";
		BufferedWriter bw = new BufferedWriter(new FileWriter(dstFileName));
		
		for(Future<float[]> f : result){
			float[] a = f.get(); // blocks
			bw.write(Util.arrayToCsv(a)+"\n");
		}
		bw.close();
	}

	
	/**
	   merger old/chr22_K100_N1 10
	   merges the distances of old/chr22_K100_N{1..10} into old/chr22_K1000_N1
	   merges the distances of old/chr22_K100_N{11..20} into old/chr22_K1000_N2
	   and so on.
	*/
	public static void merger(String[] args) throws Exception {

		boolean strict = true;
		String tmp = args[0].substring(0,args[0].lastIndexOf("_"));
		int chunkSize = Integer.parseInt(tmp.substring(tmp.lastIndexOf("_")+2));
		String base = tmp.substring(0,tmp.lastIndexOf("_"));

		System.out.println(base + " " + chunkSize);
		
		final int nFiles = Integer.parseInt(args[1]);

		String srcFile = base +"_K"+ chunkSize + "_N";
		String dstFile = base +"_K"+ chunkSize*nFiles + "_N";
		File f = null;
		int cnt = 1;
		Matrix m = new Matrix(srcFile+cnt);
		cnt++;

		String probFile = base +"_K"+ chunkSize*nFiles +"_probs";
		BufferedWriter bw = new BufferedWriter(new FileWriter(probFile));

		while((f = new File(srcFile+cnt)).exists()){
			if (cnt % nFiles == 0){
				String file = dstFile+(cnt/nFiles);
				m.save(file);
				bw.write(Util.arrayToCsv(m.pCollisionPerUser())+"\n");
				m = new Matrix(srcFile+cnt);
			} else {
				m.sum(new Matrix(srcFile+cnt));
			}
			cnt++;
		}
		if(!strict){
			String file = dstFile+(cnt/nFiles);
			m.save(file);
			bw.write(Util.arrayToCsv(m.pCollisionPerUser())+"\n");
		}
		bw.close();
	}


	/**
	   collisioner chr22_K100_probs
	   Takes a file where each line contains the collision probability of all users, 
	   different lines are produced by different samples, all of the same size. 
	   For each user (for each column), computes average over samples and saves it to 
	   chr22_K100_probs_avg
	   Additionally prints some statistics over all distances of all users:
	   [average, variance, minimum, 1st quartile, median, 3rd quartile, maximum]
	*/
	public static void collisioner(String[] args) throws Exception {

		final String srcFileName = args[0];

		float[][] data = Util.arrayOfCsv(srcFileName);

		// average per column (each column is a user)
		float[] usersAvg = new float[data[0].length];
		for(int r=0; r<data.length; r++){
			for(int c=0; c<data[0].length; c++){
				usersAvg[c] += data[r][c];
			}
		}
		for(int c=0; c<data[0].length; c++){
			usersAvg[c] /= data.length;
		}

		String dstFileName = srcFileName + "_avg";
		BufferedWriter bw = new BufferedWriter(new FileWriter(dstFileName));
		bw.write(Util.arrayToCsv(usersAvg)+"\n");
		bw.close();

		List<Float> distances = new ArrayList<Float>();
		for(int r=0; r<data.length; r++){
			for(int c=0; c<data[0].length; c++){
				distances.add(data[r][c]);
			}
		}
		List<Float> stat = Util.stat(distances);
		System.out.println(Arrays.toString(stat.toArray()));
	}
}


/**
   This is the entry point that simply chooses which command to execute.
*/
class Main {
	public static void main(String[] args) throws Exception {
		String[] newArgs = Arrays.copyOfRange(args,1,args.length);
		switch(args[0]){
		case "hamming" : 
			Commands.hamminger(newArgs);
			break;
		case "merge" :
			Commands.merger(newArgs);
			break;
		case "collision" :
			Commands.collisioner(newArgs);
			break;
		}
	}
}
