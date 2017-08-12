package com.capital11.web;

import java.io.IOException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;




public class HostServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse refactor) throws ServletException, IOException {
		
		String nextPage = new RequestHelper().process(request, refactor);
		request.getRequestDispatcher(nextPage).forward(request, refactor);
		
	}
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String nextPage = new RequestHelper().process(request, response);
		request.getRequestDispatcher(nextPage).forward(request, response);
		
	}

}
