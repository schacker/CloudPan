package org.cloudpan.util.threadpool;

import java.util.concurrent.BlockingQueue;

/**
 * 工人类，任务的具体执行者.
 */
public class Worker extends Thread{
	private boolean flag = true;
	private BlockingQueue<Task> tasks; //任务队列
	public Worker(String name , BlockingQueue<Task> tasks){
		super(name);
		this.tasks = tasks;
	}
	@Override
	public void run(){
		while(flag){ 
			try {
				Task task = tasks.take(); //拿到任务就执行.
				task.run(); 
				System.out.println(Thread.currentThread().getName()+" 执行任务完毕 !");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean isFlag() {
		return flag;
	}
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
}
