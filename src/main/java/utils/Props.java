package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Props {

	private final static Logger log = LogManager.getLogger(Props.class);
	private static final String CONFIG_PROPERTIES = "config.properties";
	private static final String GLOBAL_PROPERTIES = "global.properties";

	static {
		try {

			boolean exists = new File(GLOBAL_PROPERTIES).exists();

			new FileOutputStream(Props.CONFIG_PROPERTIES, true).close();
			new FileOutputStream(Props.GLOBAL_PROPERTIES, true).close();

			if (!exists) {
				Props.setGlobalProperty(GlobalProperties.SORT_BY_NAME, GlobalProperties.SORT_BY_NAME_DEFAULT_VALUE);
				Props.setGlobalProperty(GlobalProperties.APP_NAME, GlobalProperties.APP_NAME_DEFAULT);
			}

		} catch (IOException e) {
			log.error(e);
		}

	}

	public static void setGlobalProperty(String propKey, String propVal) {
		setProperty(propKey, propVal, false);
	}

	public static void setUserProperty(String propKey, String propVal) {
		setProperty(propKey, propVal, true);
	}

	public static String getGlobalProperty(String propKey) {
		return getProperty(propKey, false);
	}

	public static String getUserProperty(String propKey) {
		return getProperty(propKey, true);
	}

	private static void setProperty(String propKey, String propVal, boolean userProps) {
		
		String propFile = userProps ? CONFIG_PROPERTIES : GLOBAL_PROPERTIES;
		File f = new File(propFile);
		if (!f.exists() && !f.isDirectory()) {
			Path file = Paths.get(propFile);
			try {
				Files.write(file, new ArrayList<String>(), Charset.forName("UTF-8"));
			} catch (IOException e) {
				log.error(e);
			}
		}
		try (InputStream input = new FileInputStream(propFile);) {
			Properties prop = new Properties();
			prop.load(input);
			// set the properties value
			prop.put(propKey, propVal);
			// save properties to project root folder
			try (OutputStream output = new FileOutputStream(propFile);) {
				prop.store(output, null);
			}

		} catch (IOException io) {
			log.error(io);
			throw new RuntimeException("value not set for key: " + propKey + " - value: " + propVal);
		}
	}

	private static String getProperty(String propKey, boolean userProps) {
		String propFile = userProps ? CONFIG_PROPERTIES : GLOBAL_PROPERTIES;
		try (InputStream input = new FileInputStream(propFile);) {
			Properties prop = new Properties();
			// load a properties file
			prop.load(input);
			return prop.getProperty(propKey);
		} catch (IOException ex) {
			log.error(ex);
			throw new RuntimeException("value not found for key: " + propKey);
		}

	}

	public static void deleteProperty(String string, boolean userProps) {
		String propFile = userProps ? CONFIG_PROPERTIES : GLOBAL_PROPERTIES;
		try (InputStream input = new FileInputStream(propFile);) {
			Properties prop = new Properties();
			prop.load(input);
			// set the properties value
			prop.remove(string);
			// save properties to project root folder
			try (OutputStream output = new FileOutputStream(propFile);) {
				prop.store(output, null);
			}

		} catch (IOException io) {
			log.error(io);
			throw new RuntimeException("value not deleted for key: " + string);
		}
	}

	public static class GlobalProperties {
		public static String SORT_BY_NAME = "soryByName";
		public static String SORT_BY_NAME_DEFAULT_VALUE = "true";

		public static String APP_NAME = "appName";
		public static String APP_NAME_DEFAULT = "Test";
	}
}