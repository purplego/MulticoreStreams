package skyline.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;

public class MyProperties {
	
	private Properties prop;
	private String filePath; // the file path of the config file
	
	/**
	 * The constructor
	 * @param filePath
	 */
	public MyProperties(String filePath){
		
		this.prop = new Properties();
		this.filePath = filePath;
		
		try {
			
//			InputStream instream = new BufferedInputStream(new FileInputStream(filePath));
			InputStream instream = new FileInputStream(filePath);
			this.prop.load(instream);
			
			instream.close();
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Get value by key
	 * @param key
	 * @return
	 */
	public String getValueByKey(String key){
		
		String value = this.prop.getProperty(key);
		return value;
	}
	
	/**
	 * Enumerate all information in properties file
	 * @param prop
	 */
	public Enumeration enumProperties(){
		
//		for(Enumeration en = prop.propertyNames(); en.hasMoreElements();){
//			System.out.println(en.nextElement());
//		}
		Enumeration en = prop.propertyNames();
		while(en.hasMoreElements()){
			String key = (String) en.nextElement();
			String proper = prop.getProperty(key);
			System.out.println(key + "=" + proper);
		}
		return en;
	}
	
	/**
	 * Return all properties names 
	 * @return
	 */
	public Set<String> propertiesNames(){
		
		return prop.stringPropertyNames();
	}
	
	/**
	 * Write a new key-value pair into properties file
	 * @param key
	 * @param value
	 */
	public void setProperties(String key, String value){
		
		try {
			OutputStream outstream = new FileOutputStream(filePath);
			prop.setProperty(key, value);
			prop.store(outstream, "Update " + key + "=" + value);
			
			outstream.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Visit " + filePath + " for updating " + key + "=" + value + " error.");
		}
	}
	
	/**
	 * Dose the file under the filePath really exist?
	 * @return
	 */
	public boolean isFileExist(){
		return new File(filePath).isFile();
	}
	
	
}
