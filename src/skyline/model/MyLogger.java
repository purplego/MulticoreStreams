package skyline.model;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 封装了java自带的Logger类，用于写日志
 * Logger 上执行的所有方法都是多线程安全的
 * 
 * @author Administrator
 *
 */
public class MyLogger {
	
	private Logger logger;
	private String filePath;
	
	/**
	 * The constructor
	 * @param filePath
	 * @param loggerStr
	 */
	public MyLogger(String filePath, String loggerStr){
		
		this.filePath = filePath;
		this.logger = Logger.getLogger(loggerStr);						// 为指定子系统查找或创建一个 logger(static Logger方法)
		logger.setLevel(Level.INFO);									// 设置日志级别,指定此 logger 记录的消息级别
		
		try {
			FileHandler fileHandler = new FileHandler(filePath,true);	// 初始化要写入给定文件名的 FileHandler(使用可选的 append)
			fileHandler.setLevel(Level.ALL);							// 设置日志级别,指定该 Handler 所记录的信息级别(将丢弃低于该值的信息级别)
			fileHandler.setFormatter(new LogFormat());					// 设置 Formatter:将该 Formatter 用于格式化该 Handler 的 LogRecords
			
			logger.addHandler(fileHandler);								// 添加一个日志 Handler 以接收日志记录消息
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create a log info at INFO level
	 * @param msg
	 */
	public void info(String msg){
		logger.info(msg);
	}
}
