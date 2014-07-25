package org.cloudpan.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 服务器
 */
public class CloudPanServer {
	private static boolean flag = true;
	//private static BlockingQueue<Task> queue = new LinkedBlockingQueue<Task>();
	public  static void main(String[] args) {
		ServerSocket  server = null;
		try {
			 server = new ServerSocket(9527);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("启动服务器成功 !");
		while(flag){
			try {
				Socket socket = server.accept();
				System.out.println("客户机["+socket.getRemoteSocketAddress()+"] 成功连接 !");
				//new CloudPanServerThread(socket, queue).start();
				new CloudPanServerThread(socket).start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
