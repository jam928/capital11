package com.capital11.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.capital11.beans.Account;
import com.capital11.beans.Customer;
import com.capital11.beans.Transaction;
import com.capital11.dao.DAO;
import com.capital11.dao.DAOImp;

public class RequestHelper {
	public String process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		DAOImp user = new DAO();
		switch(request.getRequestURI())
		{
			case "/capital11/register.do":
			{
				String name = request.getParameter("name");
				String email = request.getParameter("email");
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				String birthday = request.getParameter("BirthMonth") + "/" + request.getParameter("BirthDay") + "/" + request.getParameter("BirthYear");
				String gender = request.getParameter("gender");
				String acctType = request.getParameter("acct");
				Random random = new Random();
				int cid = random.ints(1000,100000).findFirst().getAsInt();
								
				Customer customer = new Customer(cid,name,email,username,password,birthday,gender,new Account(acctType));
				user.addCustomer(customer);
				return "login.jsp";
			}
			case "/capital11/login.do":
			{
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				
				Customer customer = user.login(username,password,request);
				if(customer == null)
				{
					request.setAttribute("nouser", "no user found");
					return "login.jsp";
				}
				return "main.jsp";
			}
			case "/capital11/deposit.do":
			{
				float amount = Float.parseFloat(request.getParameter("depAmt"));
				
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				Date today = Calendar.getInstance().getTime();        
			    String reportDate = df.format(today);
			    
			    // Get HttpSession Object
				HttpSession session = request.getSession();
				Customer currentCustomer = (Customer)session.getAttribute("currentUser");
			    
				Transaction transaction = new Transaction(reportDate, "deposit", amount);
				user.addTransaction(transaction, currentCustomer,request);
				
				return "default.do";
			}
			case "/capital11/withdrawl.do":
			{
				float amount = Float.parseFloat(request.getParameter("depAmt"));
				
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
				Date today = Calendar.getInstance().getTime();        
			    String reportDate = df.format(today);
			    
			    // Get HttpSession Object
				HttpSession session = request.getSession();
				Customer currentCustomer = (Customer)session.getAttribute("currentUser");
			    
				Transaction transaction = new Transaction(reportDate, "withdrawl", amount);
				
				// add transaction, if the balance is less than 0 it will return 0
				user.addTransaction(transaction, currentCustomer, request);
				
				return "default.do";
			}
			case "/capital11/exist.do":
			{
				String username = request.getParameter("username");
				// check if username is taken
				Boolean isAvaliable = user.checkUserName(username);
		        response.setContentType("text/html;charset=UTF-8");
				PrintWriter out = response.getWriter();
				if(!isAvaliable)
				{
					out.println("<font color=red>");
                    out.println("Taken");
                    out.println("</font>");
				}
				else
				{
					 out.println("<font color=green>");
                      out.println("Avaliable");
                      out.println("</font>");
				}
			}
			case "/capital11/update.do":
			{
				String email = request.getParameter("email");
				String username = request.getParameter("username");
				String password = request.getParameter("password");
				HttpSession session = request.getSession();
				Customer currentCustomer = (Customer)session.getAttribute("currentUser");
				int cid = currentCustomer.getCid();
				user.updateCustomer(cid,email,username,password);
				return "default.do";
			}
			case "/capital11/verify.do":
			{
				String username = request.getParameter("username");
				Customer currentCustomer = user.findCustomer(username,request);
				if(currentCustomer == null)
					return "verifyReset.jsp";
				return "reset.jsp";
			}
			case "/capital11/reset.do":
			{
				int cid = Integer.parseInt(request.getParameter("cid"));
				String password = request.getParameter("password");
				user.resetCustomer(cid,password);
				return "login.jsp";
			}
			case "/capital11/default.do":
			{
				// Get HttpSession Object
				HttpSession session = request.getSession();
				Customer currentCustomer = (Customer)session.getAttribute("currentUser");
				ArrayList<Transaction> listOfTransactions = user.listTransactions(currentCustomer);
				request.setAttribute("listOfTransactions", listOfTransactions);
				request.setAttribute("balance", user.retrieveBalance(currentCustomer));
				request.setAttribute("name", currentCustomer.getName());
				//request.setAttribute("customer", currentCustomer);

				return "main.jsp";
			}
			case "/capital11/logout.do":
			{
				HttpSession session = request.getSession();
				session.invalidate();
				return "login.jsp";
			}
			
		}
		return null;
	}
}
