package com.ctb.prism.core.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is the implementation class of the interface <b>IAppLogger</b>. This
 * implementation class performs all logging activity using Apache log4j logger.
 * 
 * @author TATA CONSULTANCY SERVICES
 * @version 1.0
 **/
public class AppLoggerImpl implements IAppLogger {
	/**
	 * A Logger type variable to store the logger to be used.
	 **/
	private transient final Logger logger;

	/**
	 * Constructor to retrieve a <i>Logger</i> object for the given class.
	 * 
	 * @param className
	 **/
	public AppLoggerImpl(String className) {
		// Get a logger
		logger = LoggerFactory.getLogger(className);
	}

	/**
	 * This method is responsible for mapping the application logger level with
	 * the slf4j logger methods.
	 * 
	 * @param level
	 *            - This is application logger level
	 * @param message
	 *            - This is the message to be logged in the configured log file
	 **/
	public void log(final int level, String message) {
		message = logger.getName() + ": " + message;
		switch (level) {
		case 1:
			logger.trace(message);
			break;
		case 2:
			logger.info(message);
			break;
		case 3:
			logger.debug(message);
			break;
		case 4:
			logger.warn(message);
			break;
		case 5:
			logger.error(message);
			break;
		default:
			logger.warn("No Logger Level Found");
		}
	}

	/**
	 * This method is responsible for mapping the application logger level with
	 * the slf4j logger methods
	 * 
	 * @param level
	 *            - This is application logger level
	 * @param message
	 *            - This is the message to be logged in the configured log file
	 * @param thrower
	 *            - This is the exception to be logged
	 **/
	public void log(final int level, String message, final Throwable thrower) {
		message = logger.getName() + ": " + message;
		switch (level) {
		case 1:
			logger.trace(message, thrower);
			break;
		case 2:
			logger.info(message, thrower);
			break;
		case 3:
			logger.debug(message, thrower);
			break;
		case 4:
			logger.warn(message, thrower);
			break;
		case 5:
			logger.error(message, thrower);
			break;
		default:
			logger.warn("No Logger Level Found");
		}
	}

}