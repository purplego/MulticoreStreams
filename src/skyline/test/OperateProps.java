package skyline.test;

import java.io.File;

import skyline.model.MyProperties;

public class OperateProps {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String filePath = "./config/" + "prop.properties";
		if(new File(filePath).isFile()){
			
			MyProperties mypro = new MyProperties(filePath);

			System.out.println(mypro.getValueByKey("LOG_DIRECTORY"));
			
//			ParseProperties.parseProperties(filePath);
//			
//			System.out.println(Constants.QueryGran);
			
			/* Properties sysProp = System.getProperties();
			Enumeration en = sysProp.propertyNames();
			while(en.hasMoreElements()){
				String key = (String) en.nextElement();
				String value = sysProp.getProperty(key);
				System.out.println(key + "=" + value);
			}*/
			
		}

	}

}
