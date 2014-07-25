package org.cloudpan.biz.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.cloudpan.biz.AccountService;
import org.cloudpan.entity.Account;

/**
 * 基于文件的账户管理
 */
@SuppressWarnings("unchecked")
public class FileAccountServiceImpl implements AccountService{

	private static ObjectInputStream ois;
	private static ObjectOutputStream oos;
	private static String fileName="data/user.dat";
	private static Map<String, Account> accts = new HashMap<String,Account>();
	static{
		try {
			File file = new File(fileName);
			if( file.exists() ){
				ois = new ObjectInputStream(new FileInputStream(fileName));
				accts = (Map<String,Account>)ois.readObject();
			}
		}  catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if(ois != null){
					ois.close();
				}
				if(oos != null){
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	@Override
	public boolean addAccount(Account acct) {
		accts.put(acct.getName(), acct);
		try {
			File file = new File(fileName);
			if( !file.exists() ){
				file.createNewFile();
			}
			oos = new ObjectOutputStream(new FileOutputStream(fileName));
			oos.writeObject(accts);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if(ois != null){
					ois.close();
				}
				if(oos != null){
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public Account queryAccountByName(String name) {
		return accts.get(name);
	}

}
