package org.cloudpan.client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.cloudpan.constant.CloudPanMessageType;
import org.cloudpan.entity.Account;
import org.cloudpan.entity.CloudPanMessage;
import org.cloudpan.entity.Task;

/**
 * 客户端
 */
public class CloudPanClient {
	private static Scanner scanner = new Scanner(System.in);
	private static ObjectOutputStream oos; //输入输出流
	private static ObjectInputStream  ois; 
	private static String filePath="F:\\local"; //本地同步路径
	private static BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>(); //任务队列
	private static Map<String, Long> matchDatas = new ConcurrentHashMap<String, Long>(); //使用安全的hashMap集合，匹配补丁数据
	private static Account account;
	public static void main(String[] args) throws Exception{
		Socket socket   = new Socket("localhost", 9527); //传入服务端地址，端口号
		oos = new ObjectOutputStream(socket.getOutputStream());
		ois = new ObjectInputStream(socket.getInputStream());
		System.out.println("成功连接上服务器 !");
		// 1. 显示主界面
		showMainMenu();
		// 2. 登录成功后启动监听线程去监听一个目录.
		new CloudPanClientMonitorThread(queue, filePath, matchDatas).start();
		//3 . 处理消息线程
		new CloudPanClientThread(socket, oos, ois,queue,account,matchDatas).start();
	}
	

	public static void showMainMenu() throws Exception{
		while(true){
			System.out.println("----------------------------");
			System.out.println("-    CloudPan System 0.1     -");
			System.out.println("-    请输入你的选择:           -");
			System.out.println("-    0 : 创建账户                  -");
			System.out.println("-    1 : 登录                         -");
			System.out.println("-    x : 退出                         -");
			System.out.println("----------------------------");
			String input = scanner.nextLine();
			if("0".equals(input)){ //创建账户
				System.out.println("请输入你的账户 :");
				String name = scanner.nextLine();
				System.out.println("请输入你的密码 :");
				String pwd = scanner.nextLine();
				System.out.println("请重复你的密码 :");
				String pwd2 = scanner.nextLine();
				if(pwd.equals(pwd2)){
					//向服务器发送注册的消息.
					System.out.println("发送注册消息 !");
					Account acct = new Account(name,pwd);
					CloudPanMessage msg = new CloudPanMessage();
					msg.setMsgType(CloudPanMessageType.ACCOUNT_CREATE);
					msg.setAcct(acct);
					msg.setContent("用户注册");
					oos.writeObject(msg);
					//接收服务器的响应消息
					CloudPanMessage rMsg = (CloudPanMessage) ois.readObject();
					if(rMsg.getMsgType() == CloudPanMessageType.SUCCESS){
						System.out.println("用户 ["+name+"] 成功完成注册 !");
						continue;
					}else{
						System.out.println("用户 ["+name+"] 注册失败 , 失败原因如下 : "+rMsg.getContent());
						continue;
					}
				}else{
					System.out.println("两次密码不一致 !");
					continue;
				}
			}else if("1".equals(input)){ //登录
				System.out.println("请输入你的账户 :");
				String name = scanner.nextLine();
				System.out.println("请输入你的密码 :");
				String pwd = scanner.nextLine();
				//发送登录的消息
				System.out.println("发送登录消息 !");
				Account acct = new Account(name,pwd);
				CloudPanMessage msg = new CloudPanMessage();
				msg.setMsgType(CloudPanMessageType.ACCOUNT_LOGIN);
				msg.setAcct(acct);
				msg.setContent("用户登录 !");
				oos.writeObject(msg);
				//接收服务器的响应消息
				CloudPanMessage rMsg = (CloudPanMessage) ois.readObject();
				if(rMsg.getMsgType() == CloudPanMessageType.SUCCESS){
					System.out.println("用户 ["+name+"] 成功登录 !");
					matchDatas = rMsg.getMatchFile();
					account = acct;
					break;
				}else{
					System.out.println("用户 ["+name+"] 登录失败 , 失败原因如下 : "+rMsg.getContent());
					continue;
				}
			}else if("x".equals(input.toLowerCase())){
				System.out.println("用户退出系统 !");
				CloudPanMessage msg = new CloudPanMessage();
				msg.setMsgType(CloudPanMessageType.ACCOUNT_LOGOUT);
				oos.writeObject(msg);
				CloudPanMessage rMsg = (CloudPanMessage) ois.readObject();
				System.out.println(rMsg.getContent());
				System.exit(1);
			}else{
				System.out.println("无效的命令 !");
			}
		}
		
	}
	
	
}
