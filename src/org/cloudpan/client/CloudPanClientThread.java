package org.cloudpan.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.cloudpan.algorithm.rsync.Chunk;
import org.cloudpan.algorithm.rsync.Patch;
import org.cloudpan.algorithm.rsync.Rsync;
import org.cloudpan.constant.CloudPanMessageType;
import org.cloudpan.entity.Account;
import org.cloudpan.entity.CloudPanMessage;
import org.cloudpan.entity.Task;

/**
 */
public class CloudPanClientThread extends Thread{
	
	private Socket socket;
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	private BlockingQueue<Task> queue;
	private Account account;
	private Map<String,Long> matchDatas;
	public CloudPanClientThread(Socket socket , 
			ObjectOutputStream oos , 
			ObjectInputStream ois, 
			BlockingQueue<Task> queue,
			Account account , 
			Map<String,Long>matchDatas){
		this.socket = socket;
		this.oos  = oos;
		this.ois = ois;
		this.queue = queue;
		this.account = account;
		this.matchDatas = matchDatas;
	}
	@Override
	public void run(){
		while(true){
			long start=0;
			long end=0;
			try {
				Task task = queue.take();
				if(task.getFileType() == 1){ //普通的文件
					//1. 需要从服务器拿到checksum;
					System.out.println("获取到任务 "+task.getSourceFilePath());
					CloudPanMessage msg = new CloudPanMessage();
					msg.setMsgType(CloudPanMessageType.RSYNC_CHECKSUM);
					msg.setAcct(account);
					msg.setFileName(task.getFilePath());
					msg.setSourceFileName(task.getSourceFilePath());
					msg.setFileLength(task.getSize());
					oos.writeObject(msg);
					//2. 利用Rsync算法产生Patch
					start = System.currentTimeMillis();
					CloudPanMessage rMsg = (CloudPanMessage) ois.readObject();
					Map<Integer,List<Chunk>> checkSums = rMsg.getCheckSums();
					System.out.println("成功从服务器获取到校验和 ！");
					Patch patch = Rsync.createPatch(checkSums, task.getSourceFilePath(),0,task.getSize());
					
					//3. 向服务器发送Patch
					System.out.println("利用rsync算法成功生成patch ！");
					CloudPanMessage msg02 = new CloudPanMessage();
					msg02.setMsgType(CloudPanMessageType.RSYNC_PATCH);
					msg02.setAcct(account);
					msg02.setFileName(task.getFilePath());
					msg02.setSourceFileName(task.getSourceFilePath());
					msg02.setFileLength(task.getSize());
					msg02.setPatch(patch);
					oos.writeObject(msg02);
				}else if(task.getFileType() ==0){ //如果是目录
					//只需要告诉服务器创建一个对应的目录就可以了.
					CloudPanMessage msg = new CloudPanMessage();
					msg.setMsgType(CloudPanMessageType.RSYNC_DIR);
					msg.setAcct(account);
					msg.setFileName(task.getFilePath());
					msg.setSourceFileName(task.getSourceFilePath());
					msg.setFileLength(task.getSize());
					oos.writeObject(msg);
				}
				//获取到服务器的回复
				CloudPanMessage rMsg02 = (CloudPanMessage) ois.readObject();
				if(rMsg02.getMsgType() == CloudPanMessageType.SUCCESS){
					end = System.currentTimeMillis();
					System.out.println("time:"+(end-start));
					matchDatas.put(task.getSourceFilePath(), task.getSize());
				}
				System.out.println(rMsg02.getContent());
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public ObjectOutputStream getOos() {
		return oos;
	}
	public void setOos(ObjectOutputStream oos) {
		this.oos = oos;
	}
	public ObjectInputStream getOis() {
		return ois;
	}
	public void setOis(ObjectInputStream ois) {
		this.ois = ois;
	}

}
