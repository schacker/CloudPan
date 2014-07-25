package org.cloudpan.client;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.io.FileUtils;
import org.cloudpan.entity.Task;

/**
 * @author schacker
 * 客户端监控线程
 */
public class CloudPanClientMonitorThread  extends Thread{
	
	private BlockingQueue<Task> queue;
	private String filePath;
	private long sleepTime = 10*1000; //监听时间
	private Map<String,Long> matchDatas; //存在服务端的与本地文件进行比对，判断是否同步的补丁文件
	public CloudPanClientMonitorThread(BlockingQueue<Task> queue , String filePath, Map<String, Long> matchDatas) {
		this.queue = queue;
		this.filePath = filePath;
		this.matchDatas = matchDatas;
	}
	@Override
	public void run(){
		while(true){
			File file  = new File(filePath);
			if(!file.exists()){
				System.out.println("请设置同步目录 !");
				break;
			}
			if(queue.isEmpty()){
				System.out.println("队列为空，开始检查目录 :");
				try {
					listFile(new File(filePath));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}else{
				System.out.println("队列不为空，开始同步 ！");
			}
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * 遍历文件夹下文件
	 * @param file
	 * @throws IOException
	 */
	public void listFile(File file) throws IOException{
		if(file.isFile()){
			if(matchDatas.containsKey(file.getCanonicalPath())){ //getCanonicalPath将去掉路径中.、..等特殊字符,且该路径是唯一的。
				if(matchDatas.get(file.getCanonicalPath()) == FileUtils.sizeOf(file)){
					return;
				}
			}
			System.out.println(file.getCanonicalPath());
			Task task = new Task();
			try {
				System.out.println("创建任务 "+file.getCanonicalPath());
				task.setSourceFilePath(file.getCanonicalPath());
				task.setFilePath(file.getCanonicalPath().substring(filePath.length()));
				task.setSize(FileUtils.sizeOf(file));
				task.setFileType(1); //普通的文件
				queue.offer(task);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else if(file.isDirectory()){
			System.out.println(file.getName()+"  "+matchDatas.get(file.getCanonicalPath())+"  "+FileUtils.sizeOf(file));
			if(matchDatas.containsKey(file.getCanonicalPath())){
				if(matchDatas.get(file.getCanonicalPath()) == FileUtils.sizeOf(file)){
					return ;
				}
			}
			Task task = new Task();
			try {
				System.out.println("创建任务 "+file.getCanonicalPath());
				task.setSourceFilePath(file.getCanonicalPath());
				task.setFilePath(file.getCanonicalPath().substring(filePath.length()));
				task.setSize(FileUtils.sizeOf(file));
				task.setFileType(0); //目录
				queue.offer(task);
			} catch (Exception e) {
				e.printStackTrace();
			}
			matchDatas.put(file.getCanonicalPath(),FileUtils.sizeOf(file));
			File[] files = file.listFiles();
			if(files != null){
				for( File temp : files){
					listFile(temp);
				}
			}
		}
	}
}
