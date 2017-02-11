/**
 * \file Example_confgen.java
 * This example illustrates the way ApMon can obtain the configuration 
 * parameters from a servlet or a CGI script.
 */ 

import java.util.Vector;
import java.util.logging.Logger;

import apmon.*;

public class Example_confgen {
	private static Logger logger = Logger.getLogger("apmon");
	
	public static void main(String args[]) {
		Vector destList = new Vector(); 
		int nDatagrams = 20;
		ApMon apm = null;
		double val = 0;
		int i, timestamp;
		
		if (args.length == 1)
			nDatagrams = Integer.parseInt(args[0]);

		destList.add("http://pcardaab.cern.ch:8888/cgi-bin/ApMonConf?appName=confgen_test"); 
		
		try {
			apm = new ApMon(destList);
			
		} catch (Exception e) {
			logger.severe("Error initializing ApMon: " + e);
			System.exit(-1);
		}
		
		apm.setRecheckInterval(300);
		for (i = 0; i < nDatagrams - 1; i++) {
			/* add a value for the CPU load (random between 0 and 2) */
			val += 0.05;
			if (val > 2)
				val = 0;
			
			logger.info("Sending " + val + " for cpu_load");
					
			try {
				/* use the wrapper function with simplified syntax */
				apm.sendParameter("TestClusterCG_java", null, "cpu_load", 
						ApMon.XDR_REAL64, new Double(val));
				
				/* now send a datagram with timestamp (as if this was 5h ago) */
				timestamp = (int)System.currentTimeMillis() / 1000 - (5 * 3600);
				apm.sendTimedParameter("TestClusterOldCG_java", null, "cpu_load", 
						ApMon.XDR_REAL64, new Double(val), timestamp);
			} catch(Exception e) {
				logger.warning("Send operation failed: " + e);
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {}
		} // for
		apm.stopIt();			
		
	}
}
