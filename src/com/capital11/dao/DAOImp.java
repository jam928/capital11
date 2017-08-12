package com.capital11.dao;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import com.capital11.beans.Customer;
import com.capital11.beans.Transaction;

public interface DAOImp {
	public void addCustomer(Customer customer);
	public Customer login(String username, String password, HttpServletRequest request);
	public float retrieveBalance(Customer customer);
	public ArrayList<Transaction> listTransactions(Customer customer);
	public void addTransaction(Transaction transaction, Customer customer, HttpServletRequest request);
	public int retrieveAcctNum(Customer customer);
	public Boolean checkUserName(String username);
	public void updateCustomer(int cid, String email, String username, String password);
	public Customer findCustomer(String username, HttpServletRequest request);
	public void resetCustomer(int cid, String password);
	public void sendWelcomeEmail(String name,String email);
}
