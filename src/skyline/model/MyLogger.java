package skyline.model;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ��װ��java�Դ���Logger�࣬����д��־
 * Logger ��ִ�е����з������Ƕ��̰߳�ȫ��
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
		this.logger = Logger.getLogger(loggerStr);						// Ϊָ����ϵͳ���һ򴴽�һ�� logger(static Logger����)
		logger.setLevel(Level.INFO);									// ������־����,ָ���� logger ��¼����Ϣ����
		
		try {
			FileHandler fileHandler = new FileHandler(filePath,true);	// ��ʼ��Ҫд������ļ����� FileHandler(ʹ�ÿ�ѡ�� append)
			fileHandler.setLevel(Level.ALL);							// ������־����,ָ���� Handler ����¼����Ϣ����(���������ڸ�ֵ����Ϣ����)
			fileHandler.setFormatter(new LogFormat());					// ���� Formatter:���� Formatter ���ڸ�ʽ���� Handler �� LogRecords
			
			logger.addHandler(fileHandler);								// ���һ����־ Handler �Խ�����־��¼��Ϣ
			
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
