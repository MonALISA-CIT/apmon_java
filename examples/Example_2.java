/**
 * \file Example_2.java
 * This example illustrates the way to send to MonALISA an UDP datagram with a 
 * single parameter, using the sendParameter() and sendTimedParameter()
 * functions.
 * The number of parameters that will be sent can be specified in the command 
 * line (if it is not, the default is 20). For each parameter value two 
 * datagrams are sent: one with a timestamp and one without a timestamp. For the
 * latter, the local time at the MonALISA host will be considered.
 */ 
import java.util.Vector;
import java.util.logging.Logger;

import apmon.*;

public class Example_2 {
	private static Logger logger = Logger.getLogger("apmon");
	
	public static void main(String args[]) {
		Vector destList = new Vector(); 
		int nDatagrams = 20;
		ApMon apm = null;
		double val = 0;
		int i, timestamp;
		
		if (args.length == 1)
			nDatagrams = Integer.parseInt(args[0]);
		
		destList.add(new String("ce.rogrid.pub.ro:8884 password"));
		destList.add(new String("http://lcfg.rogrid.pub.ro/~corina/destinations_2.conf"));
		
		try {
			apm = new ApMon(destList);
			
		} catch (Exception e) {
			logger.severe("Error initializing ApMon: " + e);
			System.exit(-1);
		}
		
		// set the time interval for periodically checking the 
		// configuration URL
		apm.setRecheckInterval(300);
		// this way we can change the logging level
		ApMon.setLogLevel("DEBUG");
		for (i = 0; i < nDatagrams - 1; i++) {
			/* add a value for the CPU load (random between 0 and 2) */
			val += 0.05;
			if (val > 2)
				val = 0;
			
			logger.info("Sending " + val + " for my_cpu_load");
					
			try {
				/* use the wrapper function with simplified syntax */
				apm.sendParameter("TestCluster2_java", null, "my_cpu_load", 
						ApMon.XDR_REAL64, new Double(val));
				
				/* now send a datagram with timestamp (as if this was 5h ago) */
				long crtTime = System.currentTimeMillis();
				timestamp = (int)(crtTime / 1000 - (5 * 3600));
				apm.sendTimedParameter("TestClusterOld2_java", null, "my_cpu_load", 
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
