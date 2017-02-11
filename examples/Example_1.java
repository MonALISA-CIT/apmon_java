/**
 * \file Example_1.java
 * This is a simple example for using the ApMon class. 
 * It illustrates the way to send to MonALISA an UDP datagram with a 
 * parameter and its value, using the function sendParameter().
 * The number of datagrams can be specified in the command line (if it is not,
 * the default is 20).
 * The file "destinations_1.conf" contains the addresses of the hosts to which
 * we send the parameters, and also the corresponding ports.
 */ 
 
import java.util.Random;
import java.util.logging.Logger;

import apmon.*;

public class Example_1 {
	private static Logger logger = Logger.getLogger("apmon");
	
	public static void main(String args[]) {	    
		String filename = "destinations_1.conf";
		long nDatagrams = 20, i;
		ApMon apm = null;
		double val = 0;		
		Random rgen = new Random();
		
		if (args.length == 1)
			nDatagrams = Integer.parseInt(args[0]);
		
		try {
			apm = new ApMon(filename);
		} catch (Exception e) {
			logger.severe("Error initializing ApMon: " + e);
			System.exit(-1);    
		}
		
		apm.setMaxMsgRate(60);
		
		/* for the first datagram sent we specify the cluster name, which will be
	       cached and used for the next datagrams; the node name is null, so the 
	       local IP will be sent instead) */
		try {
			apm.sendParameter("TestCluster1_java", null, "my_cpu_load", val);
			Thread.sleep(1000);  
		} catch(Exception e) {
			logger.warning("Send operation failed: " + e);
		}
		
		for (i = 0; i < nDatagrams - 1; i++) {
			/* add a value for the CPU load (random between 0 and 2) */
			val = 2 * rgen.nextDouble();  
			//logger.info("Sending " + val + " for my_cpu_load");
			
			/* use the wrapper function with simplified syntax */
			/* (the node name is left null, so the local hostname will be sent instead) */
			try {
				apm.sendParameter("TestCluster1_java", null, "my_cpu_load", val);
				/* just a short delay to test the limitation of the message rate */
				for(int j = 0; j < 1000000; j++);
				//Thread.sleep(1000);  
			} catch(Exception e) {
				logger.warning("Send operation failed: " + e);
			}
		} // for
		apm.stopIt();
	}
}
