package org.zihao.opc.utils;

import java.io.IOException;
import java.util.Properties;

/**
 * 
 * @author zihaozhu
 * @date 2017/10/28
 */
public class PropertyFileManager {
	private static Properties mProperties;

	private PropertyFileManager() {
	};

	public static void loadPropertyFile(String fileName) {
		if (null == mProperties) {
			mProperties = new Properties();
			try {
				mProperties.load(PropertyFileManager.class.getClassLoader().getResourceAsStream(fileName));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static String getValue(String keyName) {
		String value = null;
		if (null != mProperties) {
			if (mProperties.containsKey(keyName)) {
				Object object = mProperties.get(keyName);
				value = object.toString();
			}
		}
		return value;
	}
}
