package org.cloudpan.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudpan.algorithm.MD5;
import org.cloudpan.algorithm.rsync.Chunk;
import org.cloudpan.algorithm.rsync.Patch;
import org.cloudpan.algorithm.rsync.Rsync;
import org.cloudpan.biz.AccountService;
import org.cloudpan.biz.impl.FileAccountServiceImpl;
import org.cloudpan.constant.CloudPanMessageType;
import org.cloudpan.entity.Account;
import org.cloudpan.entity.CloudPanMessage;

/**
 * 服务器线程
 */
public class CloudPanServerThread extends Thread{
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private AccountService  acctService = new FileAccountServiceImpl();
	private Map<String,Long> matchDatas = new HashMap<String,Long>();
	private boolean flag = true;
	private static String filePath="F:\\remote\\";
	//private BlockingQueue<Task> queue;
	public CloudPanServerThread( Socket socket) {
		try {
			ois = new ObjectInputStream(socket.getInputStream());
			oos = new ObjectOutputStream(socket.getOutputStream());
			//this.queue = queue;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run(){
		while(flag){
			try {
				CloudPanMessage msg = (CloudPanMessage) ois.readObject();
				switch(msg.getMsgType()){
					case CloudPanMessageType.ACCOUNT_CREATE:
						createAccount(msg.getAcct());
						break;
					case CloudPanMessageType.ACCOUNT_LOGIN:
						loginAccount(msg.getAcct());
						break;
					case CloudPanMessageType.ACCOUNT_LOGOUT:
						loginout(msg.getAcct());
						flag =false;
						break;
					case CloudPanMessageType.RSYNC_CHECKSUM:
						calcCheckSum(msg);
						break;
					case CloudPanMessageType.RSYNC_PATCH:
						applyPatch(msg);
						break;
					case CloudPanMessageType.RSYNC_DIR: //创建目录
						createDir(msg);
						break;
					default:
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
	}
	/**
	 * 创建目录
	 * @param msg
	 * @throws IOException
	 */
	private void createDir(CloudPanMessage msg) throws IOException {
		String clientFilePath = msg.getFileName();
		File file  = new File(filePath+msg.getAcct().getName()+clientFilePath);
		if( !file.exists() ){
			file.mkdirs();
		}
		matchDatas.put(msg.getSourceFileName(),msg.getFileLength());
		CloudPanMessage msg02 = new CloudPanMessage();
		msg02.setMsgType(CloudPanMessageType.SUCCESS);
		msg02.setContent("文件同步成功 !");
		oos.writeObject(msg02);
	}
	/**
	 * 将Patch补丁传到服务端
	 * @param msg
	 * @throws Exception
	 */
	private void applyPatch(CloudPanMessage msg) throws Exception {
		String clientFilePath = msg.getFileName();
		Patch patch = msg.getPatch();
		System.out.println(filePath+msg.getAcct().getName()+clientFilePath);
		File file  = new File(filePath+msg.getAcct().getName()+clientFilePath);
		if(!file.exists()){
			File parent = file.getParentFile();
			if(!parent.exists()){
				parent.mkdirs();
			}
			file.createNewFile();
		}
		Rsync.createNewFile(patch, filePath+msg.getAcct().getName()+clientFilePath);
		//生成一个HadoopTask ,放进队列中,让线程池去从队列中获取任务并处理.
		/*Task task = new HadoopTask(
					filePath+msg.getAcct().getName()+clientFilePath ,
					"/hadoop/"+msg.getAcct().getName()+clientFilePath);
		queue.offer(task);*/
		
		matchDatas.put(msg.getSourceFileName(),msg.getFileLength());
		CloudPanMessage msg02 = new CloudPanMessage();
		msg02.setMsgType(CloudPanMessageType.SUCCESS);
		msg02.setContent("文件同步成功 !");
		oos.writeObject(msg02);
	}
	/**
	 * 计算校验和
	 * @param msg
	 * @throws Exception
	 */
	private void calcCheckSum(CloudPanMessage msg) throws Exception {
		String clientFilePath = msg.getFileName();
		Map<Integer,List<Chunk>> checkSums = Rsync.calcCheckSum(filePath+clientFilePath);
		if(checkSums == null){
			checkSums = new HashMap<Integer,List<Chunk>>();
		}
		CloudPanMessage msg02 = new CloudPanMessage();
		msg02.setMsgType(CloudPanMessageType.RSYNC_CHECKSUM);
		msg02.setCheckSums(checkSums);
		oos.writeObject(msg02);
	}
	/**
	 * 账户登录
	 * @param acct
	 * @throws IOException
	 */
	private void loginAccount(Account acct) throws IOException {
		System.out.println("[Server] 用户登录");
		Account temp  =  acctService.queryAccountByName(acct.getName());
		if(temp == null){
			CloudPanMessage msg = new CloudPanMessage();
			msg.setMsgType(CloudPanMessageType.ERROR);
			msg.setContent("此账户不存在 !");
			oos.writeObject(msg);
			return;
		}
		if(temp.getPassword().equals(MD5.getMD5(acct.getPassword().getBytes()))){
			CloudPanServerMatchFileUtil matchUtil = new CloudPanServerMatchFileUtil(filePath+acct.getName()+"\\match.dat");
			try {
				matchDatas = matchUtil.parseMatchFile();
				//启动一个定时刷新匹配文件到硬盘的线程
				new CloudPanServerSaveThread(acct, filePath, matchDatas).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
			CloudPanMessage msg = new CloudPanMessage();
			msg.setMsgType(CloudPanMessageType.SUCCESS);
			msg.setMatchFile(matchDatas);
			msg.setContent("服务器校验通过 !");
			oos.writeObject(msg);
		}else{
			CloudPanMessage msg = new CloudPanMessage();
			msg.setMsgType(CloudPanMessageType.ERROR);
			msg.setContent("服务器校验失败 !");
			oos.writeObject(msg);
		}
	}
	/**
	 * 用户注册
	 * @param acct
	 * @throws IOException
	 */
	private void createAccount(Account acct) throws IOException {
		System.out.println("[Server] 用户注册");
		Account temp  =  acctService.queryAccountByName(acct.getName());
		if( temp !=null ){ //如果查询到该用户名的账户，失败.
			CloudPanMessage msg = new CloudPanMessage();
			msg.setMsgType(CloudPanMessageType.ERROR);
			msg.setContent("服务器已存在该用户 !");
			oos.writeObject(msg);
		}else{
			CloudPanMessage msg = new CloudPanMessage();
			msg.setMsgType(CloudPanMessageType.SUCCESS);
			msg.setContent("成功注册改用户  !");
			oos.writeObject(msg);
			acct.setPassword(MD5.getMD5(acct.getPassword().getBytes()));
			acctService.addAccount(acct);
		}
	}
	/**
	 * 退出登录
	 * @param acct
	 * @throws IOException
	 */
	private void loginout(Account acct) throws IOException {
		CloudPanMessage msg = new CloudPanMessage();
		msg.setMsgType(CloudPanMessageType.SUCCESS);
		msg.setContent("成功退出  !");
		CloudPanServerMatchFileUtil matchUtil = new CloudPanServerMatchFileUtil(filePath+acct.getName()+"\\match.dat");
		try {
			matchUtil.saveMatchFile(matchDatas);
		} catch (Exception e) {
			e.printStackTrace();
		}
		oos.writeObject(msg);
	}	
}
