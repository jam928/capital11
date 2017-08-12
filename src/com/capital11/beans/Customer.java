package com.capital11.beans;

public class Customer {
	
	private int cid;
	private String name;
	private String email;
	private String username;
	private String password;
	private String birthday;
	private String gender;
	private Account customerAccount;
	
	
	public Customer(int cid,String name, String email, String username, String password, String birthday, String gender, Account customerAccount) {
		this.cid = cid;
		this.name = name;
		this.email = email;
		this.username = username;
		this.password = password;
		this.birthday = birthday;
		this.gender = gender;
		this.customerAccount = customerAccount;
	}
	
	public Customer(int cid,String name, String email, String username, String password, String birthday, String gender) {
		this.cid = cid;
		this.name = name;
		this.email = email;
		this.username = username;
		this.password = password;
		this.birthday = birthday;
		this.gender = gender;
	}
	
	


	public int getCid()
	{
		return cid;
	}
	public void setCid(int cid)
	{
		this.cid = cid;
	}
	public Account getCustomerAccount() {
		return customerAccount;
	}


	public void setCustomerAccount(Account customerAccount) {
		this.customerAccount = customerAccount;
	}


	public String getPassword()
	{
		return password;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	
}
