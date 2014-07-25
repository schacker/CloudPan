package org.cloudpan.util.threadpool;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 简单的线程池。
 */
public class SimpleThreadPool implements ThreadPool{
	private Vector<Worker> workers = new Vector<Worker>();
	private BlockingQueue<Task> tasks = new LinkedBlockingQueue<Task>();
	public SimpleThreadPool(BlockingQueue<Task> tasks, int size){
		//将工人加入到线程池
		for( int i = 1; i <= size; i++){
			Worker worker = new Worker("工人【"+i+"】",tasks);
			workers.add(worker);
		}
	}
	
	@Override
	public void start(){
		for(Worker worker : workers){
			worker.start();
		}
	}
	@Override
	public boolean submit(Task task) {
		try {
			tasks.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return true;
	}
	@Override
	public void shutdown() {
		for(Worker worker : workers){
			worker.setFlag(false);
		}
	}

	@Override
	public boolean isAlive() {
		return true;
	}

}
