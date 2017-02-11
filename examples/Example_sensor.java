 /**
 * \file Example_sensor.java
 * This example shows how ApMon can be used for collecting system monitoring
 * information. The program acts like a simple sensor, which only sends 
 * system monitoring datagrams in the background thread. The time interval at
 * which the datagrams are sent can be set from the destinations_s.conf file.
 */ 

import java.util.logging.Logger;

import apmon.*;

public class Example_sensor {
	private static Logger logger = Logger.getLogger("apmon");
	
	public static void main(String args[]) {	    
		String filename = "destinations_s.conf";
		ApMon apm = null;
		
		try {
			apm = new ApMon(filename);
			//apm.setMonitorClusterNode("SensorCluster", null);
		} catch (Exception e) {
			logger.severe("Error initializing ApMon: " + e);
			System.exit(-1);    
		}	
		//set the time interval at which the configuration file is checked for changes	
		apm.setRecheckInterval(300);
		
		while (true) {
		    try {
			Thread.sleep(1000);
		    } catch (Exception e) {}
		}
	}
}
