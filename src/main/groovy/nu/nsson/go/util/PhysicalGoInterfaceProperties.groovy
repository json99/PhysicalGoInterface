package nu.nsson.go.util

import java.util.Properties

class PhysicalGoInterfaceProperties {
	private static PhysicalGoInterfaceProperties instance = null
	
	public static PhysicalGoInterfaceProperties getInstance() {
		if(instance == null) {
			instance = new PhysicalGoInterfaceProperties()
		}
		
		return instance
	}
	
	private Properties configuration;
	
	private PhysicalGoInterfaceProperties() {
		configuration = new Properties()
		
		def propertyFile = new File("physicalgointerface.properties")
		
		if(propertyFile.exists()) {
			configuration.load(propertyFile.newInputStream())
		}
	}
	
	private String getValue(String key, String defaultValue) {
		def value = configuration.get(key)
		
		return (value ? value : defaultValue)
	}
	
	private int getValueInt(String key, int defaultValue) {
		def value = configuration.get(key)
		
		return (value ? value.toInteger() : defaultValue)
	}
	
	public int getWebCamDeviceId(int defaultValue) {
		return getValueInt("WebCamDeviceId", defaultValue)
	}
	
	public String getGnuGoPath(String defaultValue) {
		return getValue("GnuGoPath", defaultValue)
	}
		
} 
