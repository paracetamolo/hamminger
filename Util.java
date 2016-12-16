import java.util.*;
import java.io.*;

/**
   A few helping functions.
*/

class Util {
	// print log messages if enabled
	private static final boolean debug = false;
	public static void log(String s){
		if(debug){System.out.println(s);}
	} 


	public static String arrayToCsv(float[] a){
		StringBuilder res = new StringBuilder();
		res.append(String.valueOf(a[0]));
		for(int i=1; i<a.length; i++){
			res.append(","+String.valueOf(a[i]));
		}
		return res.toString();
	}

	// reads a file and parses a array of floats
	public static float[][] arrayOfCsv(String fileName){
		ArrayList<float[]> a = new ArrayList<float[]>();
		try{
			final BufferedReader in = new BufferedReader(new FileReader(fileName));
			String line = null;
			String[] fields = null;
			float[] nums = null;
			while((line = in.readLine()) != null) {
				fields = line.split(",");
				nums = new float[fields.length];
				for(int i=0; i<fields.length; i++){
					nums[i] = Float.parseFloat(fields[i]);
				}
				a.add(nums);
			}
			in.close();

		}catch(IOException i) {
			i.printStackTrace();
		}
		float[][] res = new float[a.size()][];
		for(int i=0; i<a.size(); i++) res[i] = a.get(i);
		return res;
	}

	// average of a list of floats
	public static float avg(List<Float> a){
		float sum = 0;
		for(int i=0; i<a.size(); i++){
			sum += a.get(i);
		}
		return sum / (float) a.size();
	}

	// variance of a list of floats
	public static float variance(float avg, List<Float> a){
		float var = 0;
		for(int i=0; i<a.size(); i++){
			var += Math.pow(avg - a.get(i), 2);
		}
		return var / (float) a.size();
	}

	// computes [average, variance, minimum, 1st quartile, median, 3rd quartile, maximum]
	public static List<Float> stat(List<Float> a){
		List<Float> b = new ArrayList<Float>(a);
		int l = b.size();
		Collections.sort(b);
		float avg = avg(a);
		float var = variance(avg, a);
		List<Float> res = new ArrayList<Float>();
		res.add(avg);
		res.add(var);
		res.add(b.get(0));
		res.add(b.get(l/4));
		res.add(b.get(l/2));
		res.add(b.get(l*3/4));
		res.add(b.get(l-1));
		return res;
	}
}
