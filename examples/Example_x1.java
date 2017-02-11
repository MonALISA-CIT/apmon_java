/**
 * \file Example_x1.java
 * This is a simple example for using the ApMon class and the xApMon extension.
 * It illustrates the way to set up job and system monitoring, and also sends
 * datagrams with parameters specified by the user. This example "monitors
 * itself" (i.e., sends datagrams containing information about the current
 *  process).  
 * The file "destinations_x1.conf" contains the addresses of the hosts to which
 * we send the parameters, and also the corresponding ports. The file also
 * contains settings for the job and system monitoring (there is also the 
 * possibility to enable or disable each monitoring parameter separately).
 */ 

import java.util.Random;
import java.util.logging.Logger;
import java.util.logging.Level;

import apmon.*;
import apmon.util.*;

public class Example_x1 {
	private static Logger logger = Logger.getLogger("apmon");
    /* we need this so that we can use the getpid() function */
    static {
	    String osName = System.getProperty("os.name");
	    if (osName.indexOf("Linux") >=0)
		System.loadLibrary("nativeapm");	
    }

	public static void main(String args[]) {
		String filename = "destinations_x1.conf";  		
		long nDatagrams = 20, i;  
		double val;
		int mypid = 0;
		ApMon apm = null;
		String clusterName, workdir;
		
		/* use a simple native function provided by ApMon, which is similar
		 * with getpid()
		 */
		String osName = System.getProperty("os.name");
		if (osName.indexOf("Linux") >= 0)
			mypid = (int) (new NativeLinux().mygetpid());
		else {
			logger.severe("This example is only for Linux systems.");
			System.exit(-1);
		}		
		
		
		/* get the current working directory */
		workdir = System.getProperty("user.dir");
		clusterName = "JobClusterx1_java";
		
		Random rgen = new Random();
		
		if (args.length == 1)
			nDatagrams = Integer.parseInt(args[0]);
		
		try {
			apm = new ApMon(filename);
				
			// alternative way to initialize ApMon:
			/*
			Vector destList = new Vector(); 
			destList.add("http://lcfg.rogrid.pub.ro/~corina/destinations_x1.conf");
			destList.add("http://cipsm.rogrid.pub.ro/~corina/destinations_x1.conf");
			apm = new ApMon(destList);
			*/
			
			apm.addJobToMonitor(mypid, workdir, clusterName, null);
		} catch (Exception e) {
		    logger.log(Level.SEVERE, "Error initializing ApMon: ", e);
		    //e.printStackTrace(System.out);
			System.exit(-1);    
		}	

		logger.info("process PID = " + mypid);
		// set the time interval at which the configuration file is checked for changes	
		apm.setRecheckInterval(180);    
		for (i = 0; i < nDatagrams; i++) {
			/** add a value for the CPU load (random between 0 and 2) */
			val = 2 * rgen.nextDouble();  	
			logger.info("Sending " + val + " for cpu_load");
			
			try {
				apm.sendParameter("TestClusterx1_java", null, "cpu_load", val);
				Thread.sleep(1000);  
			} catch(Exception e) {
				logger.warning("Send operation failed: " + e);
			}
			// display a system monitoring parameter
			Double paramVal = apm.getSystemParameter("load1");
			System.out.println("Load1 for the system: " + paramVal);
			
			// this way we can change the logging level
			if (i == 15)
			    ApMon.setLogLevel("INFO");
		} // for
		apm.stopIt();
		
		
	}
}
