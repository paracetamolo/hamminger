import java.util.concurrent.*;

/**
   Fixed size pool that blocks the submitter of tasks if at full capacity.
   Simple way to throttle the producer and avoid wasting memory.
   Configure with nThreads equal to the number of threads and nTasks the number 
   of submittable tasks.
*/

public class Pool {
	final private int nThreads;
	final private int nTasks;
	final private Semaphore s;
	final private ExecutorService pool;
	private int cnt = 0;

	public Pool(int nThreads, int nTasks){
		this.nThreads = nThreads;
		this.nTasks = nTasks;
		this.s = new Semaphore(nTasks);
		this.pool = Executors.newFixedThreadPool(nThreads);
	}

	private class Wrap<T> implements Callable<T>{
		final private int id;
		final private Callable<T> fun;

		Wrap(int id, Callable<T> fun){
			this.id = id;
			this.fun = fun;
		}
		
		public T call() throws Exception {
			T res = fun.call();
			s.release();
			Util.log("released " + id);
			return res;
		}
	}

	public <T> Future<T> submit(Callable<T> task){
		s.acquireUninterruptibly(); // blocks
		cnt++;
		Util.log("Acquired "+ cnt);
		return pool.submit(new Wrap<T>(cnt, task));
	}

	public void shutdown(){
		pool.shutdown();
		s.acquireUninterruptibly(nTasks); // wait for all thread to terminate
		Util.log("Shutdown");
	}
}


class TestPool {
	public static void main(String[] args){
		Pool p = new Pool(2,2);
		for(int i=0; i<100; i++){
			final int fi = i;
			p.submit(new Callable<Integer>() {
					public Integer call() throws Exception {
						System.out.println("Start "+fi);
						Thread.sleep(10*1000);
						System.out.println("Stop  "+fi);
						return fi;
					}
				});
		}
	}
}
