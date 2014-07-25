package org.cloudpan.biz;

import org.cloudpan.entity.Account;

/**
 * 用户的管理接口
 */
public interface AccountService {
	
	public boolean addAccount(Account acct);
	public Account  queryAccountByName(String name);
}
