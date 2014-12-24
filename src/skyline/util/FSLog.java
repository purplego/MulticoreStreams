package skyline.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
/**
 * @author Purple Wang
 * Jan 20, 2014
 */
public class FSLog {
	private PrintStream output;
	private PrintStream err;
	private String filePath;
	
	/**
	 * 带参数的构造函数
	 * @param logDir
	 * @param logname
	 */
	public FSLog(String logDir, String logname){
		this.output = System.out;
		this.err = System.err;
		this.filePath = logDir + logname;
	}
	
	public void message(String msg){								//输出到控制台
		msg = "message: " + new Date() + " // " + msg;
		output.println(msg);
	}
	
	public void info(String msg){									//输出到文件（写文件）
		File fileOut = new File(filePath);	
		FileWriter fWriter;
		try {
			msg = "message: " + new Date() + " // " + msg;
			fWriter = new FileWriter(fileOut, true);				//第二个参数为true表示设置为追加写
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			bWriter.append(msg);
			bWriter.flush();
			bWriter.newLine();
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void error(String errmsg){								//输出到控制台的同时写到文件
		File fileOut = new File(filePath);	
		FileWriter fWriter;
		try {
			errmsg = "message: " + new Date() + " // " + errmsg;
			err.println(errmsg);
			fWriter = new FileWriter(fileOut, true);				//第二个参数为true表示设置为追加写
			BufferedWriter bWriter = new BufferedWriter(fWriter);
			bWriter.append(errmsg);
			bWriter.flush();
			bWriter.newLine();
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
