package com.capital11.beans;

public class Transaction {
	private int tid;
	private String tr_type;
	private int acct_num;
	private float amount;
	private String date;
	
	public Transaction()
	{
		
	}
	
	public Transaction(String date, String tr_type, float amount) {
		this.date = date;
		this.tr_type = tr_type;
		this.amount = amount;
	}

	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public int getTid() {
		return tid;
	}
	public void setTid(int tid) {
		this.tid = tid;
	}
	public String getTr_type() {
		return tr_type;
	}
	public void setTr_type(String tr_type) {
		this.tr_type = tr_type;
	}
	public int getAcct_num() {
		return acct_num;
	}
	public void setAcct_num(int acct_num) {
		this.acct_num = acct_num;
	}
	public float getAmount() {
		return amount;
	}
	public void setAmount(float amount) {
		this.amount = amount;
	}
	
	
}
