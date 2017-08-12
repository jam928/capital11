package com.capital11.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.capital11.beans.Customer;
import com.capital11.beans.Transaction;
import com.capital11.util.DBUtil;

public class DAO implements DAOImp{
	private String userEmail = "uremail@uremail.com";
	private String password = "urpassword";
	
	@Override
	public void addCustomer(Customer customer) {
		try(Connection conn = DBUtil.getConnection();)
		{
			PreparedStatement stmt = conn.prepareStatement("insert into customer(cid,name,email,username,password,birthday,gender) values(?,?,?,?,?,?,?)");
			stmt.setInt(1, customer.getCid());
			stmt.setString(2, customer.getName());
			stmt.setString(3, customer.getEmail());
			stmt.setString(4, customer.getUsername());
			stmt.setString(5, customer.getPassword());
			stmt.setString(6, customer.getBirthday());
			stmt.setString(7, customer.getGender());
			stmt.executeUpdate();
			
			PreparedStatement stmt2 = conn.prepareStatement("insert into account(acct_type, cid) values (?,?)");
			stmt2.setString(1, customer.getCustomerAccount().getAcct_type());
			stmt2.setInt(2, customer.getCid());
			stmt2.executeUpdate();
			// sendWelcomeEmail(customer.getName(),customer.getEmail());

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public Customer login(String username, String password, HttpServletRequest request) {
		ArrayList<Customer> customerList = new ArrayList<>();
		
		Customer currentCustomer = null;
		
		try(Connection conn = DBUtil.getConnection();)
		{
			// SQL statement to get users from the database
			String sql = "SELECT * FROM customer";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			ResultSet rs = pstmt.executeQuery();
			
			// process rows
			while(rs.next())
			{
				int cid = rs.getInt(1);
				String name = rs.getString(2);
				String email = rs.getString(3);
				String uname = rs.getString(4);
				String pw = rs.getString(5);
				String bd = rs.getString(6);
				String gender = rs.getString(7);
				customerList.add(new Customer(cid,name,email,uname,pw,bd,gender));
			}
			// check if the username and password matches a customer in the database
			for(Customer cust: customerList)
			{
				if(cust.getPassword().equals(password) && cust.getUsername().equals(username))
					currentCustomer = cust;
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
		if(currentCustomer == null)
			return null;
		// retrieve customer balance and display in the main page
		Float balance = retrieveBalance(currentCustomer);
		// Add the user to the session and the currentbalance
		HttpSession session = request.getSession();
		session.setAttribute("currentUser", currentCustomer);
		request.setAttribute("balance", balance);
		request.setAttribute("listOfTransactions", listTransactions(currentCustomer));
		request.setAttribute("name", currentCustomer.getName());
		
		return currentCustomer;
	}
	@Override
	public float retrieveBalance(Customer customer)
	{
		try(Connection conn = DBUtil.getConnection();)
		{
			String sql = "SELECT balance " +
						 "FROM account, customer " + 
						 "WHERE account.cid = ?";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1,customer.getCid());
			ResultSet myRs = pstmt.executeQuery();
			
			if(myRs.next())
			{
				float balance = myRs.getFloat("balance");
				return balance;
			}
		}catch(SQLException e)
		{
			e.printStackTrace();
		}
		return 0;
	}
	
	@Override
	public ArrayList<Transaction> listTransactions(Customer customer)
	{	
		ArrayList<Transaction> listOfTransactions = new ArrayList<>();
		
		// cid is 85670 & 3
		try(Connection conn = DBUtil.getConnection();)
		{
			String sql = "SELECT dateTrans,tr_type,amount " +
					 	 "FROM customer, transaction, account " +
					     "WHERE transaction.cid = ? " +
					     "AND transaction.acct_num = account.acct_num " + 
					     "AND customer.cid = transaction.cid " + 
                         "AND customer.cid = account.cid ";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, customer.getCid());
			ResultSet myRs = pStmt.executeQuery();
			
			// retrieve results
			while(myRs.next())
			{
				String date = myRs.getString("dateTrans");
				String tr_type = myRs.getString("tr_type");
				Float amount = myRs.getFloat("amount");
				
				// add the results to the arraylist
				listOfTransactions.add(new Transaction(date,tr_type,amount));
			}
			return listOfTransactions;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@Override
	public void addTransaction(Transaction transaction, Customer customer, HttpServletRequest request) {
		try(Connection conn = DBUtil.getConnection();)
		{
			// update the balance
			float updatedBalance = 0;
						
			if(transaction.getTr_type().equals("withdrawl"))
			{
				updatedBalance = retrieveBalance(customer) - transaction.getAmount();
				// if the withdrawl is more than the customer's balance
				if(updatedBalance < 0)
				{
					updatedBalance = retrieveBalance(customer);
					return;
				}
								
			}
			else
			{
				updatedBalance = retrieveBalance(customer) + transaction.getAmount();
			}
			String sql = "INSERT INTO transaction(tr_type, acct_num, amount, dateTrans,cid) values (?,?,?,?,?)";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, transaction.getTr_type());
			pStmt.setInt(2, retrieveAcctNum(customer));
			pStmt.setFloat(3, transaction.getAmount());
			pStmt.setString(4, transaction.getDate());
			pStmt.setInt(5, customer.getCid());
			pStmt.executeUpdate();
			
			
			String sql2 = "UPDATE account " +
						 "SET balance = ? " + 
					     "WHERE account.cid = ?";
			PreparedStatement pStmt2 = conn.prepareStatement(sql2);
			pStmt2.setFloat(1, updatedBalance);
			pStmt2.setInt(2, customer.getCid());
			pStmt2.execute();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public int retrieveAcctNum(Customer customer)
	{
		try(Connection conn = DBUtil.getConnection();)
		{
			String sql = "SELECT acct_num " + 
						 "FROM account, customer " +
						 "WHERE account.cid = ?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setInt(1, customer.getCid());
			
			ResultSet myRs = pStmt.executeQuery();
			
			if(myRs.next())
			{
				int acct_num = myRs.getInt("acct_num");
				return acct_num;
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	@Override
	public Boolean checkUserName(String username) {
		// TODO Auto-generated method stub
		try(Connection conn = DBUtil.getConnection();)
		{
			String sql = "SELECT username " +
						 "FROM customer " + 
						 "WHERE username = ?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, username);
			ResultSet myRs = pStmt.executeQuery();
			if(myRs.first())
				return false;
			else
				return true;
		}catch(SQLException e)
		{
				e.printStackTrace();
		}
		return false;
	}
	@Override
	public void updateCustomer(int cid, String email, String username, String password) {
		try(Connection conn = DBUtil.getConnection();)
		{
			String sql = "UPDATE customer " +
						 "SET email = ?, username = ?, password = ? " + 
						 "WHERE cid = ?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, email);
			pStmt.setString(2, username);
			pStmt.setString(3, password);
			pStmt.setInt(4, cid);
			
			pStmt.execute();
		}catch(SQLException e)
		{
				e.printStackTrace();
		}
		
	}
	@Override
	public Customer findCustomer(String username, HttpServletRequest request)
	{
		ArrayList<Customer> customerList = new ArrayList<>();
		
		Customer currentCustomer = null;
		
		try(Connection conn = DBUtil.getConnection();)
		{
			// SQL statement to get users from the database
			String sql = "SELECT * FROM customer";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			ResultSet rs = pstmt.executeQuery();
			
			// process rows
			while(rs.next())
			{
				int cid = rs.getInt(1);
				String name = rs.getString(2);
				String email = rs.getString(3);
				String uname = rs.getString(4);
				String pw = rs.getString(5);
				String bd = rs.getString(6);
				String gender = rs.getString(7);
				customerList.add(new Customer(cid,name,email,uname,pw,bd,gender));
			}
			// check if the username and password matches a customer in the database
			for(Customer cust: customerList)
			{
				if(cust.getUsername().equals(username))
					currentCustomer = cust;
			}
		} catch(SQLException e)
		{
			e.printStackTrace();
		}
		if(currentCustomer == null)
			return null;
		request.setAttribute("user", currentCustomer);
		
		return currentCustomer;
	}
	@Override
	public void resetCustomer(int cid, String password) {
		try(Connection conn = DBUtil.getConnection();)
		{
			String sql = "UPDATE customer " +
						 "SET password = ? " + 
						 "WHERE cid = ?";
			PreparedStatement pStmt = conn.prepareStatement(sql);
			pStmt.setString(1, password);
			pStmt.setInt(2, cid);
			
			pStmt.execute();
		}catch(SQLException e)
		{
				e.printStackTrace();
		}
		
	}
	@Override
	public void sendWelcomeEmail(String name,String email)
	{
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.live.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props,
		  new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userEmail, password);
			}
		  });

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(userEmail)); // same email id
			message.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse(email));// send to 
			String welcomeSubjectLine = "Welcome to capital 11 Bank: " + name;
			message.setSubject(welcomeSubjectLine);
			String body = "We are glad that you chose capital 11 bank.";
			message.setText(body);

			Transport.send(message);

			//System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
		
		
	}
}

