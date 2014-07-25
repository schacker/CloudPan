package org.cloudpan.biz.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cloudpan.biz.AccountService;
import org.cloudpan.entity.Account;

/**
 * 基于内存的用户管理
 */
public class MemoryAccountServiceImpl implements AccountService{
	private static Map<String,Account> accts = 
			new ConcurrentHashMap<String,Account>();
	@Override
	public boolean addAccount(Account acct) {
		accts.put(acct.getName(), acct);
		return true;
	}

	@Override
	public Account queryAccountByName(String name) {
		return accts.get(name);
	}

}
