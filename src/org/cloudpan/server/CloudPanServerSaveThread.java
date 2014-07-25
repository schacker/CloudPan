package org.cloudpan.server;

import java.util.HashMap;
import java.util.Map;

import org.cloudpan.entity.Account;

/**
 * 
 */
public class CloudPanServerSaveThread extends Thread{
	
	private Map<String,Long> matchDatas = new HashMap<String,Long>();
	private String filePath ;
	private Account acct;
	public CloudPanServerSaveThread(Account acct , String filePath , Map<String,Long> matchDatas) {
		this.matchDatas = matchDatas;
		this.filePath = filePath;
		this.acct = acct;
	}
	@Override
	public void run(){
		while(true){
			System.out.println(filePath+acct.getName()+"\\match.dat");
			CloudPanServerMatchFileUtil matchUtil = new CloudPanServerMatchFileUtil(filePath+acct.getName()+"\\match.dat");
			try {
				System.out.println("刷新匹配文件到硬盘 !");
				matchUtil.saveMatchFile(matchDatas);
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Thread.sleep(30*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
