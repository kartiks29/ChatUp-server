package com.org.chatup;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

public class Config implements ServletContextListener {
    private static final String ATTRIBUTE_NAME = "config";
    private DataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
    	System.out.println("contextInitialized()");
        ServletContext servletContext = event.getServletContext();
        String databaseName = servletContext.getInitParameter("database.name");
        try {
            dataSource = (DataSource) new InitialContext().lookup(databaseName);
        } catch (NamingException e) {
            throw new RuntimeException("Config failed: datasource not found", e);
        }
        servletContext.setAttribute(ATTRIBUTE_NAME, this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // NOOP.
    	System.out.println("contextDestroyed()");
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public static Config getInstance(ServletContext servletContext) {
        return (Config) servletContext.getAttribute(ATTRIBUTE_NAME);
    }
}