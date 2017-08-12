package com.capital11.beans;

public class Account {
	private int acct_num;
	private String acct_type;
	private int cid;
	private float balance;
	
	
	
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public float getBalance() {
		return balance;
	}
	public void setBalance(float balance) {
		this.balance = balance;
	}
	public Account(String acct_type)
	{
		this.acct_type = acct_type;
	}
	public int getAcct_num() {
		return acct_num;
	}
	public void setAcct_num(int acct_num) {
		this.acct_num = acct_num;
	}
	public String getAcct_type() {
		return acct_type;
	}
	public void setAcct_type(String acct_type) {
		this.acct_type = acct_type;
	}
	
	
}
