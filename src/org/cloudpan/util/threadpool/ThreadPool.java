package org.cloudpan.util.threadpool;


/**
 * 线程池
 */
public interface ThreadPool {
	//启动线程池
	public void start();
	//提交任务
	public boolean submit(Task task);
	//关闭线程池
	public void shutdown();
	//判断是否存活
	public boolean isAlive();
}
