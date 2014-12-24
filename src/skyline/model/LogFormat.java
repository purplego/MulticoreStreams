package skyline.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * 定义消息输出格式： 消息等级 + 消息内容
 */
public class LogFormat extends Formatter {

	@Override
	public String format(LogRecord record) {
		
		SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
		
		return (datef.format(new Date()) + " " + record.getLevel() + ": " + record.getMessage() +"\n");
	}

}
