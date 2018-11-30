package com.log8430.rest;

import javax.servlet.*;

public class ServletContextHolder implements ServletContextListener {
	public static ServletContext context;

	@Override
	public void contextInitialized(ServletContextEvent servletContextEvent) {
		context = servletContextEvent.getServletContext();
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		context = null;
	}
}
